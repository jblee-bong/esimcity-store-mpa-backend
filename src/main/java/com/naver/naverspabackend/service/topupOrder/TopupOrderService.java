package com.naver.naverspabackend.service.topupOrder;

import com.naver.naverspabackend.dto.EsimPriceDto;
import com.naver.naverspabackend.dto.TopupOrderDto;

import java.util.List;

public interface TopupOrderService {

    void insert(TopupOrderDto topupOrderDto);

    TopupOrderDto findByTokenId(TopupOrderDto param);

    void updateTopupStatus(TopupOrderDto topupOrderDto);

    void updatePaypalStatus(TopupOrderDto topupOrderDto);

    void updateTopupOrderNo(TopupOrderDto topupOrderDto);

    List<TopupOrderDto> findByEsimIccidSuccessCharge(TopupOrderDto topupOrderParam);

    TopupOrderDto findById(TopupOrderDto param);

    void updateTransactionId(TopupOrderDto topupOrderDto);
}
