package com.naver.naverspabackend.batch.tasklet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.CoupangUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 메인 DB에서 batch 관련 설정값을 불러와 ExcuteContext에 저장하는 Step
 *
 */

@Component
@Slf4j
public class CoupnagChangeStatus {


    @Autowired
    private OrderMapper orderMapper;

    @Value("${coupang-api.base}")
    private String baseUrl;


    @Value("${coupang-api.ordering-status-ing}")
    private String orderingStatusIngUrl;

    @Value("${coupang-api.ordering-status-check}")
    private String orderingStatusCheckUrl;

    public void processCoupangStore(StoreDto storeDto) {
        try {

            List<OrderDto> orderDtoList = orderMapper.selectOrderForTransStatusChange(storeDto);


            if(orderDtoList.size()>0){

                int batchSize = 50;
                for (int i = 0; i < orderDtoList.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, orderDtoList.size());
                    List<OrderDto> batchOrderDtos = orderDtoList.subList(i, endIndex);
                    List<String> shipmentBoxIds = new ArrayList<>();
                    for(OrderDto batchOrderDto: batchOrderDtos){
                        if(batchOrderDto.getShipmentBoxId()!=null)
                            shipmentBoxIds.add(batchOrderDto.getShipmentBoxId());
                    }

                    if(shipmentBoxIds.size()>0){
                        Map<String, Object> resIngMap = requestChangeOrderIng(storeDto, shipmentBoxIds, batchOrderDtos);
                        HashMap<String, Object> ingData = (HashMap<String, Object>) resIngMap.get("data");
                        List<HashMap<String, Object>> responseIngList = (List<HashMap<String, Object>>) ingData.get("responseList");

                        for(HashMap<String, Object> responseIng : responseIngList){
                            String shipmentBox = responseIng.get("shipmentBoxId").toString();
                            boolean succed = (boolean) responseIng.get("succeed");

                            //배송완료 처리 실패시, 현재 상태 체크
                            if(!succed){
                                Map<String, Object> resCheckMap = requestChangeOrderCheck(storeDto, shipmentBox);
                                HashMap<String, Object> checkData = (HashMap<String, Object>) resCheckMap.get("data");
                                succed = checkData.get("status").equals("FINAL_DELIVERY") || checkData.get("status").equals("NONE_TRACKING");
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
                                orderDtoParam.setProductOrderId(productOrderId);
                                orderMapper.updateOrderForTransSuccessYn(orderDtoParam);
                            }
                        }
                    }


                }



            }

        } catch (Exception e) {
            log.error("네이버 스토어 오더 처리 중 오류 발생 - Store ID: {}, Store Name: {}, Error: {}", storeDto.getId(), storeDto.getStoreName(), e.getMessage());
        }
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



    public Map<String, Object> requestChangeOrderIng(StoreDto storeDto, List<String> shipmentBoxIds, List<OrderDto> batchOrderDtos) throws Exception {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(orderingStatusIngUrl);
        UriComponents uriComponents = builder.buildAndExpand(storeDto.getVendorId() + "");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("vendorId", storeDto.getVendorId());
        List<Map<String, Object>> paramSubMapList = new ArrayList<>();
        for(String shipmentBoxId: shipmentBoxIds){
            String orderId = "";
            String vendorItemId = "";
            for(OrderDto orderDto : batchOrderDtos){
                if(orderDto.getShipmentBoxId().equals(shipmentBoxId)){
                    orderId = orderDto.getProductOrderId();
                    vendorItemId = orderDto.getOptionId()+"";
                }
            }
            if(orderId.equals(""))
                continue;
            Map<String, Object> paramSubMap = new HashMap<>();
            paramSubMap.put("shipmentBoxId", shipmentBoxId);
            paramSubMap.put("orderId", orderId);
            paramSubMap.put("vendorItemId", vendorItemId);
            paramSubMap.put("deliveryCompanyCode", "DIRECT");
            paramSubMap.put("invoiceNumber", "TICKET-" + shipmentBoxId);
            paramSubMap.put("splitShipping", false);
            paramSubMap.put("preSplitShipped", false);
            paramSubMap.put("estimatedShippingDate", "");
            paramSubMapList.add(paramSubMap);
        }
        paramMap.put("orderSheetInvoiceApplyDtos",paramSubMapList);

        String uri = uriComponents.toUriString();


        String authorization = CoupangUtil.getAuthorization("POST",uri,storeDto.getClientId(),storeDto.getClientSecret());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", authorization);
        headerMap.put("content-type", "application/json");
        Map<String, Object> resMap = new HashMap<>();

        String res = ApiUtil.post(baseUrl + uri,headerMap, paramMap,null ,MediaType.parse("application/json; charset=UTF-8"));

        return objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});
    }


}
