package com.naver.naverspabackend.service.wifi.impl;

import com.google.gson.Gson;
import com.naver.naverspabackend.dto.WifiCategoryDto;
import com.naver.naverspabackend.dto.WifiDeviceDto;
import com.naver.naverspabackend.dto.WifiMemberDto;
import com.naver.naverspabackend.dto.WifiOrderDto;
import com.naver.naverspabackend.dto.WifiPointLogDto;
import com.naver.naverspabackend.dto.WifiProductDto;
import com.naver.naverspabackend.exception.WifiException;
import com.naver.naverspabackend.mybatis.mapper.WifiMapper;
import com.naver.naverspabackend.response.model.ResponseCode;
import com.naver.naverspabackend.service.payup.PayUpService;
import com.naver.naverspabackend.service.wifi.WifiAuthService;
import com.naver.naverspabackend.service.wifi.WifiCountries;
import com.naver.naverspabackend.service.wifi.WifiPricing;
import com.naver.naverspabackend.service.wifi.WifiService;
import com.naver.naverspabackend.service.wifi.WifiSmsSender;
import static com.naver.naverspabackend.service.wifi.WifiSmsSender.params;
import static com.naver.naverspabackend.service.wifi.WifiSmsSender.won;
import com.naver.naverspabackend.util.TugeDeviceUtil;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WifiServiceImpl implements WifiService {

    private static final int MAX_RETRY = 4;          // 첫 시도 + 재시도 3번 (1분 간격 → 약 3분)
    private static final int STATUS_TTL_SEC = 90;
    private static final int ORDERS_TTL_SEC = 300;

    /** 재시도해볼 만한 TUGE 코드: 통신오류, 호출과다/일시실패, 잔액부족, 주문흐름 실패, 내부오류, 주문은 됐으나 바인딩 조회 실패 */
    private static final Set<String> RETRYABLE = new HashSet<>(Arrays.asList(
        "NETWORK", "NOT_CONFIGURED", "1000", "1014", "1015", "1016", "2012", "4000"));

    @Autowired
    private WifiMapper wifiMapper;
    @Autowired
    private TugeDeviceUtil tuge;
    @Autowired
    private WifiPricing pricing;
    @Autowired
    private PayUpService payUpService;
    @Autowired
    private WifiSmsSender smsSender;
    @Autowired
    private WifiAuthService authService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${wifi.payupSdkUrl:https://standard.payup.co.kr/assets/js/payup_standard-1.0.js}")
    private String payupSdkUrl;
    @Value("${wifi.publicUrl:https://www.esimcity.co.kr/api}")
    private String publicUrl;
    @Value("${wifi.pointValidDays:365}")
    private int pointValidDays;

    private final Gson gson = new Gson();
    private final SecureRandom random = new SecureRandom();

    /* ===================== 장비 ===================== */

    @Override
    public List<WifiDeviceDto> myDevices(WifiMemberDto member) {
        return wifiMapper.myDevices(member.getId());
    }

    @Override
    public WifiDeviceDto registerDevice(WifiMemberDto member, String rawSn, String rawNickname) {
        String sn = normalizeSn(rawSn);
        String nickname = normalizeNickname(rawNickname);
        WifiDeviceDto existing = wifiMapper.findDevice(sn);
        if (existing != null && member.getId().equals(existing.getMemberId())) { // 이미 내 장비
            if (nickname != null) wifiMapper.updateNickname(sn, member.getId(), nickname);
            return wifiMapper.findDevice(sn);
        }
        if (existing != null && existing.getMemberId() != null) throw alreadyRegistered();

        // 소유 확인 없이 먼저 등록한 계정이 주인 (정책). 우리 채널 장비인지만 확인 (0000 개통 / 0002 미개통)
        Map<String, Object> check = tuge.checkDevice(sn);
        String code = String.valueOf(check.get("code"));
        if ("NOT_CONFIGURED".equals(code)) {
            // shortcut: TUGE 키 없는 로컬 개발용으로 장비 확인 생략. 운영은 api.tugeDevice 필수
            log.warn("[WIFI] api.tugeDevice 미설정 - 장비 확인 없이 등록 sn={}", sn);
        } else if (!"0000".equals(code) && !"0002".equals(code)) {
            throw new WifiException("등록할 수 없는 장비입니다. 장비번호를 확인해주세요. (" + code + ")");
        }
        Map<?, ?> data = check.get("data") instanceof Map ? (Map<?, ?>) check.get("data") : new HashMap<>();
        wifiMapper.upsertDevice(sn, str(data.get("deviceType")), str(data.get("deviceStatus")));

        if (wifiMapper.registerDevice(sn, member.getId(), nickname) == 0) throw alreadyRegistered(); // 동시 등록 경합
        wifiMapper.insertDeviceLog(sn, member.getId(), "REGISTER", null);
        return wifiMapper.findDevice(sn);
    }

    @Override
    public void renameDevice(WifiMemberDto member, String sn, String nickname) {
        WifiDeviceDto d = ownDevice(member, sn);
        wifiMapper.updateNickname(d.getSn(), member.getId(), normalizeNickname(nickname));
    }

    /** 앞뒤 공백 제거, 빈 값은 null(=SN 표시), 최대 20자 */
    static String normalizeNickname(String n) {
        String t = StringUtils.trimToNull(n);
        if (t == null) return null;
        t = t.replaceAll("\\s+", " ");
        if (t.codePointCount(0, t.length()) > 20) throw new WifiException("별명은 20자 이내로 입력해주세요.");
        return t;
    }

    private static WifiException alreadyRegistered() {
        return new WifiException("이미 등록된 장비입니다. 관리자에게 문의하세요.", ResponseCode.DUPLICATE_RESOURCE);
    }

    @Override
    public void releaseDevice(WifiMemberDto member, String sn) {
        ownDevice(member, sn);
        wifiMapper.releaseDevice(normalizeSn(sn), member.getId());
        wifiMapper.insertDeviceLog(normalizeSn(sn), member.getId(), "RELEASE", null);
    }

    @Override
    public Map<String, Object> deviceDetail(WifiMemberDto member, String rawSn) {
        WifiDeviceDto device = ownDevice(member, rawSn);
        String sn = device.getSn();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sn", sn);
        result.put("deviceType", device.getDeviceType());
        result.put("nickname", device.getNickname());

        // 실시간 상태 (3.10)
        Cached st = cached("wifi:st:" + sn, STATUS_TTL_SEC, () -> tuge.getConnectionStatus(sn), "1011", "1013");
        Map<String, Object> status = new LinkedHashMap<>();
        String stCode = st.res == null ? null : String.valueOf(st.res.get("code"));
        if ("0000".equals(stCode) && st.res.get("data") instanceof Map) {
            Map<?, ?> d = (Map<?, ?>) st.res.get("data");
            status.put("online", "online".equals(d.get("deviceStatus")));
            status.put("connection", d.get("deviceStatus"));
            status.put("battery", num(d.get("batteryRemaining")));
            status.put("signal", num(d.get("signal")));
            status.put("connectedDevices", num(d.get("hotspotDeviceCount")));
            status.put("networkType", d.get("networkType"));
            status.put("networkSpeed", d.get("networkSpeed"));
            status.put("mcc", d.get("mcc"));
            status.put("countryCode", d.get("countryCode"));
            status.put("lastLogin", TugeDeviceUtil.utcToKst(d.get("lastloginTime")));
            status.put("todayUsageMb", d.get("dataUsage"));   // 당일 사용량, 5분마다 갱신 (TUGE 답변: 요금제 목록 dataUsage 보다 정확)
            status.put("dataTotalMb", d.get("dataTotal"));
        } else if ("1011".equals(stCode)) {
            status.put("message", "아직 개통 전인 장비입니다. 요금제를 충전하면 개통됩니다.");
        } else if ("1013".equals(stCode)) {
            status.put("message", "이용 중인 요금제가 없습니다.");
        } else {
            status.put("message", "장비 상태를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
        status.put("stale", st.stale);
        result.put("status", status);

        // 요금제 (3.7, 누적 사용량 약 1시간 지연)
        Cached od = cached("wifi:od:" + sn, ORDERS_TTL_SEC, () -> tuge.queryOrderList(sn));
        List<Map<String, Object>> plans = new ArrayList<>();
        if (od.res != null && TugeDeviceUtil.isOk(od.res) && od.res.get("data") instanceof Collection) {
            Map<String, WifiProductDto> productCache = new HashMap<>();
            for (Object o : (Collection<?>) od.res.get("data")) {
                if (!(o instanceof Map)) continue;
                Map<?, ?> p = (Map<?, ?>) o;
                String productNo = str(p.get("productNo"));
                WifiProductDto prod = productNo == null ? null
                    : productCache.computeIfAbsent(productNo, k -> wifiMapper.findProduct(k));
                Map<String, Object> plan = new LinkedHashMap<>();
                plan.put("orderNo", p.get("orderNo"));
                plan.put("name", prod != null && StringUtils.isNotBlank(prod.getDisplayName()) ? prod.getDisplayName() : p.get("productName"));
                plan.put("productType", prod == null ? null : prod.getProductType());
                plan.put("dailyHighSpeedMb", prod == null ? null : prod.getDailyHighSpeedMb());
                plan.put("status", p.get("status"));
                // 장비 개통 상품은 켜기 전 날짜가 임시값(주문일 기준) → 켜면 다시 계산됨. 날짜 지정 상품은 주문 때 확정
                plan.put("dateFixed", prod == null || prod.isDateStart());
                plan.put("start", TugeDeviceUtil.utcToKst(p.get("startDate")));
                plan.put("end", TugeDeviceUtil.utcToKst(p.get("endDate")));
                plan.put("dataTotal", p.get("dataTotal"));
                plan.put("dataUsage", p.get("dataUsage"));
                plans.add(plan);
            }
        }
        result.put("plans", plans);
        result.put("plansStale", od.stale);
        result.put("pointBalance", member.getPointBalance());
        return result;
    }

    /* ===================== 상품 ===================== */

    @Override
    public List<WifiCategoryDto> categories(WifiMemberDto member, String sn) {
        return wifiMapper.userCategoryList(ownDevice(member, sn).getDeviceType());
    }

    @Override
    public List<WifiProductDto> products(WifiMemberDto member, String sn, Long categoryId) {
        List<WifiProductDto> list = wifiMapper.userProductList(categoryId, ownDevice(member, sn).getDeviceType());
        Map<Long, WifiCategoryDto> cats = pricing.categories();
        for (WifiProductDto p : list) {
            pricing.fillUnitPrice(p, cats);
            p.setSettlementUsd(null); // 원가 비노출
            p.setCountries(WifiCountries.of(p.getMcc(), p.getCountryOverride()));
            p.setMcc(null);
            p.setCountryOverride(null);
        }
        return list;
    }

    @Override
    public List<Map<String, String>> coverage(WifiMemberDto member, String sn) {
        Map<String, Map<String, String>> byIso = new HashMap<>();
        for (WifiProductDto p : wifiMapper.userProductList(null, ownDevice(member, sn).getDeviceType())) {
            for (Map<String, String> c : WifiCountries.of(p.getMcc(), p.getCountryOverride())) byIso.putIfAbsent(c.get("iso"), c);
        }
        List<Map<String, String>> list = new ArrayList<>(byIso.values());
        list.sort((a, b) -> a.get("name").compareTo(b.get("name")));
        return list;
    }

    @Override
    public Map<String, Object> productsByCountries(WifiMemberDto member, String sn, List<String> rawIsos) {
        List<String> isos = new ArrayList<>();
        for (String i : rawIsos) if (i != null && i.trim().matches("[A-Za-z]{2}") && !isos.contains(i.trim().toUpperCase())) isos.add(i.trim().toUpperCase());
        if (isos.isEmpty() || isos.size() > 10) throw new WifiException("나라를 1~10개 골라주세요.");
        List<WifiProductDto> all = products(member, sn, null);
        List<WifiProductDto> full = new ArrayList<>();
        List<WifiProductDto> partial = new ArrayList<>();
        for (WifiProductDto p : all) {
            List<String> has = new ArrayList<>();
            for (Map<String, String> c : p.getCountries()) has.add(c.get("iso"));
            List<String> matched = new ArrayList<>();
            List<String> missing = new ArrayList<>();
            for (String iso : isos) (has.contains(iso) ? matched : missing).add(iso);
            if (matched.isEmpty()) continue;
            p.setMatched(matched);
            p.setMissing(missing);
            (missing.isEmpty() ? full : partial).add(p);
        }
        Comparator<WifiProductDto> cheap = Comparator.comparing(p -> p.getSaleKrw() == null ? Integer.MAX_VALUE : p.getSaleKrw());
        full.sort(cheap);
        partial.sort(Comparator.<WifiProductDto>comparingInt(p -> -p.getMatched().size()).thenComparing(cheap));
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("full", full);
        r.put("partial", full.isEmpty() ? partial.subList(0, Math.min(5, partial.size())) : new ArrayList<>());
        return r;
    }

    @Override
    public WifiProductDto productDetail(WifiMemberDto member, String sn, String productCode) {
        WifiDeviceDto device = ownDevice(member, sn);
        WifiProductDto p = wifiMapper.findProduct(productCode);
        if (p == null || !isSellable(p, device)) throw new WifiException("판매 중인 상품이 아닙니다.", ResponseCode.NOT_EXIST);
        pricing.fillPrices(p);         // prices[n-1] = n일(월) 총액
        p.setSettlementUsd(null);      // 원가 비노출
        p.setPopupJson(null);
        p.setCountries(WifiCountries.of(p.getMcc(), p.getCountryOverride()));
        p.setMcc(null);
        return p;
    }

    private boolean isSellable(WifiProductDto p, WifiDeviceDto device) {
        if (!"Y".equals(p.getSaleYn()) || !"Y".equals(p.getTugeActive()) || p.getCategoryId() == null || p.getSettlementUsd() == null) return false;
        if (StringUtils.isBlank(p.getDeviceType()) || StringUtils.isBlank(device.getDeviceType())) return true;
        return Arrays.asList(p.getDeviceType().replace(" ", "").split(",")).contains(device.getDeviceType());
    }

    /* ===================== 주문 / 결제 ===================== */

    @Override
    public Map<String, Object> createOrder(WifiMemberDto member, String sn, String productCode, int period, int usePoint, String startDate) {
        WifiDeviceDto device = ownDevice(member, sn);
        WifiProductDto product = productCode == null ? null : wifiMapper.findProduct(productCode);
        if (product == null || !isSellable(product, device)) throw new WifiException("판매 중인 상품이 아닙니다.");

        // 기간: 선택 가능 상품은 [최소, 최대] 범위, 나머지는 1 고정. 금액은 서버가 나라 가격정책으로 계산
        if (product.isPeriodSelectable()) {
            if (period < product.getMinPeriod() || period > product.getMaxPeriod()) {
                throw new WifiException("이용 기간은 " + product.getMinPeriod() + "~" + product.getMaxPeriod() + periodUnit(product) + " 사이로 선택해주세요.");
            }
        } else {
            period = 1;
        }
        Integer price = pricing.price(product, period);
        if (price == null || price <= 0) throw new WifiException("판매 중인 상품이 아닙니다.");
        String start = product.isDateStart() ? checkStartDate(product, startDate) : null;
        int total = price;
        int point = Math.max(0, Math.min(usePoint, Math.min(member.getPointBalance(), total)));

        WifiOrderDto order = new WifiOrderDto();
        order.setOrderNo(newOrderNo());
        order.setMemberId(member.getId());
        order.setDeviceSn(device.getSn());
        order.setProductCode(product.getProductCode());
        order.setProductName(productLabel(product, period));
        order.setOrderPeriod(period);
        order.setStartDate(start);
        if (start != null) order.setProductName(order.getProductName() + " (" + start.substring(5).replace('-', '/') + "부터)");
        order.setTotalKrw(total);
        order.setPointUsed(point);
        order.setPayKrw(total - point);
        order.setSettlementUsd(product.getSettlementUsd() == null ? null
            : product.getSettlementUsd().multiply(BigDecimal.valueOf(period)));
        order.setPaymentStatus(order.getPayKrw() == 0 ? "POINT_ONLY" : "PAY_READY");
        order.setTopupStatus("READY");
        wifiMapper.insertOrder(order);

        Map<String, Object> r = new HashMap<>();
        r.put("orderNo", order.getOrderNo());
        if (order.getPayKrw() == 0) {
            if (!deductPoint(order)) {
                wifiMapper.changePaymentStatus(order.getId(), "POINT_ONLY", "PAY_FAILED", null);
                throw new WifiException("포인트 잔액이 부족합니다.");
            }
            fulfill(order);
            r.put("payRequired", false);
        } else {
            r.put("payRequired", true);
            r.put("payUrl", publicUrl + "/external/wifi/pay?orderNo=" + order.getOrderNo());
        }
        return r;
    }

    @Override
    public Map<String, Object> payPageModel(String orderNo) {
        WifiOrderDto order = wifiMapper.findOrderByNo(orderNo);
        if (order == null || !"PAY_READY".equals(order.getPaymentStatus())) return null;
        WifiMemberDto member = wifiMapper.findMemberById(order.getMemberId());
        Map<String, Object> m = new HashMap<>(payUpService.getPayConfig());
        m.put("orderNumber", order.getOrderNo());
        m.put("itemName", order.getProductName());
        m.put("amount", order.getPayKrw());
        m.put("totalKrw", order.getTotalKrw());
        m.put("pointUsed", order.getPointUsed());
        m.put("userName", member == null ? "고객" : WifiAuthService.maskTel(member.getTel()));
        m.put("sdkUrl", payupSdkUrl);
        return m;
    }

    @Override
    public String capturePayment(String transactionId, String orderNo, String amount) {
        WifiOrderDto order = wifiMapper.findOrderByNo(orderNo);
        if (order == null || !"PAY_READY".equals(order.getPaymentStatus())) return orderNo; // 중복 콜백

        // 콜백 금액은 신뢰하지 않음: DB 금액과 다르면 승인하지 않음
        int paid;
        try {
            paid = new BigDecimal(amount).intValueExact();
        } catch (Exception e) {
            paid = -1;
        }
        if (paid != order.getPayKrw()) {
            log.error("[WIFI] 결제금액 불일치 orderNo={} callback={} db={}", orderNo, amount, order.getPayKrw());
            wifiMapper.changePaymentStatus(order.getId(), "PAY_READY", "PAY_FAILED", transactionId);
            setTopup(order, "FAILED", "결제금액 불일치(" + amount + ")");
            smsSender.alertAdmin(WifiSmsSender.WF_ADM_AMOUNT, params("orderNo", orderNo), "결제금액 불일치 " + orderNo);
            return orderNo;
        }

        String confirm = payUpService.confirmPayment(transactionId, orderNo, String.valueOf(order.getPayKrw()));
        if (!"SUCCESS".equals(confirm)) {
            wifiMapper.changePaymentStatus(order.getId(), "PAY_READY", "PAY_FAILED", transactionId);
            setTopup(order, "FAILED", "PG 승인 실패(" + confirm + ")");
            return orderNo;
        }
        if (wifiMapper.changePaymentStatus(order.getId(), "PAY_READY", "PAID", transactionId) == 0) return orderNo;
        order.setPaymentStatus("PAID");
        order.setPgTransactionId(transactionId);

        if (order.getPointUsed() > 0 && !deductPoint(order)) {
            order.setPointUsed(0); // 차감 안 됐으니 복구할 포인트도 없음
            failAndRefund(order, "포인트 잔액 부족");
            return orderNo;
        }
        fulfill(order);
        return orderNo;
    }

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 날짜 지정 상품의 시작일 검사: 비우면 오늘, 오늘 ~ 개통 기한(validityPeriod) 안만 허용. 반환 yyyy-MM-dd */
    static String checkStartDate(WifiProductDto product, String startDate) {
        LocalDate today = LocalDate.now(KST);
        LocalDate d;
        try {
            d = StringUtils.isBlank(startDate) ? today : LocalDate.parse(startDate.trim());
        } catch (DateTimeParseException e) {
            throw new WifiException("사용 시작일을 다시 골라주세요.");
        }
        LocalDate last = today.plusDays(product.startWithinDays() - 1);
        if (d.isBefore(today) || d.isAfter(last)) {
            throw new WifiException("사용 시작일은 오늘부터 " + last.getMonthValue() + "월 " + last.getDayOfMonth() + "일 사이로 골라주세요.");
        }
        return d.toString();
    }

    /** TUGE addOrder startDate: 날짜 지정 상품은 고른 날(이미 지났으면 오늘), 그 외는 지금(개통 시 다시 계산됨) */
    static String tugeStartDate(String orderStartDate) {
        if (orderStartDate == null) return TugeDeviceUtil.utcNow();
        String today = LocalDate.now(KST).toString();
        return TugeDeviceUtil.startDateOf(orderStartDate.compareTo(today) < 0 ? today : orderStartDate);
    }

    /** 원자적 포인트 차감 + 이력 */
    private boolean deductPoint(WifiOrderDto order) {
        if (order.getPointUsed() <= 0) return true;
        if (wifiMapper.usePoint(order.getMemberId(), order.getPointUsed()) == 0) return false;
        pointLog(order.getMemberId(), -order.getPointUsed(), "USE", order.getId(), null, order.getProductName());
        return true;
    }

    @Override
    public void fulfill(WifiOrderDto order) {
        if ("DONE".equals(order.getTopupStatus()) || "FAILED".equals(order.getTopupStatus())) return;
        String sn = order.getDeviceSn();

        // 1) 미개통이면 개통 (3.2 → 3.3)
        Map<String, Object> check = tuge.checkDevice(sn);
        String code = String.valueOf(check.get("code"));
        if ("0002".equals(code)) {
            Map<String, Object> act = tuge.activeDevice(sn);
            code = TugeDeviceUtil.isOk(act) ? "0000" : String.valueOf(act.get("code"));
            if ("1010".equals(code)) code = "1000"; // 개통 실패는 일시 오류로 보고 재시도
        }
        if (!"0000".equals(code)) {
            onTugeFail(order, "개통확인", code, check.get("msg"));
            return;
        }

        // 2) 주문 (requestId = orderNo → 재시도해도 중복 주문 없음)
        Map<String, Object> res = tuge.addOrder(sn, order.getProductCode(), order.getOrderPeriod(), tugeStartDate(order.getStartDate()), order.getOrderNo());
        if (!TugeDeviceUtil.isOk(res)) {
            onTugeFail(order, "주문", String.valueOf(res.get("code")), res.get("msg"));
            return;
        }
        Map<?, ?> data = res.get("data") instanceof Map ? (Map<?, ?>) res.get("data") : new HashMap<>();
        order.setTugeOrderNo(str(data.get("orderNo")));
        Object flows = data.get("flowOrderNos");
        order.setTugeFlowOrderNos(flows instanceof Collection ? StringUtils.join((Collection<?>) flows, ",") : str(flows));
        order.setLastError(null);
        setTopup(order, "DONE", null);
        redisTemplate.delete(Arrays.asList("wifi:od:" + sn, "wifi:st:" + sn));
        wifiMapper.upsertDevice(sn, null, "ACTIVATED");
        earnPoint(order);

        WifiMemberDto member = wifiMapper.findMemberById(order.getMemberId());
        if (member != null && member.getTel() != null) {
            smsSender.talk(member.getTel(), WifiSmsSender.WF_TOPUP_DONE,
                params("deviceSn", smsSender.deviceLabel(sn), "productName", order.getProductName(), "payAmount", won(order.getPayKrw()),
                    "usedPoint", won(order.getPointUsed()), "earnPoint", won(order.getEarnPoint())),
                "[이심시티] 포켓와이파이 충전 완료: " + StringUtils.abbreviate(order.getProductName(), 18));
        }
    }

    /**
     * 충전 완료 시 적립: 실제 결제(PG) 금액 x 적립률, 10P 단위 버림. 포인트로 낸 금액은 제외.
     * 충전 완료 주문은 시스템 환불 경로가 없어(관리자가 PayUp 에서 직접 환불) 회수는 관리자 포인트 조정으로 처리
     */
    private void earnPoint(WifiOrderDto order) {
        int earn = WifiPricing.earnAmount(order.getPayKrw(), pricing.earnRate());
        if (earn <= 0 || wifiMapper.setOrderEarnPoint(order.getId(), earn) == 0) return; // 0 rows = 이미 적립됨
        wifiMapper.addPoint(order.getMemberId(), earn);
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.add(java.util.Calendar.DATE, pointValidDays);
        wifiMapper.extendPointExpire(order.getMemberId(), c.getTime());
        pointLog(order.getMemberId(), earn, "EARN", order.getId(), null, order.getProductName());
        order.setEarnPoint(earn);
    }

    private void onTugeFail(WifiOrderDto order, String step, String code, Object msg) {
        String err = step + " 실패 " + code + " " + (msg == null ? "" : msg);
        if ("1014".equals(code)) alertOnce("wifi:alert:balance", 30, "TUGE 채널 잔액 부족으로 충전 대기 중입니다. 잔액을 충전해주세요.");
        if (RETRYABLE.contains(code) && order.getRetryCount() + 1 < MAX_RETRY) {
            order.setRetryCount(order.getRetryCount() + 1);
            setTopup(order, "RETRY", err);
        } else {
            failAndRefund(order, err);
        }
    }

    @Override
    public void failAndRefund(WifiOrderDto order, String reason) {
        setTopup(order, "FAILED", reason);
        String ps = order.getPaymentStatus();
        if ("PAID".equals(ps)) {
            String cancel = payUpService.cancelPayment(order.getPgTransactionId());
            boolean ok = "SUCCESS".equals(cancel);
            wifiMapper.changePaymentStatus(order.getId(), "PAID", ok ? "REFUNDED" : "REFUND_FAILED", null);
            order.setPaymentStatus(ok ? "REFUNDED" : "REFUND_FAILED");
            if (!ok) smsSender.alertAdmin(WifiSmsSender.WF_ADM_CANCEL_FAIL, params("orderNo", order.getOrderNo()), "결제취소 실패 " + order.getOrderNo() + " 수동 환불 필요");
        } else if ("POINT_ONLY".equals(ps)) {
            wifiMapper.changePaymentStatus(order.getId(), "POINT_ONLY", "REFUNDED", null);
            order.setPaymentStatus("REFUNDED");
        } else {
            return; // 결제 전/결제 실패: 돌려줄 것 없음
        }
        if (order.getPointUsed() > 0) {
            wifiMapper.addPoint(order.getMemberId(), order.getPointUsed());
            pointLog(order.getMemberId(), order.getPointUsed(), "REFUND", order.getId(), null, "충전 실패 복구");
        }
        WifiMemberDto member = wifiMapper.findMemberById(order.getMemberId());
        if (member != null && member.getTel() != null) {
            smsSender.talk(member.getTel(), WifiSmsSender.WF_TOPUP_FAIL,
                params("deviceSn", smsSender.deviceLabel(order.getDeviceSn()), "productName", order.getProductName(),
                    "payAmount", won(order.getPayKrw()), "usedPoint", won(order.getPointUsed())),
                "[이심시티] 포켓와이파이 충전 실패로 결제가 취소되었습니다.");
        }
        smsSender.alertAdmin(WifiSmsSender.WF_ADM_TOPUP_FAIL, params("orderNo", order.getOrderNo(), "reason", StringUtils.abbreviate(reason, 100)),
            "충전 실패 " + order.getOrderNo() + " " + StringUtils.abbreviate(reason, 40));
    }

    @Override
    public Map<String, Object> orderResult(WifiMemberDto member, String orderNo) {
        WifiOrderDto o = wifiMapper.findOrderByNo(orderNo);
        if (o == null || !o.getMemberId().equals(member.getId())) throw new WifiException("주문을 찾을 수 없습니다.", ResponseCode.NOT_EXIST);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("orderNo", o.getOrderNo());
        r.put("deviceSn", o.getDeviceSn());
        r.put("productName", o.getProductName());
        r.put("totalKrw", o.getTotalKrw());
        r.put("pointUsed", o.getPointUsed());
        r.put("payKrw", o.getPayKrw());
        r.put("earnPoint", o.getEarnPoint());
        r.put("paymentStatus", o.getPaymentStatus());
        r.put("topupStatus", o.getTopupStatus());
        r.put("refundStatus", o.getRefundStatus());
        r.put("refundKrw", o.getRefundKrw());
        r.put("refundPoint", o.getRefundPoint());
        if ("REJECTED".equals(o.getRefundStatus())) r.put("refundMemo", o.getRefundMemo()); // 처리 메모는 내부용, 반려 사유만 고객에게
        r.put("registDt", o.getRegistDt());
        return r;
    }

    @Override
    public List<WifiOrderDto> myOrders(WifiMemberDto member) {
        return wifiMapper.myOrders(member.getId());
    }

    static final java.util.Set<String> DEVICE_LANGS = new java.util.HashSet<>(java.util.Arrays.asList("KR", "US", "JP", "CN", "TW", "VN"));

    @Override
    public Map<String, Object> deviceSettings(WifiMemberDto member, String sn) {
        WifiDeviceDto d = ownDevice(member, sn);
        Map<String, Object> p = currentProps(d.getSn(), false);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("ssid", p.get("ssid"));
        r.put("wifiPwd", p.get("wifiPwd"));
        r.put("deviceLang", p.get("deviceLang"));
        r.put("blackList", p.get("blackList"));
        r.put("tel", WifiAuthService.maskTel(member.getTel()));
        return r;
    }

    @Override
    public void updateDeviceSettings(WifiMemberDto member, String sn, String wifiPwd, String deviceLang, String blackList, String code) {
        WifiDeviceDto d = ownDevice(member, sn);
        String newPwd = StringUtils.trimToNull(wifiPwd);
        deviceLang = StringUtils.trimToNull(deviceLang);
        blackList = StringUtils.trimToNull(blackList);
        if (newPwd != null && !validWifiPwd(newPwd)) throw new WifiException("비밀번호는 공백 없이 영문·숫자·기호 8~16자로 입력해주세요.");
        if (deviceLang != null && !DEVICE_LANGS.contains(deviceLang)) throw new WifiException("화면 언어를 확인해주세요.");
        if (blackList != null && !"Y".equals(blackList) && !"N".equals(blackList)) throw new WifiException("데이터 절약 모드를 확인해주세요.");

        authService.verifyActionOtp(member, code); // 입력이 맞을 때만 인증번호 소진

        Map<String, Object> cur = currentProps(d.getSn(), true);
        String pwd = newPwd != null ? newPwd : StringUtils.trimToNull(str(cur.get("wifiPwd")));
        if (pwd == null) throw new WifiException("현재 비밀번호를 불러오지 못했습니다. 새 비밀번호를 입력해주세요.");
        String lang = deviceLang != null ? deviceLang : StringUtils.defaultIfBlank(str(cur.get("deviceLang")), "KR");
        String bl = blackList != null ? blackList : StringUtils.defaultIfBlank(str(cur.get("blackList")), "N");
        Map<String, Object> res = tuge.updateIndividualDeviceInfo(d.getSn(), pwd, lang, str(cur.get("cardLabel")), bl);
        if (!TugeDeviceUtil.isOk(res)) {
            log.warn("[WIFI] 장비 설정 변경 실패 sn={} code={} msg={}", d.getSn(), res.get("code"), res.get("msg"));
            throw new WifiException("장비 설정을 변경하지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
        redisTemplate.delete(Arrays.asList("wifi:props:" + d.getSn(), "wifi:props:" + d.getSn() + ":stale",
            "wifi:info:" + d.getSn(), "wifi:info:" + d.getSn() + ":stale"));
        log.info("[WIFI] 장비 설정 변경 sn={} member={} pwdChanged={}", d.getSn(), member.getId(), newPwd != null);
    }

    /** TUGE 3.15 는 8~16자. 장비 입력·표시 문제를 피하려고 공백 없는 ASCII 만 허용 */
    static boolean validWifiPwd(String p) {
        return p != null && p.matches("[\\x21-\\x7E]{8,16}");
    }

    /** 3.16 현재 속성. 이름·비밀번호가 비어 오면 3.23 장비 정보로 보충. 화면 표시는 캐시, 변경할 때는 fresh */
    private Map<String, Object> currentProps(String sn, boolean fresh) {
        Map<String, Object> res = fresh ? tuge.getIndividualDeviceInfo(sn)
            : cached("wifi:props:" + sn, 600, () -> tuge.getIndividualDeviceInfo(sn)).res;
        if (!TugeDeviceUtil.isOk(res)) {
            log.warn("[WIFI] 장비 속성 조회 실패 sn={} code={} msg={}", sn, res.get("code"), res.get("msg"));
            throw new WifiException("장비 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
        Map<String, Object> p = new HashMap<>();
        Object data = res.get("data");
        if (data instanceof Collection) {
            for (Object o : (Collection<?>) data) {
                if (o instanceof Map && sn.equalsIgnoreCase(String.valueOf(((Map<?, ?>) o).get("deviceNo")))) {
                    ((Map<?, ?>) o).forEach((k, v) -> p.put(String.valueOf(k), v));
                }
            }
        }
        if (StringUtils.isBlank(str(p.get("ssid"))) || StringUtils.isBlank(str(p.get("wifiPwd")))) {
            Map<String, Object> info = fresh ? tuge.getDeviceInfo(sn) : cached("wifi:info:" + sn, 600, () -> tuge.getDeviceInfo(sn)).res;
            if (TugeDeviceUtil.isOk(info) && info.get("data") instanceof Map) {
                Map<?, ?> i = (Map<?, ?>) info.get("data");
                if (StringUtils.isBlank(str(p.get("ssid")))) p.put("ssid", i.get("ssid"));
                if (StringUtils.isBlank(str(p.get("wifiPwd")))) p.put("wifiPwd", i.get("wifiPwd"));
            }
        }
        return p;
    }

    @Override
    public void requestRefund(WifiMemberDto member, String orderNo, String reason) {
        WifiOrderDto o = wifiMapper.findOrderByNo(orderNo);
        if (o == null || !o.getMemberId().equals(member.getId())) throw new WifiException("주문을 찾을 수 없습니다.", ResponseCode.NOT_EXIST);
        String r = StringUtils.abbreviate(StringUtils.trimToEmpty(reason), 500);
        if (r.isEmpty()) throw new WifiException("환불 사유를 입력해주세요.");
        if (wifiMapper.requestRefund(o.getId(), member.getId(), r) == 0) {
            throw new WifiException(o.getRefundStatus() != null ? "이미 환불 신청한 주문입니다." : "충전이 완료된 주문만 환불 신청할 수 있어요.");
        }
        smsSender.talk(member.getTel(), WifiSmsSender.WF_REFUND_REQ, params("orderNo", orderNo, "productName", o.getProductName()),
            "[이심시티] 포켓와이파이 환불 신청이 접수되었습니다.");
        smsSender.alertAdmin(WifiSmsSender.WF_ADM_REFUND_REQ, params("orderNo", orderNo, "reason", StringUtils.abbreviate(r, 100)), "포켓와이파이 환불 신청 " + orderNo);
    }

    @Override
    public List<WifiPointLogDto> myPointLogs(WifiMemberDto member) {
        return wifiMapper.pointLogList(member.getId());
    }

    /* ===================== 공통 ===================== */

    private WifiDeviceDto ownDevice(WifiMemberDto member, String sn) {
        WifiDeviceDto d = wifiMapper.findDevice(normalizeSn(sn));
        if (d == null || d.getMemberId() == null || !d.getMemberId().equals(member.getId())) {
            throw new WifiException("내 장비로 등록되지 않은 장비입니다.", ResponseCode.FORBIDDEN);
        }
        return d;
    }

    static String normalizeSn(String sn) {
        if (sn == null || !sn.trim().matches("[A-Za-z0-9]{6,32}")) throw new WifiException("장비번호를 확인해주세요.");
        return sn.trim().toUpperCase();
    }

    private String productLabel(WifiProductDto p, int period) {
        String name = StringUtils.defaultIfBlank(p.getDisplayName(), p.getTugeName());
        return p.isPeriodSelectable() ? name + " " + period + periodUnit(p) : name;
    }

    private static String periodUnit(WifiProductDto p) {
        return "MONTH".equals(p.getProductType()) ? "개월" : "일";
    }

    private String newOrderNo() {
        return ORDER_PREFIX + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + String.format("%06d", random.nextInt(1_000_000));
    }

    private void setTopup(WifiOrderDto order, String status, String err) {
        order.setTopupStatus(status);
        order.setLastError(err == null ? null : StringUtils.abbreviate(err, 1000));
        wifiMapper.updateOrderTopup(order);
    }

    private void pointLog(Long memberId, int amount, String reason, Long orderId, String adminId, String memo) {
        WifiPointLogDto pl = new WifiPointLogDto();
        pl.setMemberId(memberId);
        pl.setAmount(amount);
        pl.setReason(reason);
        pl.setOrderId(orderId);
        pl.setAdminId(adminId);
        pl.setMemo(StringUtils.abbreviate(memo, 500));
        wifiMapper.insertPointLog(pl);
    }

    private void alertOnce(String key, int minutes, String msg) {
        Boolean first = redisTemplate.opsForValue().setIfAbsent(key, "1", minutes, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(first)) smsSender.alertAdmin(WifiSmsSender.WF_ADM_BALANCE, params("balance", "부족(충전 대기)"), msg);
    }

    private static class Cached {
        Map<String, Object> res;
        boolean stale;
    }

    /**
     * Redis 캐시 (TUGE TPS 를 전체 고객이 공유하므로 필수). 성공 응답(+지정 업무코드)만 캐시,
     * 호출 실패 시 하루짜리 이전 값(stale)을 대신 반환
     */
    private Cached cached(String key, int ttlSec, Supplier<Map<String, Object>> loader, String... cacheableCodes) {
        Cached c = new Cached();
        Object hit = redisTemplate.opsForValue().get(key);
        if (hit != null) {
            c.res = gson.fromJson(hit.toString(), Map.class);
            return c;
        }
        Map<String, Object> res = loader.get();
        String code = String.valueOf(res.get("code"));
        if ("0000".equals(code) || Arrays.asList(cacheableCodes).contains(code)) {
            String json = gson.toJson(res);
            redisTemplate.opsForValue().set(key, json, ttlSec, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(key + ":stale", json, 1, TimeUnit.DAYS);
            c.res = res;
            return c;
        }
        Object old = redisTemplate.opsForValue().get(key + ":stale");
        if (old != null) {
            c.res = gson.fromJson(old.toString(), Map.class);
            c.stale = true;
        } else {
            c.res = res;
        }
        return c;
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    /** Gson 은 숫자를 Double 로 줌 → 정수로 */
    private static Object num(Object o) {
        if (o instanceof Number) return ((Number) o).intValue();
        if (o == null) return null;
        try {
            return (int) Double.parseDouble(o.toString());
        } catch (Exception e) {
            return o;
        }
    }
}
