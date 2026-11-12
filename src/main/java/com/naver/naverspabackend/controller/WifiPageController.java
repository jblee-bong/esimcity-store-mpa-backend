package com.naver.naverspabackend.controller;

import com.naver.naverspabackend.service.wifi.WifiService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** PayUp 결제창 호출용 Thymeleaf 페이지. React 앱(pm2) 에서 location.href 로 들어옴 */
@Controller
@RequiredArgsConstructor
public class WifiPageController {

    private final WifiService wifiService;

    /** React 앱 주소 (운영 https://www.esimcity.co.kr/wifi) */
    @Value("${wifi.appUrl:https://www.esimcity.co.kr/wifi}")
    private String appUrl = "https://www.esimcity.co.kr/wifi";

    @GetMapping("/external/wifi/pay")
    public String pay(@RequestParam String orderNo, Model model) {
        Map<String, Object> m = wifiService.payPageModel(orderNo);
        if (m == null) return "redirect:" + appUrl + "/result?orderNo=" + orderNo; // 이미 결제됐거나 없는 주문
        model.addAllAttributes(m);
        // 결제 취소 시 PayUp 이 orderNumber 없이 돌아오므로, 우리 주문번호를 돌아올 주소에 실어 보냄
        String back = String.valueOf(m.get("redirectUrl"));
        model.addAttribute("redirectUrl", back + (back.contains("?") ? "&" : "?") + "wf=" + orderNo);
        return "wifi/pay";
    }
}
