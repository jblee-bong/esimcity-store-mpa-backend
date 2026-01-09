package com.naver.naverspabackend.batch.tasklet;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.dto.*;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.service.apipurchaseitem.ApiPurchaseItemService;
import com.naver.naverspabackend.service.esimPrice.EsimPriceService;
import com.naver.naverspabackend.service.order.OrderService;
import com.naver.naverspabackend.service.product.ProductService;
import com.naver.naverspabackend.service.sms.MatchInfoService;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.OriginTsimUtil;
import com.naver.naverspabackend.util.OriginTugeUtil;
import com.naver.naverspabackend.util.OriginWorldMoveUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;


/**
 * esim api의 상품 목록 조회화여 db 저장
 *
 * @author jblee
 */



@Slf4j
@Component
public class ApiRetransMailScheduler {



    @Autowired
    private OrderService orderService;



    @Scheduled(cron = "0 */5 * * * *")
    public void ApiRetransMail () {
        List<OrderRetransMailInfoDto> orderRetransMailInfoDtoList = orderService.fetchRetransMailInfoDtoAll();
        for(OrderRetransMailInfoDto orderRetransMailInfoDto : orderRetransMailInfoDtoList) {
            try{
                Map<String, Object> paramMapMap =new HashMap<>();
                paramMapMap.put("id", orderRetransMailInfoDto.getOrderId());
                paramMapMap.put("reEmail", orderRetransMailInfoDto.getMail());
                orderService.updateOrderMailReTrans(paramMapMap);
                orderService.updateRetransMailInfoComfirm(orderRetransMailInfoDto);
            }catch (Exception e) {

            }
        }
    }
}