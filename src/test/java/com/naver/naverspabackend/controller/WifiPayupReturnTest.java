package com.naver.naverspabackend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.naver.naverspabackend.service.payup.PayUpService;
import com.naver.naverspabackend.service.wifi.WifiService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

/** PayUp 결제창에서 돌아올 때: 취소면 eSIM 결과 화면이 아니라 React 결과 화면으로 */
class WifiPayupReturnTest {

    private final WifiService wifi = mock(WifiService.class);
    private final PayUpService payUp = mock(PayUpService.class);
    private final ExternalController c = new ExternalController();

    WifiPayupReturnTest() {
        ReflectionTestUtils.setField(c, "wifiService", wifi);
        ReflectionTestUtils.setField(c, "payUpService", payUp);
        ReflectionTestUtils.setField(c, "wifiAppUrl", "https://x/wifi");
    }

    @Test
    void cancelWithoutOrderNumberGoesToWifiResult() {
        Map<String, String> data = new HashMap<>();
        data.put("wf", "WF1"); // 취소 시 PayUp 은 orderNumber/transactionId 없이 돌아옴
        assertEquals("redirect:https://x/wifi/result?orderNo=WF1", c.paypalSuccess(new ExtendedModelMap(), data, null));
        verify(wifi, never()).capturePayment(anyString(), anyString(), anyString());
        verify(payUp, never()).captureOrder(org.mockito.ArgumentMatchers.any(), anyString(), anyString(), anyString());
    }

    @Test
    void successCapturesWifiOrder() {
        when(wifi.capturePayment("T1", "WF1", "1000")).thenReturn("WF1");
        Map<String, String> data = new HashMap<>();
        data.put("wf", "WF1");
        data.put("orderNumber", "WF1");
        data.put("transactionId", "T1");
        data.put("amount", "1000");
        assertEquals("redirect:https://x/wifi/result?orderNo=WF1", c.paypalSuccess(new ExtendedModelMap(), data, null));
        verify(wifi).capturePayment("T1", "WF1", "1000");
    }
}
