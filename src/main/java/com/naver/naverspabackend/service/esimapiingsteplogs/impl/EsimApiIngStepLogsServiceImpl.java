package com.naver.naverspabackend.service.esimapiingsteplogs.impl;

import com.google.gson.Gson;
import com.naver.naverspabackend.dto.EsimApiIngStepLogsDto;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.mybatis.mapper.EsimApiIngStepLogsMapper;
import com.naver.naverspabackend.service.esimapiingsteplogs.EsimApiIngStepLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EsimApiIngStepLogsServiceImpl implements EsimApiIngStepLogsService {
    @Autowired
    private EsimApiIngStepLogsMapper esimApiIngStepLogsMapper;

    @Override
    public void insert(String esimLogs, Long orderId) {
        try{
            if(orderId!=null){
                EsimApiIngStepLogsDto esimApiIngStepLogsDto = new EsimApiIngStepLogsDto();
                esimApiIngStepLogsDto.setEsimLogs(esimLogs);
                esimApiIngStepLogsDto.setOrderId(orderId);
                esimApiIngStepLogsMapper.insert(esimApiIngStepLogsDto);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void insertRest(HttpHeaders headers, Object jsonObject, Long orderId) {
        try{
            if(orderId!=null) {
                String header = "header : " + new Gson().toJson(headers);
                String contents = "\ncontents : " + new Gson().toJson(jsonObject);
                EsimApiIngStepLogsDto esimApiIngStepLogsDto = new EsimApiIngStepLogsDto();
                esimApiIngStepLogsDto.setEsimLogs(EsimApiIngSteLogsType.PARAM.getExplain() + header + contents);
                esimApiIngStepLogsDto.setOrderId(orderId);
                esimApiIngStepLogsMapper.insert(esimApiIngStepLogsDto);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
