package com.naver.naverspabackend.batch.tasklet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.batch.writer.CoupangWritter;
import com.naver.naverspabackend.batch.writer.NaverWritter;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.mybatis.mapper.ProductMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.CoupangUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 메인 DB에서 batch 관련 설정값을 불러와 ExcuteContext에 저장하는 Step
 *
 */

@Component
@Slf4j
public class CoupangOrder {

    @Autowired
    private CoupangWritter coupangWritter;


    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Value("${coupang-api.base}")
    private String baseUrl;

    @Value("${coupang-api.order-list}")
    private String orderListInfoUrl;

    @Value("${coupang-api.order-cancel-list}")
    private String orderCancelListInfoUrl;


    private final int DIFF_TIME = 30; //분

    /*
    ACCEPT	결제완료
    INSTRUCT	상품준비중
    DEPARTURE	배송지시
    DELIVERING	배송중
    FINAL_DELIVERY	배송완료
    NONE_TRACKING	업체 직접 배송(배송 연동 미적용), 추적불가
    */
    private final String[] COUPANG_ORDER_STATUS = {"ACCEPT", "INSTRUCT", "DEPARTURE", "DELIVERING", "FINAL_DELIVERY", "NONE_TRACKING"};

    @Autowired
    private ProductMapper productMapper;

    public void processCoupangStore(StoreDto storeDto) {
        try {

            String createdAtFrom = getCreatedAtFrom();
            String createdAtTo = getCreatedAtTo();

            String cancelCreatedAtFrom = getCancelCreatedAtFrom();
            String cancelCreatedAtTo = getCancelCreatedAtTo();

            List<Map<String, Object>> contentsList = new ArrayList<>();
            List<Map<String, Object>> cancelRefundContentsList = new ArrayList<>();

            //구매 리스트 조회
            for(String status : COUPANG_ORDER_STATUS){
                String nextToken = "1";
                while (nextToken!=null){
                    Map<String, Object> resMap = requestOrderList(storeDto, status, nextToken,createdAtFrom, createdAtTo);
                    contentsList.addAll((List<Map<String, Object>>)resMap.get("data"));
                    if(resMap.get("nextToken")==null || resMap.get("nextToken").equals("")){
                        nextToken = null;
                    }else{
                        nextToken = resMap.get("nextToken").toString();
                    }
                }
            }

            //취소 리스트 조회
            String nextToken = "1";
            while (nextToken!=null){
                Map<String, Object> resMap = requestOrderCancelList(storeDto,  nextToken,cancelCreatedAtFrom, cancelCreatedAtTo);
                cancelRefundContentsList.addAll((List<Map<String, Object>>)resMap.get("data"));
                if(resMap.get("nextToken")==null || resMap.get("nextToken").equals("")){
                    nextToken = null;
                }else{
                    nextToken = resMap.get("nextToken").toString();
                }
            }

            //반품 리스트 조회
            nextToken = "1";
            while (nextToken!=null){
                Map<String, Object> resMap = requestOrderRefundList(storeDto,  nextToken,cancelCreatedAtFrom, cancelCreatedAtTo);
                cancelRefundContentsList.addAll((List<Map<String, Object>>)resMap.get("data"));
                if(resMap.get("nextToken")==null || resMap.get("nextToken").equals("")){
                    nextToken = null;
                }else{
                    nextToken = resMap.get("nextToken").toString();
                }
            }

            //주문 만들기
            List<OrderDto> orderDtoList = buildOrderDtoList(contentsList, storeDto);

            //취소만들기
            orderDtoList.addAll(buildCancelOrderDtoList(orderDtoList, cancelRefundContentsList, storeDto));

            coupangWritter.orderWrite(orderDtoList);


        } catch (Exception e) {
            log.error("네이버 스토어 오더 처리 중 오류 발생 - Store ID: {}, Store Name: {}, Error: {}", storeDto.getId(), storeDto.getStoreName(), e.getMessage());
        }
    }

    /* 이 api는 결제 완료 이후단계에서의 리스트만 주기때문에 사용  결제 전 상태(예: 장바구니, 주문 접수, 결제 대기)는 발주서 API로는 조회되지 않음*/
    public Map<String, Object> requestOrderList(StoreDto storeDto, String status, String nextToken, String createdAtFrom, String createdAtTo) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(orderListInfoUrl);
        UriComponents uriComponents = builder.buildAndExpand(storeDto.getVendorId() + "");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("createdAtFrom", createdAtFrom);
        paramMap.put("createdAtTo", createdAtTo);
        paramMap.put("status", status);
        paramMap.put("nextToken", nextToken);
        paramMap.put("searchType", "timeFrame");

        String uri = ApiUtil.buildQueryParameter(uriComponents.toUriString(),paramMap,true);
        String authorization = CoupangUtil.getAuthorization("GET",uri,storeDto.getClientId(),storeDto.getClientSecret());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", authorization);
        headerMap.put("content-type", "application/json");
        Map<String, Object> resMap = new HashMap<>();
        String res = ApiUtil.get(baseUrl + uri,headerMap);

        return objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});
    }

    /* 결제완료 단계
    결제완료단계에서 취소된 주문 조회를 위해서는 status, orderId 파라메터를 제외하고  cancelType=CANCEL*/
    public Map<String, Object> requestOrderCancelList(StoreDto storeDto, String nextToken, String createdAtFrom, String createdAtTo) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(orderCancelListInfoUrl);
        UriComponents uriComponents = builder.buildAndExpand(storeDto.getVendorId() + "");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("searchType", "timeFrame");
        paramMap.put("createdAtFrom", createdAtFrom);
        paramMap.put("createdAtTo", createdAtTo);
        paramMap.put("nextToken", nextToken);
        paramMap.put("cancelType", "CANCEL");

        String uri = ApiUtil.buildQueryParameter(uriComponents.toUriString(),paramMap,true);
        String authorization = CoupangUtil.getAuthorization("GET",uri,storeDto.getClientId(),storeDto.getClientSecret());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", authorization);
        headerMap.put("content-type", "application/json");
        Map<String, Object> resMap = new HashMap<>();
        String res = ApiUtil.get(baseUrl + uri,headerMap);

        return objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});
    }

    /* 결제완료 단계가 아닌 단계
    결제완료단계에서 취소된 주문 조회를 위해서는 status, orderId 파라메터를 제외하고  cancelType=CANCEL*/
    public Map<String, Object> requestOrderRefundList(StoreDto storeDto, String nextToken, String createdAtFrom, String createdAtTo) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(orderCancelListInfoUrl);
        UriComponents uriComponents = builder.buildAndExpand(storeDto.getVendorId() + "");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("searchType", "timeFrame");
        paramMap.put("createdAtFrom", createdAtFrom);
        paramMap.put("createdAtTo", createdAtTo);
        paramMap.put("nextToken", nextToken);
        paramMap.put("cancelType", "RETURN");

        String uri = ApiUtil.buildQueryParameter(uriComponents.toUriString(),paramMap,true);
        String authorization = CoupangUtil.getAuthorization("GET",uri,storeDto.getClientId(),storeDto.getClientSecret());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", authorization);
        headerMap.put("content-type", "application/json");
        Map<String, Object> resMap = new HashMap<>();
        String res = ApiUtil.get(baseUrl + uri,headerMap);

        return objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});
    }


    public List<OrderDto> buildOrderDtoList(List<Map<String, Object>> orderDetailList,  StoreDto storeDto){
        List<OrderDto> orderDtoList = new ArrayList<>();

        for (Map<String, Object> objectMap : orderDetailList) {

            // paidAt 날짜 확인 - 현재 시간보다 크지 않으면 continue
            String paidAtStr = Objects.toString(objectMap.get("paidAt"), "");
            ZonedDateTime paidAt = ZonedDateTime.parse(paidAtStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            if (paidAt.isAfter(now)) {
                continue; // 현재 시간보다 크면 continue
            }


            List<Map<String, Object>> orderItems = (List<Map<String, Object>>) objectMap.get("orderItems");

            for(Map<String, Object> orderItem: orderItems){
                OrderDto orderDto = new OrderDto();
                String shipmentBoxId = Objects.toString(objectMap.get("shipmentBoxId"), "");
                orderDto.setShipmentBoxId(shipmentBoxId);

                orderDto.setInsertFlag(true);

                orderDto.setStoreId(storeDto.getId());
                //주문자
                Map<String, Object> orderer = (Map<String, Object>) objectMap.get("orderer");

                String ordererId = null; //주문자ID
                orderDto.setOrdererId(ordererId);

                String ordererName = Objects.toString(orderer.get("name"), "");//주문자이름
                orderDto.setOrdererName(ordererName);

                String ordererNo = Objects.toString(orderer.get("safeNumber"), "");
                if(orderer.get("ordererNumber") !=null && !orderer.get("ordererNumber").toString().equals("")){
                    ordererNo = orderer.get("ordererNumber").toString();
                }//주문자안전번호
                orderDto.setOrdererNo(ordererNo);

                String ordererTel = Objects.toString(orderer.get("ordererNumber"), "");//주문자휴대폰번호
                orderDto.setOrdererTel(ordererTel);


                String memoText = "";
                if(orderItem.get("etcInfoValues")!=null){
                    List<String> etcInfoValues = (List<String>) orderItem.get("etcInfoValues");
                    for(String etcInfoValue: etcInfoValues){
                        if(!memoText.equals(""))
                            memoText += ",";
                        memoText += etcInfoValue;
                    }
                }
                if(objectMap.get("parcelPrintMessage")!=null && !objectMap.get("parcelPrintMessage").toString().equals("")) {
                    if(!memoText.equals(""))
                        memoText += ",";
                    memoText += objectMap.get("parcelPrintMessage").toString();
                }
                String shippingMemo = Objects.toString(orderer.get("email"), "");//주문자 이메일
                if(!shippingMemo.equals("")){
                    if(!memoText.equals(""))
                        memoText += ",";
                    memoText += shippingMemo;
                }

                orderDto.setShippingMemo(memoText);




                //수취인
                Map<String, Object> receiver = (Map<String, Object>) objectMap.get("receiver");

                String shippingBaseAddress = Objects.toString(receiver.get("addr1"), "");
                orderDto.setShippingBaseAddress(shippingBaseAddress);

                String shippingZipCode = Objects.toString(receiver.get("postCode"), "");
                orderDto.setShippingZipCode(shippingZipCode);

                String shippingDetailedAddress = Objects.toString(receiver.get("addr2"), "");
                orderDto.setShippingDetailedAddress(shippingDetailedAddress);

                String shippingTel1 = Objects.toString(receiver.get("safeNumber"), "").replaceAll("-","");
                orderDto.setShippingTel1(shippingTel1);

                String shippingTel2 = Objects.toString(receiver.get("receiverNumber"), "").replaceAll("-","");
                orderDto.setShippingTel2(shippingTel2);

                String shippingName = Objects.toString(receiver.get("name"), "");
                orderDto.setShippingName(shippingName);



                String productOrderId = Objects.toString(objectMap.get("orderId"), ""); //주문번호ID
                orderDto.setProductOrderId(productOrderId);


                String optionId = Objects.toString(orderItem.get("vendorItemId"), ""); //옵션ID
                orderDto.setOrderId(optionId);
                orderDto.setOptionId(Long.valueOf(optionId));

                String productName = Objects.toString(orderItem.get("sellerProductName"), ""); //등록상품명
                orderDto.setProductName(productName);

                String productOption = Objects.toString(orderItem.get("sellerProductItemName"), "");//등록옵션명
                orderDto.setProductOption(productOption);

                String originalProductId = Objects.toString(orderItem.get("sellerProductId"), "");//등록상품ID
                orderDto.setOriginProductNo(Long.valueOf(originalProductId));


                int shippingCount = Integer.parseInt(Objects.toString(orderItem.get("shippingCount"), "0")); //주문시 item의 구매 수량
                int holdCountForCancel = Integer.parseInt(Objects.toString(orderItem.get("holdCountForCancel"), "0")); //취소가 되어 환불 예정이 수량
                int cancelCount = Integer.parseInt(Objects.toString(orderItem.get("cancelCount"), "0")); //취소가 확정된 수량
                orderDto.setQuantity(shippingCount - (holdCountForCancel + cancelCount )); // 발주수량
                orderDto.setAllQuantity(shippingCount); //주문수량
                orderDto.setCancelQuantity(holdCountForCancel + cancelCount );//취소수량



                Map<String, Object> salesPrice = (Map<String, Object>) orderItem.get("salesPrice"); // 개당 상품가격
                String unitPrice = Objects.toString(salesPrice.get("units"), "");
                orderDto.setUnitPrice(unitPrice);


                Map<String, Object> orderPrice = (Map<String, Object>) orderItem.get("orderPrice"); // 결제 가격 : salesPrice*shippingCount
                String totalPaymentAmount = Objects.toString(orderPrice.get("units"), "");//판매가
                orderDto.setTotalPaymentAmount(totalPaymentAmount);

                Map<String, Object> shippingPrice = (Map<String, Object>) objectMap.get("shippingPrice"); // 배송비
                String deliveryFeeAmount = Objects.toString(shippingPrice.get("units"), ""); //택배비
                orderDto.setDeliveryFeeAmount(deliveryFeeAmount);


                Instant instant = paidAt.toInstant();
                orderDto.setPaymentDate(Date.from(instant)); // 결제시간


                orderDtoList.add(orderDto);
            }
        }
        return orderDtoList;
    }


    /*취소반품처리
               "CANCEL" + "RELEASE_STOP_UNCHECKED" → 취소 요청(취소전)
               "CANCEL" + "RETURNS_COMPLETED" → 취소 완료
               "RETURN" + "RETURNS_UNCHECKED" → 반품 요청
               "RETURN" + "RETURNS_COMPLETED" → 반품 완료
               "REQUEST_COUPANG_CHECK" ->쿠팡확인요청
           * */
    public List<OrderDto> buildCancelOrderDtoList(List<OrderDto> makeOrderDtoList, List<Map<String, Object>> cancelRefundContentsList, StoreDto storeDto){
        List<OrderDto> orderDtoList = new ArrayList<>();
        for(Map<String, Object> cancelRefundContent:cancelRefundContentsList){
            String productOrderId = Objects.toString(cancelRefundContent.get("orderId"), "");
            //취소반품 아이템
            List<Map<String, Object>> returnItems = (List<Map<String, Object>>) cancelRefundContent.get("returnItems");

            for(Map<String, Object> returnItem: returnItems){
                String vendorItemId = Objects.toString(returnItem.get("vendorItemId"), "");
                OrderDto param = new OrderDto();
                param.setProductOrderId(productOrderId);
                param.setOrderId(vendorItemId);
                OrderDto orderDto = orderMapper.selectOrderWithProductOrderIdAndOrderId(param);

                if(orderDto==null) {
                    for (OrderDto makeOrder : makeOrderDtoList) {
                        if(makeOrder.getProductOrderId().equals(productOrderId) && makeOrder.getOrderId().equals(vendorItemId)){
                            orderDto = makeOrder;
                            break;
                        }
                    }
                }
                //주문이 등록되지 않은경우, 주문 시퀀스가 돌기전 취소 발생
                if(orderDto==null){
                    orderDto = new OrderDto();
                    String ordererId = null; //주문자ID
                    orderDto.setOrdererId(ordererId);

                    String requesterName = Objects.toString(cancelRefundContent.get("requesterName"), "");//주문자이름
                    orderDto.setOrdererName(requesterName);

                    String requesterPhoneNumber = Objects.toString(cancelRefundContent.get("requesterPhoneNumber"), "");//주문자안전번호
                    orderDto.setOrdererNo(requesterPhoneNumber);

                    String requesterRealPhoneNumber = Objects.toString(cancelRefundContent.get("requesterRealPhoneNumber"), "");//주문자휴대폰번호
                    orderDto.setOrdererTel(requesterRealPhoneNumber);

                    String shippingMemo = Objects.toString(null, "");//주문자 이메일
                    orderDto.setShippingMemo(shippingMemo);


                    String requesterAddress = Objects.toString(cancelRefundContent.get("requesterAddress"), "");
                    orderDto.setShippingBaseAddress(requesterAddress);

                    String requesterAddressDetail = Objects.toString(cancelRefundContent.get("requesterAddressDetail"), "");
                    orderDto.setShippingDetailedAddress(requesterAddressDetail);

                    String requesterZipCode = Objects.toString(cancelRefundContent.get("requesterZipCode"), "");
                    orderDto.setShippingZipCode(requesterZipCode);


                    String shippingTel1 = Objects.toString(cancelRefundContent.get("requesterPhoneNumber"), "").replaceAll("-","");
                    orderDto.setShippingTel1(shippingTel1);

                    String shippingTel2 = Objects.toString(cancelRefundContent.get("requesterRealPhoneNumber"), "").replaceAll("-","");
                    orderDto.setShippingTel2(shippingTel2);

                    String shippingName = Objects.toString(cancelRefundContent.get("requesterName"), "");
                    orderDto.setShippingName(shippingName);

                    orderDto.setProductOrderId(productOrderId);

                    orderDto.setOrderId(vendorItemId);
                    orderDto.setOptionId(Long.valueOf(vendorItemId));

                    ///
                    String productOption = Objects.toString(returnItem.get("vendorItemName"), "");//등록옵션명
                    orderDto.setProductOption(productOption);


                    String originalProductId = Objects.toString(returnItem.get("sellerProductId"), "");//등록상품ID
                    orderDto.setOriginProductNo(Long.valueOf(originalProductId));


                    Map<String, Object > productParam = new HashMap<>();
                    productParam.put("id",originalProductId);
                    ProductDto productDto = productMapper.fetchProduct(productParam);
                    String productName = null;
                    if(productDto!=null){
                        productName = Objects.toString(productDto.getProductName(), ""); //등록상품명
                    }
                    orderDto.setProductName(productName);


                    //부분취소 없이 진행.
                    //취소카운트 처리  부분취소가 되는지 확인 purchaseCount 가 전체 구매수량인지 (부분취소 없는듯)
                    int shippingCount = Integer.parseInt(Objects.toString(returnItem.get("purchaseCount"), "0")); //주문시 item의 구매 수량
                    int holdCountForCancel = 0; //취소가 되어 환불 예정이 수량
                    int cancelCount = Integer.parseInt(Objects.toString(returnItem.get("cancelCount"), "0")); //취소가 확정된 수량
                    orderDto.setQuantity(shippingCount - (holdCountForCancel + cancelCount )); // 발주수량
                    orderDto.setAllQuantity(shippingCount); //주문수량
                    orderDto.setCancelQuantity(holdCountForCancel + cancelCount );//취소수량




                }else{
                    //취소카운트 처리  부분취소가 되는지 확인 purchaseCount 가 전체 구매수량인지 (부분취소 없는듯)
                    int shippingCount = Integer.parseInt(Objects.toString(returnItem.get("purchaseCount"), "0")); //주문시 item의 구매 수량
                    int holdCountForCancel = 0; //취소가 되어 환불 예정이 수량
                    int cancelCount = Integer.parseInt(Objects.toString(returnItem.get("cancelCount"), "0")); //취소가 확정된 수량
                    orderDto.setQuantity(shippingCount - (holdCountForCancel + cancelCount )); // 발주수량
                    orderDto.setAllQuantity(shippingCount); //주문수량
                    orderDto.setCancelQuantity(holdCountForCancel + cancelCount );//취소수량
                }


                String claimType = Objects.toString(cancelRefundContent.get("receiptType"), "");
                String claimStatus = Objects.toString(cancelRefundContent.get("receiptStatus"), "");


                //발송 상태 0: 미발송, 1: 발신 , 2:부분실패, 3:발송실패, 4: 취소, 5 : 취소요청 ,6: 반품, 7: 반품요청 99: 이전으로 돌리기
                if(!claimType.equals("") && claimType.equals("CANCEL")){ //취소상태
                    if(!claimStatus.equals("")){
                        if(claimStatus.equals("RELEASE_STOP_UNCHECKED")){//취소 요청(취소전)
                            orderDto.setSendStatus("5");
                        }else if(claimStatus.equals("REQUEST_COUPANG_CHECK")){//취소 처리중
                            orderDto.setSendStatus("5");
                        }else if(claimStatus.equals("RETURNS_COMPLETED")){//취소 완료
                            orderDto.setSendStatus("4");
                        }else{//취소 철회
                            orderDto.setSendStatus("99");
                        }
                    }
                }

                if(!claimType.equals("") && claimType.equals("RETURN")){//반품상태
                    if(!claimStatus.equals("")){
                        if(claimStatus.equals("RETURNS_UNCHECKED")){//반품 요청
                            orderDto.setSendStatus("7");
                        }else if(claimStatus.equals("REQUEST_COUPANG_CHECK")){//반품 처리중
                            orderDto.setSendStatus("7");
                        }else if(claimStatus.equals("RETURNS_COMPLETED")){//반품 완료
                            orderDto.setSendStatus("6");
                        }else{//반품 철회
                            orderDto.setSendStatus("99");
                        }
                    }
                }

                //부분취소
                if(!orderDto.getSendStatus().equals("99") && orderDto.getQuantity()>0){
                    orderDto.setSendStatus("8");
                }

                orderDto.setInsertFlag(true);
                orderDto.setStoreId(storeDto.getId());
                orderDtoList.add(orderDto);

            }

        }


        return orderDtoList;
    }






    /**
     * 30분 전 날짜를 ISO-8601 형식으로 반환
     * @return yyyy-MM-ddTHH:mm%2B09:00 형식의 문자열
     */
    private String getCreatedAtFrom() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime thirtyMinutesAgo = now.minusMinutes(DIFF_TIME);
        String isoString = thirtyMinutesAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmXXX"));
        return isoString.replace("+","%2B");
    }

    /**
     * 현재 시간을 ISO-8601 형식으로 반환
     * @return yyyy-MM-ddTHH:mm%2B09:00 형식의 문자열
     */
    private String getCreatedAtTo() {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmXXX")).replace("+","%2B");
    }


    /**
     * 30분 전 날짜를 ISO-8601 형식으로 반환
     * @return yyyy-MM-ddTHH:mm 형식의 문자열
     */
    private String getCancelCreatedAtFrom() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime thirtyMinutesAgo = now.minusMinutes(DIFF_TIME);
        String isoString = thirtyMinutesAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        return isoString;
    }

    /**
     * 현재 시간을 ISO-8601 형식으로 반환
     * @return yyyy-MM-ddTHH:mm 형식의 문자열
     */
    private String getCancelCreatedAtTo() {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }
}
