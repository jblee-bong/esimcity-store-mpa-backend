package com.naver.naverspabackend.util;

import com.naver.naverspabackend.dto.OrderTugeEsimDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.security.TugeRedisRepository;
import com.naver.naverspabackend.service.apipurchaseitem.ApiPurchaseItemService;
import com.naver.naverspabackend.service.esimapiingsteplogs.EsimApiIngStepLogsService;
import com.naver.naverspabackend.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class EsimUtil {


    static String tsimBaseUri;
    @Value("${api.tsim.baseUrl}")
    public void setTsimBaseUri(String tsimBaseUri) {
        this.tsimBaseUri = tsimBaseUri;
    }


    static String tugeVersion;
    @Value("${api.tuge.version}")
    public void setTugeVersion(String tugeVersion) {
        this.tugeVersion = tugeVersion;
    }

    static String tugeBaseUrl;
    @Value("${api.tuge.base2Url}")
    public void setTugeBaseUrl(String tugeBaseUrl) {
        this.tugeBaseUrl = tugeBaseUrl;
    }


    static String worldMoveBaseUrl;
    @Value("${api.worldmove.baseUrl}")
    public void setWorldMoveBaseUrl(String worldMoveBaseUrl) {
        this.worldMoveBaseUrl = worldMoveBaseUrl;
    }


    static String nizBaseUrl;
    @Value("${api.niz.baseUrl}")
    public void setNizBaseUrl(String nizBaseUrl) {
        this.nizBaseUrl = nizBaseUrl;
    }

    static String airaloBaseUrl;
    @Value("${api.airalo.baseUrl}")
    public void setAiraloBaseUrl(String airaloBaseUrl) {
        this.airaloBaseUrl = airaloBaseUrl;
    }


    static String esimAccessBaseUrl;
    @Value("${api.esimaccess.baseUrl}")
    public void setEsimAccessBaseUrl(String esimAccessBaseUrl) {
        this.esimAccessBaseUrl = esimAccessBaseUrl;
    }

    static String esimAccessClientId;
    @Value("${api.esimaccess.clientId}")
    public void setEsimAccessClientId(String esimAccessClientId) {
        this.esimAccessClientId = esimAccessClientId;
    }

    static String esimAccessClientSecret;
    @Value("${api.esimaccess.clientSecret}")
    public void setEsimAccessClientSecret(String esimAccessClientSecret) {
        this.esimAccessClientSecret = esimAccessClientSecret;
    }



    static TugeRedisRepository tugeRedisRepository;
    @Autowired
    public void setTugeRedisRepository(TugeRedisRepository tugeRedisRepository) {
        this.tugeRedisRepository = tugeRedisRepository;
    }


    static OrderService orderService;
    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }



    static ApiPurchaseItemService apiPurchaseItemService;
    @Autowired
    public void setApiPurchaseItemService(ApiPurchaseItemService apiPurchaseItemService) {
        this.apiPurchaseItemService = apiPurchaseItemService;
    }



    public static OrderMapper orderMapper;
    @Autowired
    public void setOrderMapper(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    public static EsimApiIngStepLogsService esimApiIngStepLogsService;
    @Autowired
    public void setEsimApiIngStepLogsService(EsimApiIngStepLogsService esimApiIngStepLogsService) {
        this.esimApiIngStepLogsService = esimApiIngStepLogsService;
    }





    public static TsimUtil getTsimUtil(ApiPurchaseItemService apiPurchaseItemService, StoreDto storeDto, String active)  {
        return new TsimUtil(apiPurchaseItemService,storeDto.getEsimApiTsimAccount(), storeDto.getEsimApiTsimSecret(), tsimBaseUri, esimApiIngStepLogsService, active);
    }

    public static TugeUtil getTugeUtil(StoreDto storeDto, String active)  {
        return new TugeUtil(storeDto.getEsimApiTugeAccount(), storeDto.getEsimApiTugeSign(), storeDto.getEsimApiTugeSecret(),storeDto.getEsimApiTugeVector(),tugeVersion,tugeBaseUrl, esimApiIngStepLogsService,tugeRedisRepository,orderService,apiPurchaseItemService, active);
    }

    public static WorldMoveUtil getWorldMoveUtil(StoreDto storeDto)  {
        return new WorldMoveUtil(storeDto.getEsimApiWorldMoveMerchantId(), storeDto.getEsimApiWorldMoveDeptId(), storeDto.getEsimApiWorldMoveToken(), worldMoveBaseUrl, esimApiIngStepLogsService);
    }

    public static NizUtil getNizUtil(StoreDto storeDto)  {
        return new NizUtil(storeDto.getEsimApiNizId(), storeDto.getEsimApiNizPass(), storeDto.getEsimApiNizPartnerCode(), nizBaseUrl, esimApiIngStepLogsService);
    }

    public static AirAloUtil getAirAloUtil(StoreDto storeDto)  {
        return new AirAloUtil(storeDto.getEsimApiAiraloClientId(), storeDto.getEsimApiAiraloClientSecret(), airaloBaseUrl, esimApiIngStepLogsService,orderMapper);
    }

    public static EsimAccessUtil getEsimAccess(StoreDto storeDto)  {
        return new EsimAccessUtil(esimAccessClientId, esimAccessClientSecret, esimAccessBaseUrl, esimApiIngStepLogsService, apiPurchaseItemService,orderMapper);
    }

}
