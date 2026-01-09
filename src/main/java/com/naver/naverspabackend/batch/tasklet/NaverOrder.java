package com.naver.naverspabackend.batch.tasklet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.batch.writer.NaverWritter;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.ProductOptionDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.ProductMapper;
import com.naver.naverspabackend.mybatis.mapper.ProductOptionMapper;
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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 메인 DB에서 batch 관련 설정값을 불러와 ExcuteContext에 저장하는 Step
 *
 */

@Component
@Slf4j
public class NaverOrder {

    @Autowired
    private NaverRedisRepository naverRedisRepository;


    @Autowired
    private NaverWritter naverWritter;


    @Value("${naver-api.base}")
    private String baseUrl;

    @Value("${naver-api.order-list-info}")
    private String orderListInfoUrl;

    @Value("${naver-api.order-detail-info}")
    private String orderDetailInfoUrl;

    // 요청량 최대 사이즈
    private final int LIMIT_SIZE = 300;

    // 검색할 diff 시간
    private final int DIFF_TIME = -30;


    public void processNaverStore(StoreDto storeDto) {
        try {
            Optional<NaverRedisToken> byStoreId = naverRedisRepository.findById(storeDto.getId());

            if (!byStoreId.isPresent()){
                return;
            }

            NaverRedisToken naverRedisToken = byStoreId.get();

            Map<String, String> headerMap = naverRedisToken.returnHeaderMap();

            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("lastChangedFrom", URLEncoder.encode(CommonUtil.naverFormatDate(DIFF_TIME, ChronoUnit.MINUTES), StandardCharsets.UTF_8.name()));
            //paramMap.put("lastChangedType", "PAYED");

            Map<String, Object> resMap = new HashMap<>();

            try {
                resMap = requestOrderNoList(headerMap, paramMap);
            } catch (Exception e) {
                log.error(storeDto.getStoreName()+" "   +  e.getMessage());
                return;
            }

            // 최종 입력 될 product 상품
            List<OrderDto> orderDtoList = new ArrayList<>();

            if( resMap.get("data") != null) {

                Map<String, Object> moreMap = (Map<String, Object>) ((Map) resMap.get("data")).get("more");
                List<Map<String, Object>> orderListMap = (List<Map<String, Object>>) ((Map) resMap.get("data")).get("lastChangeStatuses");

                while (moreMap != null && moreMap.get("moreFrom") != null) {
                    paramMap.put("lastChangedFrom", URLEncoder.encode((String) moreMap.get("moreFrom"), StandardCharsets.UTF_8.name()));
                    paramMap.put("moreSequence", moreMap.get("moreSequence"));

                    moreMap = requestOrderNoList(headerMap, paramMap);

                    List<Map<String, Object>> moreOrderListMap = (List<Map<String, Object>>) ((Map) resMap.get("data")).get("lastChangeStatuses");

                    orderListMap.addAll(moreOrderListMap);
                }

                List<String> productOrderIds = new ArrayList<>();

                productOrderIds = orderListMap.stream().map(e -> e.get("productOrderId") + "").collect(Collectors.toList());

                List<Map<String, Object>> orderDetailList = requestOrderDetailList(productOrderIds, headerMap);

                orderDtoList = buildOrderDtoList(orderDetailList, storeDto);
            }

            naverWritter.orderWrite(orderDtoList);
        } catch (Exception e) {
            log.error("네이버 스토어 오더 처리 중 오류 발생 - Store ID: {}, Store Name: {}, Error: {}", storeDto.getId(), storeDto.getStoreName(), e.getMessage());
        }
    }


    public Map<String, Object> requestOrderNoList(Map<String, String> headerMap, Map<String, Object> paramMap) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        String res = ApiUtil.get(baseUrl + orderListInfoUrl, paramMap, headerMap, true);
        return objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});

    }

    public List<Map<String, Object>> requestOrderDetailList(List<String> productOrderIds, Map<String, String> headerMap) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> paramMap = new HashMap<>();

        int repeat = productOrderIds.size() % LIMIT_SIZE != 0 ? productOrderIds.size()  / LIMIT_SIZE + 1 : productOrderIds.size()  / LIMIT_SIZE;

        List<List<String>> divicideList = new ArrayList<>();

        for(int i = 0; i < repeat; i++){

            // i 가 마지막이 아니라면
            if (i != (repeat - 1)) {
                divicideList.add(productOrderIds.subList(i * LIMIT_SIZE, (i + 1) * LIMIT_SIZE));

                // i 가 마지막 이라면
            } else {
                divicideList.add(productOrderIds.subList(i * LIMIT_SIZE, productOrderIds.size()));
            }
        }
        List resultList = new ArrayList();

        for (int i = 0; i < divicideList.size(); i++) {

            List<String> productOrderIds1 = divicideList.get(i);

            paramMap.put("productOrderIds", productOrderIds1);
            try {
                String res = ApiUtil.post(baseUrl + orderDetailInfoUrl, headerMap, paramMap, null, MediaType.parse("application/json; charset=UTF-8"));
                Map<String, Object> stringObjectMap = objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {
                });
                resultList.addAll((List) stringObjectMap.get("data"));
            }catch (Exception e){
                throw e;
            }
        }

        return resultList;
    }

    public List<OrderDto> buildOrderDtoList(List<Map<String, Object>> orderDetailList, StoreDto storeDto){
        List<OrderDto> orderDtoList = new ArrayList<>();

        for (Map<String, Object> objectMap : orderDetailList) {
            OrderDto orderDto = new OrderDto();

            Map<String, Object> productOrder = (Map<String, Object>) objectMap.get("productOrder");
            Map<String, Object> order = (Map<String, Object>) objectMap.get("order");

            String productOrderStatus = Objects.toString(productOrder.get("productOrderStatus"), "");
            if(productOrderStatus.equals("CANCELED_BY_NOPAYMENT") || productOrderStatus.equals("PAYMENT_WAITING") ){
                continue;
            }

            if(productOrder.get("shippingAddress")!=null){
                Map<String, Object> shippingAddress = (Map<String, Object>) productOrder.get("shippingAddress");
                String shippingBaseAddress = Objects.toString(shippingAddress.get("baseAddress"), "");
                String shippingZipCode = Objects.toString(shippingAddress.get("zipCode"), "");
                String shippingDetailedAddress = Objects.toString(shippingAddress.get("detailedAddress"), "");
                String shippingTel1 = Objects.toString(shippingAddress.get("tel1"), "").replaceAll("-","");
                String shippingTel2 = Objects.toString(shippingAddress.get("tel2"), "").replaceAll("-","");
                String shippingName = Objects.toString(shippingAddress.get("name"), "");

                orderDto.setShippingBaseAddress(shippingBaseAddress);
                orderDto.setShippingZipCode(shippingZipCode);
                orderDto.setShippingDetailedAddress(shippingDetailedAddress);
                orderDto.setShippingTel1(shippingTel1);
                orderDto.setShippingTel2(shippingTel2);
                orderDto.setShippingName(shippingName);
            }



            String productName = Objects.toString(productOrder.get("productName"), "");
            String productOption = Objects.toString(productOrder.get("productOption"), "");
            String originalProductId = Objects.toString(productOrder.get("originalProductId"), "");
            String optionCode = Objects.toString(productOrder.get("optionCode"), "");
            String productOrderId = Objects.toString(productOrder.get("productOrderId"), "");
            String shippingMemo = Objects.toString(productOrder.get("shippingMemo"), "");


            String quantity = Objects.toString(productOrder.get("quantity"), "1");
            String unitPrice = Objects.toString(productOrder.get("unitPrice"), "");

            String deliveryFeeAmount = Objects.toString(productOrder.get("deliveryFeeAmount"), ""); //택배비
            String totalPaymentAmount = Objects.toString(productOrder.get("totalPaymentAmount"), "");//판매가

            orderDto.setProductName(productName);
            orderDto.setProductOption(productOption);
            orderDto.setOriginProductNo(Long.valueOf(originalProductId));
            orderDto.setProductOrderId(productOrderId);
            orderDto.setOptionId(Long.valueOf(optionCode));
            orderDto.setShippingMemo(shippingMemo);


            orderDto.setQuantity(Integer.parseInt(quantity));
            orderDto.setAllQuantity(Integer.parseInt(quantity));
            orderDto.setCancelQuantity(0);

            orderDto.setUnitPrice(unitPrice);
            orderDto.setDeliveryFeeAmount(deliveryFeeAmount);
            orderDto.setTotalPaymentAmount(totalPaymentAmount);

            String ordererId = Objects.toString(order.get("ordererId"), "");
            String ordererName = Objects.toString(order.get("ordererName"), "");
            String orderId = Objects.toString(order.get("orderId"), "");
            String paymentDate = Objects.toString(order.get("paymentDate"), "");
            String ordererNo = Objects.toString(order.get("ordererNo"), "");
            String ordererTel = Objects.toString(order.get("ordererTel"), "");

            orderDto.setOrdererId(ordererId);
            orderDto.setOrdererName(ordererName);
            orderDto.setOrderId(orderId);
            if(!"".equals(paymentDate)){
                orderDto.setPaymentDate(CommonUtil.parseNaverFormatDate(paymentDate));
            }
            orderDto.setOrdererNo(ordererNo);
            orderDto.setOrdererTel(ordererTel);

            orderDto.setStoreId(storeDto.getId());

            orderDto.setInsertFlag(true);


            String claimType = Objects.toString(productOrder.get("claimType"), "");
            String claimStatus = Objects.toString(productOrder.get("claimStatus"), "");
            //발송 상태 0: 미발송, 1: 발신 , 2:부분실패, 3:발송실패, 4: 취소, 5 : 취소요청 ,6: 반품, 7: 반품요청 99: 이전으로 돌리기
            if(!claimType.equals("") && claimType.equals("CANCEL")){ //취소상태
                if(!claimStatus.equals("")){
                    if(claimStatus.equals("CANCEL_REQUEST")){//취소 요청
                        cancelOrderDto(orderDto);
                        orderDto.setSendStatus("5");
                    }else if(claimStatus.equals("CANCELING")){//취소 처리중
                        cancelOrderDto(orderDto);
                        orderDto.setSendStatus("5");
                    }else if(claimStatus.equals("CANCEL_DONE") || claimStatus.equals("ADMIN_CANCEL_DONE")){//취소 완료
                        cancelOrderDto(orderDto);
                        orderDto.setSendStatus("4");
                    }else if(claimStatus.equals("CANCEL_REJECT") || claimStatus.equals("ADMIN_CANCEL_REJECT")){//취소 철회
                        orderDto.setSendStatus("99");
                    }
                }
            }
            if(!claimType.equals("") && claimType.equals("ADMIN_CANCEL")){ //취소상태
                if(!claimStatus.equals("")){
                    if(claimStatus.equals("CANCEL_REQUEST")){//취소 요청
                        cancelOrderDto(orderDto);
                        orderDto.setSendStatus("5");
                    }else if(claimStatus.equals("CANCELING")){//취소 처리중
                        cancelOrderDto(orderDto);
                        orderDto.setSendStatus("5");
                    }else if(claimStatus.equals("CANCEL_DONE") || claimStatus.equals("ADMIN_CANCEL_DONE")){//취소 완료
                        cancelOrderDto(orderDto);
                        orderDto.setSendStatus("4");
                    }else if(claimStatus.equals("CANCEL_REJECT") || claimStatus.equals("ADMIN_CANCEL_REJECT")){//취소 철회
                        orderDto.setSendStatus("99");
                    }
                }
            }

            if(!claimType.equals("") && claimType.equals("RETURN")){//반품상태
                if(!claimStatus.equals("")){

                    if(claimStatus.equals("RETURN_REQUEST")){//반품 요청
                        cancelOrderDto(orderDto);
                        orderDto.setSendStatus("7");
                    }else if(claimStatus.equals("RETURN_DONE")){//반품 완료
                        cancelOrderDto(orderDto);
                        orderDto.setSendStatus("6");
                    }else if(claimStatus.equals("RETURN_REJECT")){//반품 철회
                        orderDto.setSendStatus("99");
                    }
                }
            }

            orderDtoList.add(orderDto);
        }

        return orderDtoList;
    }

    private void cancelOrderDto(OrderDto orderDto){
        orderDto.setCancelQuantity(orderDto.getAllQuantity());
        orderDto.setQuantity(0);
    }

}
