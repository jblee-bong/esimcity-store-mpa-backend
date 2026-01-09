package com.naver.naverspabackend.service.portone;

import org.springframework.ui.Model;

import java.util.Map;

public interface PortOneService {

    String getAccessToken();

    Map<String, Object> createOrder(Map<String,Object> params) throws Exception;


    void captureOrder(Model model, String token);

    void makeModel(String token,Model model) throws Exception;
}
