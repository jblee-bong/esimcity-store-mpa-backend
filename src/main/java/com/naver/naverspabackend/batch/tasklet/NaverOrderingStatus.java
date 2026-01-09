package com.naver.naverspabackend.batch.tasklet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.naver.naverspabackend.batch.writer.CoupangWritter;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.mybatis.mapper.ProductMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.CoupangUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 메인 DB에서 batch 관련 설정값을 불러와 ExcuteContext에 저장하는 Step
 *
 */

@Component
@Slf4j
public class NaverOrderingStatus {

    @Autowired
    private NaverRedisRepository naverRedisRepository;



    @Autowired
    private OrderMapper orderMapper;



    @Value("${naver-api.base}")
    private String baseUrl;

    @Value("${naver-api.ordering-status-change}")
    private String orderingChangeStatusUrl;

    @Autowired
    private ProductMapper productMapper;

    public void processNaverStore(StoreDto storeDto) {
        try {
            Gson gson = new Gson();
            Optional<NaverRedisToken> byStoreId = naverRedisRepository.findById(storeDto.getId());

            if (!byStoreId.isPresent()) {
                return;
            }
            NaverRedisToken naverRedisToken = byStoreId.get();

            Map<String, String> headerMap = naverRedisToken.returnHeaderMap();

            List<OrderDto> orderDtoList = orderMapper.selectOrderForOrderingStatusChange(storeDto);
            if(orderDtoList.size()>0){
                List<String> productOrderIds = new ArrayList<>();
                for(OrderDto orderDto : orderDtoList){
                    productOrderIds.add(orderDto.getProductOrderId());
                }
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("productOrderIds",productOrderIds);
                String res = ApiUtil.post(baseUrl + orderingChangeStatusUrl,headerMap,  paramMap, null, MediaType.parse("application/json; charset=UTF-8"));
                Map<String, Object>  resMap = gson.fromJson(res, Map.class);
                Map<String, Object>  resMap2 = (Map<String, Object>) resMap.get("data");
                List<Map<String,Object>> successProductOrderInfos = (List<Map<String,Object>>) resMap2.get("successProductOrderInfos");

                for(Map<String,Object> successProductOrderInfo: successProductOrderInfos){
                    String productOrderId = successProductOrderInfo.get("productOrderId").toString();
                    boolean isReceiverAddressChanged = (boolean) successProductOrderInfo.get("isReceiverAddressChanged");
                    OrderDto orderDtoParam = new OrderDto();
                    //0: 발주처리필요, 1:발주처리완료, 2:발주처리불필요
                    orderDtoParam.setOrderingUseStatus(1);
                    orderDtoParam.setProductOrderId(productOrderId);
                    orderMapper.updateOrderForOrderingUseStatus(orderDtoParam);
                }

            }
        } catch (Exception e) {
            log.error("네이버 스토어 오더 처리 중 오류 발생 - Store ID: {}, Store Name: {}, Error: {}", storeDto.getId(), storeDto.getStoreName(), e.getMessage());
        }
    }

}
