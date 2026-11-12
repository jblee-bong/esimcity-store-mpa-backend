package com.naver.naverspabackend.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import com.naver.naverspabackend.service.wifi.WifiService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class WifiPageControllerTest {

    @Test
    void payPageRedirectsToReactResultWhenOrderNotPayable() throws Exception {
        WifiService service = mock(WifiService.class);
        when(service.payPageModel(anyString())).thenReturn(null); // 결제 불가 주문 (Mockito 기본값은 빈 Map)
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new WifiPageController(service)).build();
        mvc.perform(get("/external/wifi/pay").param("orderNo", "WF1"))
            .andExpect(redirectedUrl("https://www.esimcity.co.kr/wifi/result?orderNo=WF1"));
    }
}
