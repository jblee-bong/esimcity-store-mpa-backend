package com.naver.naverspabackend.service.wifi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.naver.naverspabackend.dto.WifiProductDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class WifiPricingTest {

    private static WifiProductDto product(String type, int days, String usd) {
        WifiProductDto p = new WifiProductDto();
        p.setProductType(type);
        p.setDays(days);
        p.setSettlementUsd(new BigDecimal(usd));
        return p;
    }

    // 1달러 1,600원, 기본 30%, 7일~ 25%, 30일~ 15%
    private final WifiPricing.Policy policy = new WifiPricing.Policy(1600, 30,
        WifiPricing.parseRules("[{\"from\":7,\"rate\":25},{\"from\":30,\"rate\":15}]"));

    @Test
    void dailyUsesPeriodTiersAndRoundsUpTo100() {
        int[] t = WifiPricing.priceTable(product("DAILY", 1, "1.20"), policy, 30);
        assertEquals(2500, t[0]);   // 1.2*1600*1.30 = 2496 → 2500
        assertEquals(12500, t[4]);  // 5일: 12480 → 12500
        assertEquals(16800, t[6]);  // 7일 25%: 16800
        for (int i = 1; i < t.length; i++) assertTrue(t[i] >= t[i - 1], "기간이 길수록 총액이 줄면 안 됨: " + (i + 1) + "일");
        assertEquals(t[28], t[29]); // 30일(15%)=66300 < 29일 → 29일 가격으로 보정
    }

    @Test
    void fixedAndMonthlyUseBaseMarginOnly() {
        assertEquals(1, WifiPricing.priceTable(product("MULTIPLEMONTHS", 30, "10.00"), policy, 1).length);
        assertEquals(20800, WifiPricing.priceTable(product("MULTIPLEMONTHS", 30, "10.00"), policy, 1)[0]); // 16000*1.3
        int[] month = WifiPricing.priceTable(product("MONTH", 1, "2.00"), policy, 3);
        assertEquals(4200, month[0]);  // 3200*1.3 = 4160 → 4200
        assertEquals(12500, month[2]); // 3개월도 30% (월간은 일수 구간 미적용): 12480 → 12500
    }

    @Test
    void earnIsPercentOfPaidAmountFlooredTo10() {
        assertEquals(370, WifiPricing.earnAmount(12500, 3));  // 375 → 370
        assertEquals(0, WifiPricing.earnAmount(0, 3));        // 포인트 전액 결제는 적립 없음
        assertEquals(0, WifiPricing.earnAmount(10000, 0));
        assertEquals(500, WifiPricing.earnAmount(10000, 5));
    }

    @Test
    void badRulesAreIgnored() {
        assertEquals(0, WifiPricing.parseRules("oops").size());
        assertEquals(2, WifiPricing.parseRules("[{\"from\":1,\"rate\":5},{\"from\":7,\"rate\":25}]").size()); // from 1 = 데일리 전용 마진
    }
}
