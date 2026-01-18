package com.naver.naverspabackend.service.apipurchaseitem.impl;

import com.naver.naverspabackend.dto.ApiCardTypeDto;
import com.naver.naverspabackend.dto.ApiPurchaseItemDto;
import com.naver.naverspabackend.mybatis.mapper.ApiPurchaseItemMapper;
import com.naver.naverspabackend.service.apipurchaseitem.ApiPurchaseItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ApiPurchaseItemServiceImpl implements ApiPurchaseItemService {

    @Autowired
    private ApiPurchaseItemMapper apiPurchaseItemMapper;


    @Override
    public List<ApiPurchaseItemDto> selectApiPurchaseItemWithApiPurchaseItemType(ApiPurchaseItemDto apiPurchaseItemDto) {
        return apiPurchaseItemMapper.selectApiPurchaseItemWithApiPurchaseItemType(apiPurchaseItemDto);
    }

    @Override
    public void deleteWithApiPurchaseItemType(ApiPurchaseItemDto apiPurchaseItemDto) {
        apiPurchaseItemMapper.deleteWithApiPurchaseItemType(apiPurchaseItemDto);
    }

    @Override
    public void insert(ApiPurchaseItemDto apiPurchaseItemDto) {
        apiPurchaseItemMapper.insert(apiPurchaseItemDto);
    }

    @Override
    public ApiPurchaseItemDto findById(ApiPurchaseItemDto param) {
        return apiPurchaseItemMapper.findById(param);
    }

    @Override
    public ApiPurchaseItemDto selectApiPurchaseItemWithApiPurchaseItemTypeAndApiPurchaseItemProcutId(ApiPurchaseItemDto apiPurchaseItemParam) {
        return apiPurchaseItemMapper.selectApiPurchaseItemWithApiPurchaseItemTypeAndApiPurchaseItemProcutId(apiPurchaseItemParam);
    }

    @Override
    public List<ApiPurchaseItemDto> selectApiPurchaseItemListForTopup(ApiPurchaseItemDto param) {
        return apiPurchaseItemMapper.selectApiPurchaseItemListForTopup(param);
    }

    @Override
    public List<ApiPurchaseItemDto> selectApiPurchaseItemListForTopupWithTuge(ApiPurchaseItemDto apiPurchaseItemDto) {

        return apiPurchaseItemMapper.selectApiPurchaseItemListForTopupWithTuge(apiPurchaseItemDto);
    }

    @Override
    public List<ApiPurchaseItemDto> selectApiPurchaseItemListForTopupWithEsimaccess(ApiPurchaseItemDto apiPurchaseItemDto) {
        return apiPurchaseItemMapper.selectApiPurchaseItemListForTopupWithEsimaccess(apiPurchaseItemDto);
    }

    @Override
    public void deleteWithApiCardType() {
        apiPurchaseItemMapper.deleteWithApiCardType();
    }

    @Override
    public void insertCardType(ApiCardTypeDto apiCardTypeDto) {
        apiPurchaseItemMapper.insertCardType(apiCardTypeDto);
    }

    @Override
    public ApiCardTypeDto selectCardTypeFindByCardType(ApiCardTypeDto param) {
        return apiPurchaseItemMapper.selectCardTypeFindByCardType(param);
    }

}
