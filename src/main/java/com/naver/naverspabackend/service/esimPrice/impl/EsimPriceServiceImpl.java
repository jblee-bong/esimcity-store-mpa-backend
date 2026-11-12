package com.naver.naverspabackend.service.esimPrice.impl;

import com.naver.naverspabackend.dto.EsimPriceDto;
import com.naver.naverspabackend.enums.ApiType;
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
        EsimPriceDto byType = null;
        if (esimPriceDto != null
                && esimPriceDto.getType() != null
                && !esimPriceDto.getType().trim().isEmpty()) {
            byType = esimPriceMapper.findById(esimPriceDto);
        }

        if (esimPriceDto != null && esimPriceDto.getOriginProductNo() != null) {
            EsimPriceDto byProduct = esimPriceMapper.findByProduct(esimPriceDto);
            if (byProduct != null) {
                if (byType != null) {
                    // exchange_weight 는 PRODUCT에 없음 → 항상 TB_ESIM_PRICE(type)
                    byProduct.setExchangeWeight(byType.getExchangeWeight());
                    if (byProduct.getExchangeRate() == null) {
                        byProduct.setExchangeRate(byType.getExchangeRate());
                    }
                }
                return byProduct;
            }
        }

        return byType;
    }

    @Override
    public String resolveTypeFromSellerManagementCode(String originProductSellerManagementCode) {
        if (originProductSellerManagementCode == null) {
            return null;
        }
        String code = originProductSellerManagementCode.trim();
        if (code.length() < 2) {
            return null;
        }
        // 앞 2글자 = 타입 (예: "TG 파나마", "TS 피지", "EA 사모아", "WO 이탈리아&오스트리아")
        String prefix = code.substring(0, 2).toUpperCase();
        switch (prefix) {
            case "TG":
                return ApiType.TUGE.name();
            case "TS":
                return ApiType.TSIM.name();
            case "EA":
                return ApiType.ESIMACCESS.name();
            case "WO":
                return ApiType.WORLDMOVE.name();
            default:
                return null;
        }
    }
}
