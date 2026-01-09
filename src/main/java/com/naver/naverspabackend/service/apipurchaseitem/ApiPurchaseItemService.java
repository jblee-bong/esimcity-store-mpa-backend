package com.naver.naverspabackend.service.apipurchaseitem;

import com.naver.naverspabackend.dto.ApiCardTypeDto;
import com.naver.naverspabackend.dto.ApiPurchaseItemDto;
import com.naver.naverspabackend.enums.ApiType;

import java.util.List;

public interface ApiPurchaseItemService {

    List<ApiPurchaseItemDto> selectApiPurchaseItemWithApiPurchaseItemType(ApiPurchaseItemDto apiPurchaseItemDto);

    void deleteWithApiPurchaseItemType(ApiPurchaseItemDto apiPurchaseItemDto);

    void insert(ApiPurchaseItemDto apiPurchaseItemDto);

    ApiPurchaseItemDto findById(ApiPurchaseItemDto param);

    ApiPurchaseItemDto selectApiPurchaseItemWithApiPurchaseItemTypeAndApiPurchaseItemProcutId(ApiPurchaseItemDto apiPurchaseItemParam);


    void deleteWithApiCardType();

    void insertCardType(ApiCardTypeDto apiCardTypeDto);

    ApiCardTypeDto selectCardTypeFindByCardType(ApiCardTypeDto param);

    List<ApiPurchaseItemDto> selectApiPurchaseItemListForTopup(ApiPurchaseItemDto param);

    List<ApiPurchaseItemDto> selectApiPurchaseItemListForTopupWithTuge(ApiPurchaseItemDto apiPurchaseItemDto);
}