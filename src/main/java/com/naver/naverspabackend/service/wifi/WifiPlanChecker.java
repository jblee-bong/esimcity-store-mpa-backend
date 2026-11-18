package com.naver.naverspabackend.service.wifi;

import com.naver.naverspabackend.dto.WifiMemberDto;
import com.naver.naverspabackend.dto.WifiOrderDto;
import com.naver.naverspabackend.mybatis.mapper.WifiMapper;
import com.naver.naverspabackend.util.TugeDeviceUtil;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 요금제 사용 기간·상태를 주문에 저장(관리자 충전 주문 화면용)하고, 내일 끝나는 요금제는 고객에게 알림(WF_EXPIRE_SOON).
 * - 날짜 지정 상품(ACTIVEDBYORDER, start_date 있음): 기간은 DB 로 확정 (고른 날 ~ +기간-1일, 한국시간 23:59 종료)
 * - 켤 때 시작 상품(ACTIVEDBYDEVICE): 사용 전 TUGE 날짜는 임시값이라 사용 중/종료일 때만 TUGE 날짜를 씀
 */
@Slf4j
@Component
public class WifiPlanChecker {

    static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private WifiMapper wifiMapper;
    @Autowired
    private TugeDeviceUtil tuge;
    @Autowired
    private WifiSmsSender smsSender;

    /** notify=true 면 내일 끝나는 요금제 알림까지. 반환: 알림 보낸 건수 */
    public int run(boolean notify) {
        Map<String, List<WifiOrderDto>> byDevice = new LinkedHashMap<>();
        for (WifiOrderDto o : wifiMapper.planCheckTargets()) byDevice.computeIfAbsent(o.getDeviceSn(), k -> new ArrayList<>()).add(o);
        int sent = 0;
        LocalDate today = LocalDate.now(KST);
        for (Map.Entry<String, List<WifiOrderDto>> e : byDevice.entrySet()) {
            Map<String, Map<?, ?>> tugePlans = tugePlans(e.getKey());
            for (WifiOrderDto o : e.getValue()) {
                fill(o, tugePlans.get(o.getTugeOrderNo()), tugePlans.isEmpty());
                wifiMapper.updatePlan(o.getId(), o.getPlanStatus(), o.getPlanStartDt(), o.getPlanEndDt());
            }
            if (!notify) continue;
            for (WifiOrderDto o : e.getValue()) {
                if (o.getExpireNotifiedDt() == null && endsTomorrow(o, today) && !continuesAfter(o, e.getValue())
                        && wifiMapper.markExpireNotified(o.getId()) > 0) {
                    WifiMemberDto m = wifiMapper.findMemberById(o.getMemberId());
                    if (m != null && m.getTel() != null) {
                        smsSender.talk(m.getTel(), WifiSmsSender.WF_EXPIRE_SOON,
                            WifiSmsSender.params("deviceSn", smsSender.deviceLabel(o.getDeviceSn()), "productName", o.getProductName(), "endDate", o.getPlanEndDt()),
                            "[이심시티] 포켓와이파이 요금제가 내일(" + o.getPlanEndDt().substring(5, 10).replace('-', '/') + ") 23:59(한국시간)에 끝나요.");
                        sent++;
                    }
                }
            }
        }
        log.info("[WIFI] 요금제 확인 devices={} notified={}", byDevice.size(), sent);
        return sent;
    }

    /** 장비의 TUGE 요금제 (TUGE 주문번호 → 요금제). 실패하면 빈 Map */
    private Map<String, Map<?, ?>> tugePlans(String sn) {
        Map<String, Map<?, ?>> r = new HashMap<>();
        Map<String, Object> res = tuge.queryOrderList(sn);
        if (!TugeDeviceUtil.isOk(res) || !(res.get("data") instanceof Collection)) return r;
        for (Object x : (Collection<?>) res.get("data")) {
            if (x instanceof Map && ((Map<?, ?>) x).get("orderNo") != null) r.put(String.valueOf(((Map<?, ?>) x).get("orderNo")), (Map<?, ?>) x);
        }
        return r;
    }

    /** 주문에 상태·기간(KST "yyyy-MM-dd HH:mm") 채우기 */
    static void fill(WifiOrderDto o, Map<?, ?> t, boolean tugeFailed) {
        if (t != null) o.setPlanStatus(String.valueOf(t.get("status")));
        else if (!tugeFailed && o.getPlanStatus() != null) o.setPlanStatus("GONE"); // TUGE 목록에서 사라짐(취소·정리)
        if (o.getStartDate() != null) {                                              // 날짜 지정: DB 로 확정
            LocalDate s = LocalDate.parse(o.getStartDate().substring(0, 10));
            o.setPlanStartDt(s.atStartOfDay().format(TS));
            o.setPlanEndDt(s.plusDays(Math.max(1, o.getOrderPeriod()) - 1).atTime(23, 59).format(TS));
        } else if (t != null && !"NOTACTIVE".equals(o.getPlanStatus())) {            // 켤 때 시작: 사용 중/종료만 실제 날짜
            o.setPlanStartDt(TugeDeviceUtil.utcToKst(t.get("startDate")));
            o.setPlanEndDt(TugeDeviceUtil.utcToKst(t.get("endDate")));
        } else if (t != null) {
            o.setPlanStartDt(null);
            o.setPlanEndDt(null);
        }
    }

    static boolean endsTomorrow(WifiOrderDto o, LocalDate today) {
        if (o.getPlanEndDt() == null || "GONE".equals(o.getPlanStatus())) return false;
        return o.getPlanEndDt().startsWith(today.plusDays(1).toString());
    }

    /** 같은 장비에 이어서 쓸 요금제가 있으면 알림 안 보냄 (켤 때 시작 상품 대기 중, 또는 이 요금제 뒤에 시작하는 날짜 지정 상품) */
    static boolean continuesAfter(WifiOrderDto o, List<WifiOrderDto> sameDevice) {
        for (WifiOrderDto p : sameDevice) {
            if (p == o || "GONE".equals(p.getPlanStatus()) || "EXPIRED".equals(p.getPlanStatus())) continue;
            if (p.getStartDate() == null && "NOTACTIVE".equals(p.getPlanStatus())) return true;
            if (p.getStartDate() != null && p.getPlanStartDt() != null && p.getPlanStartDt().compareTo(o.getPlanEndDt()) > 0) return true;
        }
        return false;
    }
}
