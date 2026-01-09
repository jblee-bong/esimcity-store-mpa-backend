package com.naver.naverspabackend.batch.tasklet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.naver.naverspabackend.batch.writer.NaverWritter;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 메인 DB에서 batch 관련 설정값을 불러와 ExcuteContext에 저장하는 Step 발송처리
 *
 */

@Component
@Slf4j
public class NaverChangeStatus {

    @Autowired
    private NaverRedisRepository naverRedisRepository;


    @Autowired
    private OrderMapper orderMapper;



    @Value("${naver-api.base}")
    private String baseUrl;

    @Value("${naver-api.trans-status-change}")
    private String orderChangeStatusUrl;

    private String  TRANS_STATUS = "DIRECT_DELIVERY"; //상태 DIRECT_DELIVERY: '적접전달', NOTHING : 배송없음


    public void processNaverStore(StoreDto storeDto) {
        try {
            Gson gson = new Gson();
            Optional<NaverRedisToken> byStoreId = naverRedisRepository.findById(storeDto.getId());

            if (!byStoreId.isPresent()) {
                return ;
            }
            NaverRedisToken naverRedisToken = byStoreId.get();

            Map<String, String> headerMap = naverRedisToken.returnHeaderMap();

            List<OrderDto> orderDtoList = orderMapper.selectOrderForTransStatusChange(storeDto);
            String naverFormatDateNow = CommonUtil.naverFormatDate(0, ChronoUnit.MINUTES);
            if(orderDtoList.size()>0){
                List<Map<String,String>> dataList = new ArrayList<>();
                for(OrderDto orderDto : orderDtoList){
                    Map<String,String> data = new HashMap<>();
                    data.put("productOrderId",orderDto.getProductOrderId());
                    data.put("deliveryMethod",TRANS_STATUS);
                    data.put("dispatchDate",naverFormatDateNow);
                    dataList.add(data);
                }


                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("dispatchProductOrders",dataList);



                String res = ApiUtil.post(baseUrl + orderChangeStatusUrl,headerMap,  paramMap, null, MediaType.parse("application/json; charset=UTF-8"));
                Map<String, Object>  resMap = gson.fromJson(res, Map.class);
                Map<String, Object>  resMap2 = (Map<String, Object>) resMap.get("data");
                List<String> successProductOrderIds = (List<String>) resMap2.get("successProductOrderIds");

                for(String successProductOrderId: successProductOrderIds){
                    OrderDto orderDtoParam = new OrderDto();
                    orderDtoParam.setProductOrderId(successProductOrderId);
                    orderMapper.updateOrderForTransSuccessYn(orderDtoParam);
                }
            }
        } catch (Exception e) {
            log.error("네이버 스토어 오더 처리 중 오류 발생 - Store ID: {}, Store Name: {}, Error: {}", storeDto.getId(), storeDto.getStoreName(), e.getMessage());
        }
    }



}
