package com.naver.naverspabackend.service.payup.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** PayUp 부분취소 응답: SUCCESS + responseCode 0000 + 금액 일치일 때만 성공 */
class PayUpPartCancelResultTest {

    private static Map<String, Object> res(String status, String code, String amount) {
        Map<String, Object> data = new HashMap<>();
        data.put("responseCode", code);
        data.put("cancelAmount", amount);
        data.put("partTransactionId", "P2");
        Map<String, Object> r = new HashMap<>();
        r.put("status", status);
        r.put("data", data);
        return r;
    }

    @Test
    void success() {
        assertEquals("P2", PayUpServiceImpl.partCancelResult(res("SUCCESS", "0000", "4000"), 4000));
    }

    @Test
    void sameAmountForPaymentCheck() {
        org.junit.jupiter.api.Assertions.assertTrue(PayUpServiceImpl.sameAmount("12000", "12000"));
        org.junit.jupiter.api.Assertions.assertTrue(PayUpServiceImpl.sameAmount("12000.0", "12000"));
        org.junit.jupiter.api.Assertions.assertFalse(PayUpServiceImpl.sameAmount("100", "12000"));
        org.junit.jupiter.api.Assertions.assertFalse(PayUpServiceImpl.sameAmount(null, "12000"));
        org.junit.jupiter.api.Assertions.assertFalse(PayUpServiceImpl.sameAmount("abc", "12000"));
    }

    @Test
    void failures() {
        assertNull(PayUpServiceImpl.partCancelResult(res("ERROR", "0000", "4000"), 4000));
        assertNull(PayUpServiceImpl.partCancelResult(res("SUCCESS", "9999", "4000"), 4000));
        assertNull(PayUpServiceImpl.partCancelResult(res("SUCCESS", "0000", "3000"), 4000));
        assertNull(PayUpServiceImpl.partCancelResult(null, 4000));
    }
}
