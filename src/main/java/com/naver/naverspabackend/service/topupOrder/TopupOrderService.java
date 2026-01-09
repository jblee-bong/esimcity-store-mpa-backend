package com.naver.naverspabackend.service.topupOrder;

import com.naver.naverspabackend.dto.EsimPriceDto;
import com.naver.naverspabackend.dto.TopupOrderDto;

public interface TopupOrderService {

    void insert(TopupOrderDto topupOrderDto);

    TopupOrderDto findByTokenId(TopupOrderDto param);

    void updateTopupStatus(TopupOrderDto topupOrderDto);

    void updatePaypalStatus(TopupOrderDto topupOrderDto);

    void updateTopupOrderNo(TopupOrderDto topupOrderDto);
}
