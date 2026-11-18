package com.naver.naverspabackend.service.wifi.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.naver.naverspabackend.dto.WifiProductDto;
import com.naver.naverspabackend.exception.WifiException;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

/** 날짜 지정 상품(ACTIVEDBYORDER) 사용 시작일 검사·TUGE startDate 변환 */
class WifiStartDateTest {

    private final LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

    private WifiProductDto product(Integer validityPeriod) {
        WifiProductDto p = new WifiProductDto();
        p.setActiveType("ACTIVEDBYORDER");
        p.setValidityPeriod(validityPeriod);
        return p;
    }

    @Test
    void 비우면_오늘_범위안은_그대로() {
        assertEquals(today.toString(), WifiServiceImpl.checkStartDate(product(30), null));
        assertEquals(today.plusDays(10).toString(), WifiServiceImpl.checkStartDate(product(1), today.plusDays(10).toString())); // validityPeriod=1 이어도 미래 날짜 가능
        assertEquals(today.plusDays(59).toString(), WifiServiceImpl.checkStartDate(product(30), today.plusDays(59).toString()));
    }

    @Test
    void 지난날_기한넘김_형식오류는_거절() {
        assertThrows(WifiException.class, () -> WifiServiceImpl.checkStartDate(product(30), today.minusDays(1).toString()));
        assertThrows(WifiException.class, () -> WifiServiceImpl.checkStartDate(product(30), today.plusDays(60).toString()));
        assertThrows(WifiException.class, () -> WifiServiceImpl.checkStartDate(product(30), "2026/11/20"));
    }

    @Test
    void TUGE_startDate_변환() {
        // 고른 날 00:00 GMT (= 한국 09:00, TUGE 가 같은 한국 날짜로 잡음)
        assertEquals(today.plusDays(3) + " 00:00:00", WifiServiceImpl.tugeStartDate(today.plusDays(3).toString()));
        // 재시도가 늦어 시작일이 지났으면 오늘
        assertEquals(today + " 00:00:00", WifiServiceImpl.tugeStartDate(today.minusDays(2).toString()));
        // 날짜 지정 상품이 아니면 지금 시각
        assertTrue(WifiServiceImpl.tugeStartDate(null).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
}
