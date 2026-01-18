package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.ApiCardTypeDto;
import com.naver.naverspabackend.dto.ApiPurchaseItemDto;
import com.naver.naverspabackend.dto.BulkDto;
import com.naver.naverspabackend.dto.OrderDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ApiPurchaseItemMapper {


    List<ApiPurchaseItemDto> selectApiPurchaseItemWithApiPurchaseItemType(ApiPurchaseItemDto apiPurchaseItemDto);

    void deleteWithApiPurchaseItemType(ApiPurchaseItemDto apiPurchaseItemDto);

    void insert(ApiPurchaseItemDto apiPurchaseItemDto);

    ApiPurchaseItemDto findById(ApiPurchaseItemDto apiPurchaseItemDto);

    ApiPurchaseItemDto selectApiPurchaseItemWithApiPurchaseItemTypeAndApiPurchaseItemProcutId(ApiPurchaseItemDto apiPurchaseItemParam);

    List<ApiPurchaseItemDto> selectApiPurchaseItemListForTopup(ApiPurchaseItemDto param);

    void deleteWithApiCardType();

    void insertCardType(ApiCardTypeDto apiCardTypeDto);

    ApiCardTypeDto selectCardTypeFindByCardType(ApiCardTypeDto param);

    List<ApiPurchaseItemDto> selectApiPurchaseItemListForTopupWithTuge(ApiPurchaseItemDto apiPurchaseItemDto);

    List<ApiPurchaseItemDto> selectApiPurchaseItemListForTopupWithEsimaccess(ApiPurchaseItemDto apiPurchaseItemDto);
}
