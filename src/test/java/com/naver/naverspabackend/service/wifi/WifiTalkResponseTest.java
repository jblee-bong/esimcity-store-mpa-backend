package com.naver.naverspabackend.service.wifi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** NHN 알림톡 응답 판단 (실패면 SMS 로 대신 보내야 함) */
class WifiTalkResponseTest {
    @Test
    void 성공_실패_구분() throws Exception {
        assertTrue(WifiSmsSender.ok("{\"header\":{\"isSuccessful\":true,\"resultCode\":0},\"message\":{\"sendResults\":[{\"resultCode\":0}]}}"));
        assertFalse(WifiSmsSender.ok("{\"header\":{\"isSuccessful\":true,\"resultCode\":0},\"message\":{\"sendResults\":[{\"resultCode\":-3001}]}}"));
        assertFalse(WifiSmsSender.ok("{\"header\":{\"isSuccessful\":false,\"resultCode\":-1}}"));
    }

    @Test
    void 변수_만들기() {
        assertEquals("{orderNo=WF1, reason=}", WifiSmsSender.params("orderNo", "WF1", "reason", null).toString());
        assertEquals("22,400", WifiSmsSender.won(22400));
    }
}
