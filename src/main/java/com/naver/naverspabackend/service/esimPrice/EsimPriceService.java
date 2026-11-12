package com.naver.naverspabackend.service.esimPrice;

import com.naver.naverspabackend.dto.EsimPriceDto;

public interface EsimPriceService {

    /**
     * originProductNo가 있으면 TB_ESIM_PRICE_PRODUCT 우선.
     * 없으면(또는 행 없으면) type(또는 sellerManagementCode 시작이름)으로 TB_ESIM_PRICE.
     */
    EsimPriceDto findById(EsimPriceDto esimPriceDto);

    /** origin_product_seller_management_code 앞 2글자(TG/TS/EA/WO...) → ApiType */
    String resolveTypeFromSellerManagementCode(String originProductSellerManagementCode);
}
