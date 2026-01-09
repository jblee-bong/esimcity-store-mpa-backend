package com.naver.naverspabackend.service.papal;

import org.springframework.ui.Model;

import java.util.Map;

public interface PaypalService {

    String getAccessToken();

    String createOrder(Map<String,Object> params) throws Exception;


    String captureOrder(String orderId);

    void makeModel(String token,Model model) throws Exception;
}
