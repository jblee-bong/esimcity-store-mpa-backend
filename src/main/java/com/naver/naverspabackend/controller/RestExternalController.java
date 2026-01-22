package com.naver.naverspabackend.controller;

import com.naver.naverspabackend.service.order.OrderService;
import com.naver.naverspabackend.service.papal.PaypalService;
import com.naver.naverspabackend.service.payapp.PayAppService;
import com.naver.naverspabackend.service.portone.PortOneService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/external")
@RequiredArgsConstructor
public class RestExternalController {
    @Autowired
    private PaypalService paypalService;

    @Autowired
    private PortOneService portOneService;
    @Autowired
    private OrderService orderService;


    @Autowired
    private PayAppService payAppService;

    @PostMapping("/redemption")
    public String  worldmove(@RequestBody Map<String,Object> item, HttpServletResponse response) {
        System.out.println(item.toString());

        orderService.updateWordMoveItem(item);
        return "1";
    }

    @PostMapping("/tugecallback")
    public ResponseEntity<Map<String, String>> tuge(@RequestBody Map<String,Object> item, HttpServletResponse response) {
        System.out.println(item.toString());

        return orderService.updateTugeItem(item);
    }


    @PostMapping("/papal/ready")
    public Map<String, String> papal(@RequestBody Map<String,Object> params, HttpServletResponse response) throws Exception {

        // 페이팔 결제창 주소 생성
        String approvalUrl = paypalService.createOrder(params);

        Map<String, String> result = new HashMap<>();
        result.put("url", approvalUrl);
        return result;
    }

    @PostMapping("/portone/ready")
    public Map<String, Object> portone(@RequestBody Map<String,Object> params, HttpServletResponse response) throws Exception {

        Map<String, Object> result = portOneService.createOrder(params);

        return result;
    }


    @PostMapping("/payapp/ready")
    public Map<String, Object> payapp(@RequestBody Map<String,Object> params, HttpServletResponse response) throws Exception {

        Map<String, Object> result = payAppService.createOrder(params);

        return result;
    }


    @PostMapping("/payapp/complete")
    public String payappComplete( @RequestParam Map<String,Object> params, HttpServletResponse response) throws Exception {
        // 1. 페이팔에 최종 결제 확정(Capture) 요청
        return payAppService.captureOrder(params);
    }


    @PostMapping("/email/send")
    public Map<String, String> emailSend(@RequestBody Map<String,Object> params, HttpServletResponse response) throws Exception {

        return orderService.insertReTransMailInfo(params);
    }




}
