package com.naver.naverspabackend.service.wifi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.naver.naverspabackend.dto.WifiOrderDto;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 요금제 기간 계산 + 내일 끝나는 요금제 알림 판단 */
class WifiPlanCheckerTest {

    private final LocalDate today = LocalDate.of(2026, 11, 26);

    private WifiOrderDto order(String startDate, int period) {
        WifiOrderDto o = new WifiOrderDto();
        o.setStartDate(startDate);
        o.setOrderPeriod(period);
        return o;
    }

    private Map<String, Object> tuge(String status, String startUtc, String endUtc) {
        Map<String, Object> m = new HashMap<>();
        m.put("status", status);
        m.put("startDate", startUtc);
        m.put("endDate", endUtc);
        return m;
    }

    @Test
    void 날짜지정_상품은_DB로_기간_확정_상태와_무관() {
        WifiOrderDto o = order("2026-11-26", 2);
        WifiPlanChecker.fill(o, tuge("NOTACTIVE", "x", "y"), false);
        assertEquals("2026-11-26 00:00", o.getPlanStartDt());
        assertEquals("2026-11-27 23:59", o.getPlanEndDt());
        assertTrue(WifiPlanChecker.endsTomorrow(o, today));
    }

    @Test
    void 켤때시작_상품은_사용전_날짜를_쓰지_않음() {
        WifiOrderDto o = order(null, 1);
        WifiPlanChecker.fill(o, tuge("NOTACTIVE", "2026-11-25 15:00:00", "2026-11-30 14:59:59"), false);
        assertNull(o.getPlanEndDt());
        assertFalse(WifiPlanChecker.endsTomorrow(o, today));

        WifiPlanChecker.fill(o, tuge("INUSE", "2026-11-25 15:00:00", "2026-11-27 14:59:59"), false);
        assertEquals("2026-11-27 23:59", o.getPlanEndDt());   // GMT 14:59 = 한국 23:59
        assertTrue(WifiPlanChecker.endsTomorrow(o, today));
    }

    @Test
    void 이어서_쓸_요금제가_있으면_알림_안보냄() {
        WifiOrderDto cur = order(null, 1);
        WifiPlanChecker.fill(cur, tuge("INUSE", "2026-11-25 15:00:00", "2026-11-27 14:59:59"), false);
        WifiOrderDto queued = order(null, 1);
        WifiPlanChecker.fill(queued, tuge("NOTACTIVE", "2026-11-25 15:00:00", "2026-11-30 14:59:59"), false);
        assertTrue(WifiPlanChecker.continuesAfter(cur, Arrays.asList(cur, queued)));

        WifiOrderDto next = order("2026-11-28", 3);
        WifiPlanChecker.fill(next, null, false);
        assertTrue(WifiPlanChecker.continuesAfter(cur, Arrays.asList(cur, next)));
        assertFalse(WifiPlanChecker.continuesAfter(cur, Arrays.asList(cur)));
    }

    @Test
    void TUGE_목록에서_사라지면_취소로_보고_알림_안보냄() {
        WifiOrderDto o = order("2026-11-27", 1);
        o.setPlanStatus("NOTACTIVE");
        WifiPlanChecker.fill(o, null, false);
        assertEquals("GONE", o.getPlanStatus());
        assertFalse(WifiPlanChecker.endsTomorrow(o, today));
    }
}
