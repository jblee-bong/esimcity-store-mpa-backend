package com.naver.naverspabackend.service.esimPrice.impl;

import com.naver.naverspabackend.dto.EsimPriceDto;
import com.naver.naverspabackend.mybatis.mapper.EsimPriceMapper;
import com.naver.naverspabackend.service.esimPrice.EsimPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EsimPriceServiceImpl implements EsimPriceService {
    @Autowired
    EsimPriceMapper esimPriceMapper;

    @Override
    public EsimPriceDto findById(EsimPriceDto esimPriceDto) {
        return esimPriceMapper.findById(esimPriceDto);
    }
}
