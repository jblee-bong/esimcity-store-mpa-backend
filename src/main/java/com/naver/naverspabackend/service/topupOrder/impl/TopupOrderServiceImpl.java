package com.naver.naverspabackend.service.topupOrder.impl;

import com.naver.naverspabackend.dto.EsimPriceDto;
import com.naver.naverspabackend.dto.TopupOrderDto;
import com.naver.naverspabackend.mybatis.mapper.EsimPriceMapper;
import com.naver.naverspabackend.mybatis.mapper.TopupOrderMapper;
import com.naver.naverspabackend.service.topupOrder.TopupOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TopupOrderServiceImpl implements TopupOrderService {
    @Autowired
    TopupOrderMapper topupOrderMapper;

    @Override
    public void insert(TopupOrderDto topupOrderDto) {
        topupOrderMapper.insert(topupOrderDto);
    }

    @Override
    public TopupOrderDto findByTokenId(TopupOrderDto param) {
        return topupOrderMapper.findByTokenId(param);
    }

    @Override
    public void updateTopupStatus(TopupOrderDto topupOrderDto) {
        topupOrderMapper.updateTopupStatus(topupOrderDto);
    }

    @Override
    public void updatePaypalStatus(TopupOrderDto topupOrderDto) {
        topupOrderMapper.updatePaypalStatus(topupOrderDto);
    }

    @Override
    public void updateTopupOrderNo(TopupOrderDto topupOrderDto) {
        topupOrderMapper.updateTopupOrderNo(topupOrderDto);

    }

    @Override
    public List<TopupOrderDto> findByEsimIccidSuccessCharge(TopupOrderDto topupOrderParam) {
        return topupOrderMapper.findByEsimIccidSuccessCharge(topupOrderParam);
    }

    @Override
    public TopupOrderDto findById(TopupOrderDto param) {
        return topupOrderMapper.findById(param);
    }

    @Override
    public void updateTransactionId(TopupOrderDto topupOrderDto) {
        topupOrderMapper.updateTransactionId(topupOrderDto);

    }
}
