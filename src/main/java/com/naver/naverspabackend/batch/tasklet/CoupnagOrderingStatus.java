package com.naver.naverspabackend.batch.tasklet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.naver.naverspabackend.dto.OrderDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

/**
 * 메인 DB에서 batch 관련 설정값을 불러와 ExcuteContext에 저장하는 Step
 *
 */

@Component
@Slf4j
public class CoupnagOrderingStatus {


    @Autowired
    private OrderMapper orderMapper;

    @Value("${coupang-api.base}")
    private String baseUrl;

    @Value("${coupang-api.ordering-status-ready}")
    private String orderingStatusReadyUrl;

    @Value("${coupang-api.ordering-status-check}")
    private String orderingStatusCheckUrl;


    public void processCoupangStore(StoreDto storeDto) {
        try {
            List<OrderDto> orderDtoList = orderMapper.selectOrderForOrderingStatusChange(storeDto);
            if(orderDtoList.size()>0){
                // shipmentBoxIds를 50개씩 나누어서 처리
                int batchSize = 50;
                for (int i = 0; i < orderDtoList.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, orderDtoList.size());
                    List<OrderDto> batchOrderDtos = orderDtoList.subList(i, endIndex);
                    //배송준비 처리
                    Map<String, Object> resMap = requestChangeOrderReady(storeDto, batchOrderDtos);
                    HashMap<String, Object> data = (HashMap<String, Object>) resMap.get("data");

                    List<HashMap<String, Object>> responseList = (List<HashMap<String, Object>>) data.get("responseList");
                    List<String> shipmentBoxIds = new ArrayList<>();
                    for(HashMap<String, Object> response : responseList){
                        String shipmentBox = response.get("shipmentBoxId").toString();
                        boolean succed = (boolean) response.get("succeed");
                        //배송중 처리 실패시, 현재 상태 체크
                        if(!succed){
                            Map<String, Object> resCheckMap = requestChangeOrderCheck(storeDto, shipmentBox);
                            HashMap<String, Object> checkData = (HashMap<String, Object>) resCheckMap.get("data");
                            succed = checkData.get("status").equals("INSTRUCT") || checkData.get("status").equals("DEPARTURE") || checkData.get("status").equals("NONE_TRACKING") || checkData.get("status").equals("FINAL_DELIVERY") || checkData.get("status").equals("DELIVERING");
                            /*
                            ACCEPT	결제완료
                            INSTRUCT	상품준비중
                            DEPARTURE	배송지시
                            DELIVERING	배송중
                            FINAL_DELIVERY	배송완료
                            NONE_TRACKING	업체 직접 배송(배송 연동 미적용), 추적불가
                            */
                        }

                        if(succed){
                            String productOrderId = null;
                            for(OrderDto orderDto : batchOrderDtos){
                                if(orderDto.getShipmentBoxId().equals(shipmentBox)){
                                    productOrderId = orderDto.getProductOrderId();
                                    continue;
                                }
                            }
                            if(productOrderId==null)
                                continue;
                            OrderDto orderDtoParam = new OrderDto();
                            //0: 발주처리필요, 1:발주처리완료, 2:발주처리불필요
                            orderDtoParam.setOrderingUseStatus(1);
                            orderDtoParam.setProductOrderId(productOrderId);
                            orderMapper.updateOrderForOrderingUseStatus(orderDtoParam);
                        }
                    }

                }
            }
        } catch (Exception e) {
            log.error("네이버 스토어 오더 처리 중 오류 발생 - Store ID: {}, Store Name: {}, Error: {}", storeDto.getId(), storeDto.getStoreName(), e.getMessage());
        }
    }


    public Map<String, Object> requestChangeOrderReady(StoreDto storeDto, List<OrderDto> batchOrderDtos) throws Exception {

        List<String> shipmentBoxIds = new ArrayList<>();
        for(OrderDto orderDto : batchOrderDtos){
            if(orderDto.getShipmentBoxId()!=null)
                shipmentBoxIds.add(orderDto.getShipmentBoxId());
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(orderingStatusReadyUrl);
        UriComponents uriComponents = builder.buildAndExpand(storeDto.getVendorId() + "");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("vendorId", storeDto.getVendorId());
        paramMap.put("shipmentBoxIds", shipmentBoxIds);

        String uri = uriComponents.toUriString();

// 로그 확인
        System.out.println("최종 호출 URL: " + baseUrl + uri);

        String authorization = CoupangUtil.getAuthorization("PUT",uri,storeDto.getClientId(),storeDto.getClientSecret());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", authorization);
        headerMap.put("content-type", "application/json");
        Map<String, Object> resMap = new HashMap<>();

        String res = ApiUtil.put(baseUrl + uri,headerMap, paramMap,null ,MediaType.parse("application/json; charset=UTF-8"));

        return objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});
    }


    public Map<String, Object> requestChangeOrderCheck(StoreDto storeDto, String shipmentBoxId) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(orderingStatusCheckUrl);
        UriComponents uriComponents = builder.buildAndExpand(storeDto.getVendorId() + "", shipmentBoxId);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("vendorId", storeDto.getVendorId());
        paramMap.put("shipmentBoxId", shipmentBoxId);

        String uri = ApiUtil.buildQueryParameter(uriComponents.toUriString(),paramMap,true);

        String authorization = CoupangUtil.getAuthorization("GET",uri,storeDto.getClientId(),storeDto.getClientSecret());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", authorization);
        headerMap.put("content-type", "application/json");
        String res = ApiUtil.get(baseUrl + uri,headerMap);
        return objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});
    }




}
