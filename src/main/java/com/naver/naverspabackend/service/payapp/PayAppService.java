package com.naver.naverspabackend.service.payapp;

import org.springframework.ui.Model;

import java.util.Map;

public interface PayAppService {

    Map<String, Object> createOrder(Map<String,Object> params) throws Exception;


    String captureOrder( Map<String,Object> params);

    void makeModel(String token,Model model) throws Exception;

}
