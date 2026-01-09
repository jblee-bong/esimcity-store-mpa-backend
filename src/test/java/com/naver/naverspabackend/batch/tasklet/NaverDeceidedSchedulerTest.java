package com.naver.naverspabackend.batch.tasklet;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.dto.*;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.TugeRedisRepository;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.service.apipurchaseitem.ApiPurchaseItemService;
import com.naver.naverspabackend.service.esimPrice.EsimPriceService;
import com.naver.naverspabackend.service.product.ProductService;
import com.naver.naverspabackend.service.sms.MatchInfoService;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.OriginTsimUtil;
import com.naver.naverspabackend.util.OriginTugeUtil;
import com.naver.naverspabackend.util.OriginWorldMoveUtil;
import okhttp3.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;


/**
 * esim api의 상품 목록 조회화여 db 저장
 *
 * @author jblee
 */



@SpringBootTest
public class NaverDeceidedSchedulerTest {

    @Autowired
    NaverDeceided naverDeceided;



    @Autowired
    private StoreMapper storeMapper;



    @Autowired
    private NaverSetting naverSetting;
    @Test
    void ApiPurchaseList () throws Exception {
        Map<String,Object> param = new HashMap<>();
        param.put("userAuthority","ROLE_ADMIN");
        List<StoreDto> storeDtos = storeMapper.selectStoreList(param);
        for (StoreDto storeDto : storeDtos) {

            if(storeDto.getPlatform()==null){
            }else if(storeDto.getPlatform().equals("naver")){
                naverDeceided.processNaverStore(storeDto);
            }else if(storeDto.getPlatform().equals("coupang")){
                //coupnagChangeStatus.processCoupangStore(storeDto);
            }else{
            }


        }



    }
}