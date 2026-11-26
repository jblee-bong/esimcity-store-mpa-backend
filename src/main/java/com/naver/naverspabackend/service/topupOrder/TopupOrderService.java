package com.naver.naverspabackend.service.topupOrder;

import com.naver.naverspabackend.dto.EsimPriceDto;
import com.naver.naverspabackend.dto.TopupOrderDto;

import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.List;
import java.util.Map;

public interface TopupOrderService {

    void insert(TopupOrderDto topupOrderDto);

    TopupOrderDto findByTokenId(TopupOrderDto param);

    void updateTopupStatus(TopupOrderDto topupOrderDto);

    void updatePaypalStatus(TopupOrderDto topupOrderDto);

    void updateTopupOrderNo(TopupOrderDto topupOrderDto);

    List<TopupOrderDto> findByEsimIccidSuccessCharge(TopupOrderDto topupOrderParam);

    TopupOrderDto findById(TopupOrderDto param);

    void updateTransactionId(TopupOrderDto topupOrderDto);

    TopupOrderDto findByIccidForMaxTopupId(TopupOrderDto topupOrderDtoParam);

    ApiResult<List<TopupOrderDto>> fetchTopupList(Map<String, Object> map, PagingUtil pagingUtil);

    ApiResult<?> fetchTopupStatic(Map<String, Object> map);
}
