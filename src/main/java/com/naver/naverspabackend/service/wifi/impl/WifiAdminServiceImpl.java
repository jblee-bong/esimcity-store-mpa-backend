package com.naver.naverspabackend.service.wifi.impl;

import com.google.gson.Gson;
import com.naver.naverspabackend.dto.CodeDto;
import com.naver.naverspabackend.dto.WifiCategoryDto;
import com.naver.naverspabackend.dto.WifiDeviceDto;
import com.naver.naverspabackend.dto.WifiMemberDto;
import com.naver.naverspabackend.dto.WifiOrderDto;
import com.naver.naverspabackend.dto.WifiPointLogDto;
import com.naver.naverspabackend.dto.WifiProductDto;
import com.naver.naverspabackend.exception.WifiException;
import com.naver.naverspabackend.mybatis.mapper.CodeMapper;
import com.naver.naverspabackend.mybatis.mapper.WifiMapper;
import com.naver.naverspabackend.service.payup.PayUpService;
import com.naver.naverspabackend.service.wifi.WifiAdminService;
import com.naver.naverspabackend.service.wifi.WifiAuthService;
import com.naver.naverspabackend.service.wifi.WifiPricing;
import com.naver.naverspabackend.service.wifi.WifiService;
import com.naver.naverspabackend.util.TugeDeviceUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class WifiAdminServiceImpl implements WifiAdminService {

    private static final double DEFAULT_RATE = 1450;

    @Autowired
    private WifiMapper wifiMapper;
    @Autowired
    private CodeMapper codeMapper;
    @Autowired
    private TugeDeviceUtil tuge;
    @Autowired
    private WifiPricing pricing;
    @Autowired
    private WifiService wifiService;
    @Autowired
    private PayUpService payUpService;

    @Value("${wifi.pointValidDays:365}")
    private int pointValidDays;
    @Value("${wifi.appUrl:https://www.esimcity.co.kr/wifi}")
    private String appUrl;

    private final Gson gson = new Gson();

    @Override
    public double exchangeRate() {
        Map<String, Object> q = new HashMap<>();
        q.put("codeGroup", "WIFI");
        q.put("codeCd", "RATE");
        CodeDto c = codeMapper.fetch(q);
        try {
            return c == null ? DEFAULT_RATE : Double.parseDouble(c.getCodeValue());
        } catch (Exception e) {
            return DEFAULT_RATE;
        }
    }

    /* ===================== 상품 ===================== */

    @Override
    public Map<String, Object> productList(Map<String, Object> param) {
        paging(param);
        List<WifiProductDto> list = wifiMapper.adminProductList(param);
        Map<Long, WifiCategoryDto> cats = pricing.categories();
        for (WifiProductDto p : list) pricing.fillUnitPrice(p, cats); // 나라 정책으로 계산한 1일(고정) 판매가
        Map<String, Object> r = new HashMap<>();
        r.put("list", list);
        r.put("total", wifiMapper.adminProductCount(param));
        r.put("rate", exchangeRate());
        return r;
    }

    @Override
    public Map<String, Object> syncProducts() {
        if (!tuge.isConfigured()) throw new WifiException("TUGE 디바이스 API 설정(api.tugeDevice)이 없습니다.");
        List<String> codes = new ArrayList<>();
        int page = 1;
        int total = Integer.MAX_VALUE;
        while (codes.size() < total && page <= 100) {
            Map<String, Object> res = tuge.getProductList(page, 100);
            if (!TugeDeviceUtil.isOk(res)) {
                throw new WifiException("TUGE 상품 조회 실패 (" + res.get("code") + " " + res.get("msg") + "). 기존 상품은 그대로입니다.");
            }
            if (res.get("total") instanceof Number) total = ((Number) res.get("total")).intValue();
            if (!(res.get("data") instanceof Collection) || ((Collection<?>) res.get("data")).isEmpty()) break;
            for (Object o : (Collection<?>) res.get("data")) {
                if (!(o instanceof Map)) continue;
                WifiProductDto p = fromTuge((Map<?, ?>) o);
                if (p.getProductCode() == null) continue;
                wifiMapper.upsertProductFromTuge(p);
                codes.add(p.getProductCode());
            }
            page++;
        }
        int inactive = codes.isEmpty() ? 0 : wifiMapper.markMissingProductsInactive(codes);
        Map<String, Object> r = new HashMap<>();
        r.put("synced", codes.size());
        r.put("inactive", inactive);
        log.info("[WIFI] 상품 동기화 synced={} inactive={}", codes.size(), inactive);
        return r;
    }

    private WifiProductDto fromTuge(Map<?, ?> m) {
        WifiProductDto p = new WifiProductDto();
        p.setProductCode(str(m.get("productCode")));
        p.setTugeName(str(m.get("productName")));
        p.setProductType(str(m.get("productType")));
        p.setDays(intOrNull(m.get("days")));
        p.setValidityPeriod(intOrNull(m.get("validityPeriod")));
        p.setDataLimited(str(m.get("dataLimited")));
        Integer dataTotal = intOrNull(m.get("dataTotal"));
        p.setDataTotal(dataTotal == null ? str(m.get("dataTotal")) : String.valueOf(dataTotal));
        p.setDataUnit(str(m.get("dataUnit")));
        p.setMcc(StringUtils.abbreviate(str(m.get("mcc")), 1000));
        p.setDeviceType(str(m.get("deviceType")));
        p.setSettlementUsd(m.get("settlementPrice") == null ? null : new BigDecimal(m.get("settlementPrice").toString()));
        p.setPopupJson(m.get("popupList") == null ? null : gson.toJson(m.get("popupList")));
        p.setActiveType(str(m.get("activeType")));
        p.setRuleDesc(str(m.get("ruleDesc")));
        p.setTugeModifiedDt(str(m.get("lastModifiedTime")));
        return p;
    }

    @Override
    public WifiProductDto productDetail(String productCode) {
        WifiProductDto p = wifiMapper.findProduct(productCode);
        if (p == null) throw new WifiException("상품이 없습니다.");
        pricing.fillPrices(p); // 화면에서 1일/7일/30일 예시 가격 표시
        return p;
    }

    @Override
    @Transactional
    public void updateProduct(WifiProductDto dto) {
        WifiProductDto p = wifiMapper.findProduct(dto.getProductCode());
        if (p == null) throw new WifiException("상품이 없습니다.");

        // 기간: 1일 데일리/1개월 월간만 고객이 일(월)수 선택, 나머지는 1 고정 (TUGE 3.4 orderPeriod 규칙)
        if (p.isPeriodSelectable()) {
            if (dto.getMinPeriod() < 1 || dto.getMaxPeriod() < dto.getMinPeriod() || dto.getMaxPeriod() > 365) {
                throw new WifiException("최소/최대 기간을 확인해주세요 (1 ≤ 최소 ≤ 최대 ≤ 365).");
            }
        } else {
            dto.setMinPeriod(1);
            dto.setMaxPeriod(1);
        }
        if (dto.getCategoryId() != null && wifiMapper.findCategory(dto.getCategoryId()) == null) throw new WifiException("나라를 다시 선택해주세요.");

        if ("Y".equals(dto.getSaleYn())) {
            if (!"Y".equals(p.getTugeActive())) throw new WifiException("TUGE 에서 판매중지된 상품은 판매할 수 없습니다.");
            if (StringUtils.isBlank(dto.getDisplayName())) throw new WifiException("표시명을 입력해주세요.");
            if (dto.getCategoryId() == null) throw new WifiException("나라를 선택해주세요. (판매가는 나라 가격정책으로 계산됩니다)");
            if (p.getSettlementUsd() == null) throw new WifiException("TUGE 원가가 없는 상품은 판매할 수 없습니다.");
        }
        dto.setSaleYn("Y".equals(dto.getSaleYn()) ? "Y" : "N");
        wifiMapper.updateProductSale(dto);
    }

    /* ===================== 카테고리 ===================== */

    @Override
    public List<WifiCategoryDto> categoryList() {
        return wifiMapper.adminCategoryList();
    }

    @Override
    public WifiCategoryDto saveCategory(WifiCategoryDto dto) {
        if (StringUtils.isBlank(dto.getName())) throw new WifiException("카테고리명을 입력해주세요.");
        if (StringUtils.isBlank(dto.getUseYn())) dto.setUseYn("Y");
        if (dto.getUsdRate() != null && dto.getUsdRate() <= 0) dto.setUsdRate(null);
        if (dto.getMarginRate() != null && (dto.getMarginRate() < 0 || dto.getMarginRate() > 500)) {
            throw new WifiException("마진율은 0~500% 사이로 입력해주세요.");
        }
        if (StringUtils.isNotBlank(dto.getPeriodRules())) {
            // 화면에서 [{from, rate}] JSON 으로 보냄 → 정리해서 다시 저장 (잘못된 행 제거, 일수 순 정렬)
            java.util.TreeMap<Integer, Double> rules = WifiPricing.parseRules(dto.getPeriodRules());
            List<Map<String, Object>> clean = new ArrayList<>();
            for (Map.Entry<Integer, Double> e : rules.entrySet()) {
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("from", e.getKey());
                m.put("rate", e.getValue());
                clean.add(m);
            }
            dto.setPeriodRules(clean.isEmpty() ? null : gson.toJson(clean));
        }
        // 2단계까지만: 상위는 지역(상위가 없는 카테고리)이어야 하고, 하위가 있는 지역은 나라로 바꿀 수 없음
        if (dto.getParentId() != null) {
            WifiCategoryDto parent = wifiMapper.findCategory(dto.getParentId());
            if (parent == null || parent.getParentId() != null || parent.getId().equals(dto.getId())) {
                throw new WifiException("상위 카테고리는 지역(1단계)만 선택할 수 있습니다.");
            }
            if (dto.getId() != null && wifiMapper.countChildCategories(dto.getId()) > 0) {
                throw new WifiException("하위 나라가 있는 지역은 다른 지역 아래로 옮길 수 없습니다.");
            }
        }
        if (dto.getId() == null) wifiMapper.insertCategory(dto);
        else wifiMapper.updateCategory(dto);
        return dto;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (wifiMapper.countChildCategories(id) > 0) throw new WifiException("하위 나라를 먼저 삭제하거나 다른 지역으로 옮겨주세요.");
        wifiMapper.deleteCategoryMappings(id);
        wifiMapper.deleteCategory(id);
    }

    /* ===================== 장비 ===================== */

    @Override
    public Map<String, Object> deviceList(Map<String, Object> param) {
        paging(param);
        List<WifiDeviceDto> list = wifiMapper.adminDeviceList(param);
        for (WifiDeviceDto d : list) d.setQrUrl(appUrl + "/?d=" + d.getSn()); // "/wifi/?d=" (슬래시 포함)
        Map<String, Object> r = new HashMap<>();
        r.put("list", list);
        r.put("total", wifiMapper.adminDeviceCount(param));
        return r;
    }

    @Override
    public Map<String, Object> syncDevices() {
        if (!tuge.isConfigured()) throw new WifiException("TUGE 디바이스 API 설정(api.tugeDevice)이 없습니다.");
        int count = 0;
        int page = 1;
        int total = Integer.MAX_VALUE;
        while (count < total && page <= 200) {
            Map<String, Object> res = tuge.getDeviceList(page, 100);
            if (!TugeDeviceUtil.isOk(res)) throw new WifiException("TUGE 장비 조회 실패 (" + res.get("code") + ")");
            if (res.get("total") instanceof Number) total = ((Number) res.get("total")).intValue();
            if (!(res.get("data") instanceof Collection) || ((Collection<?>) res.get("data")).isEmpty()) break;
            for (Object o : (Collection<?>) res.get("data")) {
                if (!(o instanceof Map)) continue;
                Map<?, ?> m = (Map<?, ?>) o;
                String sn = str(m.get("deviceNO"));
                if (sn == null) continue;
                wifiMapper.upsertDevice(sn.trim().toUpperCase(), str(m.get("deviceType")), str(m.get("deviceStatus")));
                count++;
            }
            page++;
        }
        Map<String, Object> r = new HashMap<>();
        r.put("synced", count);
        return r;
    }

    @Override
    public void releaseDevice(String sn, String adminId) {
        WifiDeviceDto d = wifiMapper.findDevice(sn);
        if (d == null || d.getMemberId() == null) throw new WifiException("등록된 장비가 아닙니다.");
        wifiMapper.releaseDevice(sn, null);
        wifiMapper.insertDeviceLog(sn, d.getMemberId(), "ADMIN_RELEASE", adminId);
    }

    /* ===================== 회원 / 포인트 ===================== */

    @Override
    public Map<String, Object> memberList(Map<String, Object> param) {
        paging(param);
        Map<String, Object> r = new HashMap<>();
        r.put("list", wifiMapper.adminMemberList(param));
        r.put("total", wifiMapper.adminMemberCount(param));
        return r;
    }

    @Override
    public List<WifiPointLogDto> pointLogs(Long memberId) {
        return wifiMapper.pointLogList(memberId);
    }

    @Override
    @Transactional
    public void adjustPoint(Long memberId, int amount, String memo, String adminId) {
        WifiMemberDto m = wifiMapper.findMemberById(memberId);
        if (m == null || "DELETED".equals(m.getStatus())) throw new WifiException("회원이 없습니다.");
        if (amount == 0) throw new WifiException("금액을 입력해주세요.");
        if (amount < 0) {
            if (wifiMapper.usePoint(memberId, -amount) == 0) throw new WifiException("차감할 포인트가 잔액보다 많습니다.");
        } else {
            wifiMapper.addPoint(memberId, amount);
            wifiMapper.extendPointExpire(memberId, expireDate());
        }
        log(memberId, amount, "ADMIN", adminId, memo);
    }

    @Override
    @Transactional
    public Map<String, Object> grantPoints(List<Map<String, Object>> rows, String memo, String adminId) {
        int granted = 0;
        int created = 0;
        List<String> skipped = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String tel = WifiAuthService.normalizeTel(str(row.get("tel")));
            Integer point = intOrNull(row.get("point"));
            if (!tel.matches("01[016789][0-9]{7,8}") || point == null || point <= 0) {
                skipped.add(row.get("tel") + "/" + row.get("point"));
                continue;
            }
            WifiMemberDto m = wifiMapper.findMemberByTel(tel);
            if (m == null) {
                m = new WifiMemberDto();
                m.setTel(tel);
                m.setStatus("PRE");
                wifiMapper.insertMember(m);
                created++;
            }
            wifiMapper.addPoint(m.getId(), point);
            wifiMapper.extendPointExpire(m.getId(), expireDate());
            log(m.getId(), point, "GRANT", adminId, memo);
            granted++;
        }
        Map<String, Object> r = new HashMap<>();
        r.put("granted", granted);
        r.put("created", created);
        r.put("skipped", skipped);
        return r;
    }

    @Override
    public void expirePoints() {
        for (WifiMemberDto m : wifiMapper.selectExpiredPointMembers()) {
            log(m.getId(), -m.getPointBalance(), "EXPIRE", null, "유효기간 만료");
            wifiMapper.expirePoint(m.getId());
        }
    }

    // shortcut: 회원당 소멸일 하나 (추가 지급 시 늦은 날짜로 연장). 지급 건별 소멸이 필요하면 이력 기준 FIFO 로 바꿔야 함
    private java.util.Date expireDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, pointValidDays);
        return c.getTime();
    }

    private void log(Long memberId, int amount, String reason, String adminId, String memo) {
        WifiPointLogDto pl = new WifiPointLogDto();
        pl.setMemberId(memberId);
        pl.setAmount(amount);
        pl.setReason(reason);
        pl.setAdminId(adminId);
        pl.setMemo(StringUtils.abbreviate(memo, 500));
        wifiMapper.insertPointLog(pl);
    }

    /* ===================== 주문 ===================== */

    @Override
    public Map<String, Object> orderList(Map<String, Object> param) {
        paging(param);
        Map<String, Object> r = new HashMap<>();
        r.put("list", wifiMapper.adminOrderList(param));
        r.put("total", wifiMapper.adminOrderCount(param));
        return r;
    }

    @Override
    public void retryOrder(String orderNo) {
        WifiOrderDto o = wifiMapper.findOrderByNo(orderNo);
        if (o == null) throw new WifiException("주문이 없습니다.");
        boolean paid = "PAID".equals(o.getPaymentStatus()) || "POINT_ONLY".equals(o.getPaymentStatus());
        if (!paid || !("RETRY".equals(o.getTopupStatus()) || "READY".equals(o.getTopupStatus()))) {
            throw new WifiException("결제 완료 + 충전 대기/재시도 상태인 주문만 재처리할 수 있습니다.");
        }
        o.setRetryCount(0);
        wifiService.fulfill(o);
    }

    @Override
    public void refundOrder(String orderNo, String adminId) {
        WifiOrderDto o = wifiMapper.findOrderByNo(orderNo);
        if (o == null) throw new WifiException("주문이 없습니다.");
        if ("REFUND_FAILED".equals(o.getPaymentStatus())) {
            // 포인트는 이미 복구됨 → PG 취소만 다시
            String r = payUpService.cancelPayment(o.getPgTransactionId());
            if (!"SUCCESS".equals(r)) throw new WifiException("PG 취소 실패 (" + r + "). PayUp 관리자에서 확인해주세요.");
            wifiMapper.changePaymentStatus(o.getId(), "REFUND_FAILED", "REFUNDED", null);
            return;
        }
        if ("DONE".equals(o.getTopupStatus())) {
            throw new WifiException("이미 충전된 주문입니다. TUGE 주문 취소 후 PayUp 에서 직접 환불해주세요.");
        }
        if (!"PAID".equals(o.getPaymentStatus()) && !"POINT_ONLY".equals(o.getPaymentStatus())) {
            throw new WifiException("환불할 결제가 없습니다.");
        }
        wifiService.failAndRefund(o, "관리자 환불 (" + adminId + ")");
    }

    @Override
    public Map<String, Object> balance() {
        Map<String, Object> res = tuge.getBalance();
        if (!TugeDeviceUtil.isOk(res)) throw new WifiException("잔액 조회 실패 (" + res.get("code") + " " + res.get("msg") + ")");
        Object d = res.get("data");
        return d instanceof Map ? (Map<String, Object>) d : new HashMap<>();
    }

    /* ===================== 공통 ===================== */

    private static void paging(Map<String, Object> param) {
        int page = Math.max(1, intOrDefault(param.get("pageNo"), 1));
        int size = Math.min(500, Math.max(1, intOrDefault(param.get("size"), 20)));
        param.put("offset", (page - 1) * size);
        param.put("size", size);
    }

    private static int intOrDefault(Object o, int def) {
        Integer i = intOrNull(o);
        return i == null ? def : i;
    }

    private static Integer intOrNull(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return (int) Double.parseDouble(o.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }
}
