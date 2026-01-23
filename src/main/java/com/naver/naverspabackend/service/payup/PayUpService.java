package com.naver.naverspabackend.service.payup;

import org.springframework.ui.Model;

import java.util.Map;

public interface PayUpService {

    Map<String, Object> createOrder(Map<String,Object> params) throws Exception;

    void captureOrder(Model model, String transactionId, String token, String amount);

}
