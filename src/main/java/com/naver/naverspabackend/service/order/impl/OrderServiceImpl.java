package com.naver.naverspabackend.service.order.impl;

import com.google.gson.Gson;
import com.naver.naverspabackend.dto.*;
import com.naver.naverspabackend.exception.CustomException;
import com.naver.naverspabackend.mybatis.mapper.EsimApiIngStepLogsMapper;
import com.naver.naverspabackend.mybatis.mapper.KakaoContentsMapper;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.order.OrderService;
import com.naver.naverspabackend.service.payup.PayUpService;
import com.naver.naverspabackend.service.sms.KakaoService;
import com.naver.naverspabackend.service.sms.MailService;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.service.topupOrder.TopupOrderService;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.PagingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MailService mailService;


    @Autowired
    private TopupOrderService topupOrderService;


    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private KakaoContentsMapper kakaoContentsMapper;

    @Autowired
    private EsimApiIngStepLogsMapper esimApiIngStepLogsMapper;
    @Autowired
    private KakaoService kakaoService;


    @Value("${payApp.kakaoSuccessKey}")
    private String kakaoSuccessKey;



    @Value("${payApp.kakaoFailKey}")
    private String kakaoFailKey;

    @Value("${payApp.kakaoFail2Key}")
    private String kakaoFail2Key;


    @Value("${payup.merchantId}")
    private String merchantId;

    @Value("${payup.apiKey}")
    private String apiKey;

    @Value("${payup.baseUrl}")
    private String baseUrl;

    @Value("${payup.authTokenURI}")
    private String authTokenURI;


    @Value("${payup.cancelURI}")
    private String cancelURI;

    @Override
    public ApiResult<List<OrderDto>> fetchOrderList(Map<String, Object> map, PagingUtil pagingUtil) {
        CommonUtil.setPageIntoMap(map, pagingUtil, orderMapper.adSelectOrderListCnt(map));

        List<OrderDto> orderDtoList = orderMapper.adSelectOrderList(map);
        for(OrderDto orderDto : orderDtoList){
            try{orderDto.setEsimApiIngStepLogsDtoList(esimApiIngStepLogsMapper.adSelectEsimApiIngStepLogsList(orderDto.getId()));}catch (Exception e){}
            try{orderDto.setKakaoContentsDtoList(kakaoContentsMapper.adSelectKakaContentsList(orderDto.getId()));}catch (Exception e){}
            try{orderDto.setTransMail(mailService.formatStringToEmail(orderDto.getShippingMemo()));}catch (Exception e){}
        }
        return ApiResult.succeed(orderDtoList, pagingUtil);
    }


    @Override
    public ApiResult<?> fetchStatic(Map<String, Object> map) {
        return ApiResult.succeed(orderMapper.adSelectOrderStatic(map),null);
    }

    @Override
    public List<OrderDto> fetchOrderListForExcel(Map<String, Object> map) {
        return orderMapper.fetchOrderListForExcel(map);
    }

    @Override
    public void updateWordMoveItem(Map<String, Object> item) {
        OrderWorldmoveEsimDto orderWorldmoveEsimDto = new OrderWorldmoveEsimDto();
        if(item.get("qrcodeType").toString().equals("0")){
            orderWorldmoveEsimDto.setEsimType("image");
        }
        if(item.get("qrcodeType").toString().equals("1")){
            orderWorldmoveEsimDto.setEsimType("text");
        }
        orderWorldmoveEsimDto.setEsimQrText(item.get("qrcode").toString());
        orderWorldmoveEsimDto.setEsimIccid(item.get("iccid").toString());

        OrderDto orderDto = new OrderDto();
        orderDto.setEsimIccid(item.get("iccid").toString());
        OrderDto result = orderMapper.selectOrderWithEsimIccid(orderDto);

        orderWorldmoveEsimDto.setOrderId(result.getId());

        if(item.get("apnExplain")!=null)
            orderWorldmoveEsimDto.setApnExplain(item.get("apnExplain").toString());

        orderMapper.insertOrderWorldmoveEsim(orderWorldmoveEsimDto);
    }


    @Override
    public ResponseEntity<Map<String, String>> updateTugeItem(Map<String, Object> item) {
        try{
            HashMap<String,Object> data = (HashMap<String, Object>) item.get("data");
            int eventType = 1;
            try{
                eventType = Integer.parseInt(data.get("eventType").toString());
            }catch (Exception e){
                e.printStackTrace();
            }
            if(eventType==2){
                //충전리턴
                HashMap<String,Object> orderInfo = (HashMap<String, Object>) data.get("orderInfo");
                TopupOrderDto param = new TopupOrderDto();
                param.setId(Long.parseLong(orderInfo.get("channelOrderNo").toString().replace("renewOrderNo_","")));
                TopupOrderDto topupOrderDto = topupOrderService.findById(param);
                Map<String, Object> storeParam = new HashMap<>();
                storeParam.put("id",topupOrderDto.getStoreId());
                StoreDto storeDto = storeService.findById(storeParam);
                Map<String, Object> orderParam = new HashMap<>();
                orderParam.put("id",topupOrderDto.getOrderId());
                OrderDto orderDto = orderMapper.fetchOrderOnly(orderParam);

                if(orderDto == null || storeDto == null){
                    Map<String, String> response = new HashMap<>();
                    response.put("code", "1");
                    response.put("msg", item.get("msg").toString());
                    return ResponseEntity.ok(response);
                }
                Map<String, Object> kakaoParameters = new HashMap<>();
                kakaoParameters.put("orderRealName", Objects.toString(topupOrderDto.getProductOption(), ""));
                kakaoParameters.put("ordererName",Objects.toString(topupOrderDto.getShippingName(), ""));

                if(item.get("code").toString().equals("0000")){
                    //충전완료
                    topupOrderDto.setTopupStatus(1);
                    topupOrderService.updateTopupStatus(topupOrderDto);
                    try{
                        kakaoService.requestSendKakaoMsg(kakaoParameters, kakaoSuccessKey,storeDto,orderDto, "N", "Y",false, topupOrderDto.getShippingTel());
                    }catch (Exception e){
                    }

                    OrderRenewTugeEsimDto orderRenewTugeEsimDto = new OrderRenewTugeEsimDto();

                    orderRenewTugeEsimDto.setOrderId(orderDto.getId());
                    orderRenewTugeEsimDto.setTopupOrderId(topupOrderDto.getId());

                    orderRenewTugeEsimDto.setOrderNo(orderInfo.get("orderNo").toString());
                    if(orderInfo.get("iccid")!=null)orderRenewTugeEsimDto.setIccid(orderInfo.get("iccid").toString());
                    if(orderInfo.get("qrCode")!=null)orderRenewTugeEsimDto.setQrCode(orderInfo.get("qrCode").toString());
                    if(orderInfo.get("channelOrderNo")!=null)orderRenewTugeEsimDto.setChannelOrderNo(orderInfo.get("channelOrderNo").toString());
                    if(orderInfo.get("imsi")!=null)orderRenewTugeEsimDto.setImsi(orderInfo.get("imsi").toString());
                    if(orderInfo.get("msisdn")!=null)orderRenewTugeEsimDto.setMsisdn(orderInfo.get("msisdn").toString());

                    if(orderInfo.get("activatedStartTime")!=null){
                        // ISO 8601 문자열을 한국 시간 객체로 바로 변환
                        ZonedDateTime activatedStartTime = ZonedDateTime.parse(orderInfo.get("activatedStartTime").toString()).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                        // DB 저장용 포맷 (2026-12-09 23:26:03)
                        orderRenewTugeEsimDto.setActivatedStartTime(activatedStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));;
                    }
                    if(orderInfo.get("activatedEndTime")!=null){
                        // ISO 8601 문자열을 한국 시간 객체로 바로 변환
                        ZonedDateTime activatedEndTime = ZonedDateTime.parse(orderInfo.get("activatedEndTime").toString()).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                        // DB 저장용 포맷 (2026-12-09 23:26:03)
                        orderRenewTugeEsimDto.setActivatedEndTime(activatedEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));;
                    }
                    if(orderInfo.get("latestActivationTime")!=null){
                        // ISO 8601 문자열을 한국 시간 객체로 바로 변환
                        ZonedDateTime latestActivationTime = ZonedDateTime.parse(orderInfo.get("latestActivationTime").toString()).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                        // DB 저장용 포맷 (2026-12-09 23:26:03)
                        orderRenewTugeEsimDto.setLatestActivationTime(latestActivationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));;
                    }
                    if(orderInfo.get("renewExpirationTime")!=null){
                        // ISO 8601 문자열을 한국 시간 객체로 바로 변환
                        ZonedDateTime renewExpirationTime = ZonedDateTime.parse(orderInfo.get("renewExpirationTime").toString()).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                        // DB 저장용 포맷 (2026-12-09 23:26:03)
                        orderRenewTugeEsimDto.setRenewExpirationTime(renewExpirationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));;
                    }
                    orderMapper.insertOrderRenewTugeEsim(orderRenewTugeEsimDto);
                    Map<String, String> response = new HashMap<>();
                    response.put("code", item.get("code").toString());
                    response.put("msg", item.get("msg").toString());
                    return ResponseEntity.ok(response);
                }else if(item.get("code").toString().equals("4010") || item.get("code").toString().equals("4000") || item.get("code").toString().equals("4004")){
                    //충전실패
                    topupOrderDto.setTopupStatus(2);
                    topupOrderService.updateTopupStatus(topupOrderDto);


                    String paymentRefundResult =  refundPayment(topupOrderDto.getTopupTransactionId() );

                    if(paymentRefundResult==null || !paymentRefundResult.equals("SUCCESS")) {//결제실패
                        topupOrderDto.setPaymentStatus(3);
                        topupOrderService.updatePaypalStatus(topupOrderDto);
                        try{
                            System.out.println("충전 요청 중 오류가 발생했습니다. 다시 시도해주세요.");
                            kakaoService.requestSendKakaoMsg(kakaoParameters, kakaoFailKey,storeDto,orderDto, "N", "Y",false, topupOrderDto.getShippingTel());
                        }catch (Exception e){
                        }
                    }else{
                        topupOrderDto.setPaymentStatus(4);
                        topupOrderService.updatePaypalStatus(topupOrderDto);
                        try{
                            System.out.println("충전 요청 중 오류가 발생하여, 결제 취소를 하였으나 결제 취소에 실패하였습니다. 고객센터로 문의주세요.");
                            kakaoService.requestSendKakaoMsg(kakaoParameters, kakaoFail2Key,storeDto,orderDto, "N", "Y",false, topupOrderDto.getShippingTel());
                        }catch (Exception e){
                        }
                    }

                    Map<String, String> response = new HashMap<>();
                    response.put("code", "0000");
                    response.put("msg", "success");
                    return ResponseEntity.ok(response);
                }
            }else{
                if(item.get("code").toString().equals("0000")){
                    //이심리턴
                    HashMap<String,Object> orderInfo = (HashMap<String, Object>) data.get("orderInfo");
                    OrderTugeEsimDto orderTugeEsimDto = new OrderTugeEsimDto();

                    OrderDto orderDto = new OrderDto();
                    orderDto.setEsimApiRequestId(orderInfo.get("orderNo").toString());
                    OrderDto result = orderMapper.selectOrderWithEsimApiRequestId(orderDto);

                    // result가 null일 경우 1분 간격으로 최대 3번 재시도
                    if(result == null){
                        //TODO 이부분 나중에 TUGE에서 재 callback 완료되면 제거
                        int maxRetries = 3;
                        for(int i = 0; i < maxRetries; i++){
                            // 1분 대기
                            try {Thread.sleep(60000); } catch (InterruptedException e) {}
                            result = orderMapper.selectOrderWithEsimApiRequestId(orderDto);
                            if(result != null){
                                break; // 성공하면 루프 종료
                            }
                        }
                        // 3번 재시도 후에도 null이면 에러 응답
                        //TODO 제거 END

                        if(result == null){
                            Map<String, String> response = new HashMap<>();
                            response.put("code", "1");
                            response.put("msg", item.get("msg").toString());
                            return ResponseEntity.ok(response);
                        }
                    }
                    orderTugeEsimDto.setOrderId(result.getId());

                    orderTugeEsimDto.setOrderNo(orderInfo.get("orderNo").toString());
                    if(orderInfo.get("iccid")!=null)orderTugeEsimDto.setIccid(orderInfo.get("iccid").toString());
                    if(orderInfo.get("qrCode")!=null)orderTugeEsimDto.setQrCode(orderInfo.get("qrCode").toString());
                    if(orderInfo.get("channelOrderNo")!=null)orderTugeEsimDto.setChannelOrderNo(orderInfo.get("channelOrderNo").toString());
                    if(orderInfo.get("imsi")!=null)orderTugeEsimDto.setImsi(orderInfo.get("imsi").toString());
                    if(orderInfo.get("msisdn")!=null)orderTugeEsimDto.setMsisdn(orderInfo.get("msisdn").toString());

                    if(orderInfo.get("activatedStartTime")!=null){
                        // ISO 8601 문자열을 한국 시간 객체로 바로 변환
                        ZonedDateTime activatedStartTime = ZonedDateTime.parse(orderInfo.get("activatedStartTime").toString()).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                        // DB 저장용 포맷 (2026-12-09 23:26:03)
                        orderTugeEsimDto.setActivatedStartTime(activatedStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));;
                    }
                    if(orderInfo.get("activatedEndTime")!=null){
                        // ISO 8601 문자열을 한국 시간 객체로 바로 변환
                        ZonedDateTime activatedEndTime = ZonedDateTime.parse(orderInfo.get("activatedEndTime").toString()).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                        // DB 저장용 포맷 (2026-12-09 23:26:03)
                        orderTugeEsimDto.setActivatedEndTime(activatedEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));;
                    }
                    if(orderInfo.get("latestActivationTime")!=null){
                        // ISO 8601 문자열을 한국 시간 객체로 바로 변환
                        ZonedDateTime latestActivationTime = ZonedDateTime.parse(orderInfo.get("latestActivationTime").toString()).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                        // DB 저장용 포맷 (2026-12-09 23:26:03)
                        orderTugeEsimDto.setLatestActivationTime(latestActivationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));;
                    }
                    if(orderInfo.get("renewExpirationTime")!=null){
                        // ISO 8601 문자열을 한국 시간 객체로 바로 변환
                        ZonedDateTime renewExpirationTime = ZonedDateTime.parse(orderInfo.get("renewExpirationTime").toString()).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                        // DB 저장용 포맷 (2026-12-09 23:26:03)
                        orderTugeEsimDto.setRenewExpirationTime(renewExpirationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));;
                    }
                    orderMapper.insertOrderTugeEsim(orderTugeEsimDto);
                    Map<String, String> response = new HashMap<>();
                    response.put("code", item.get("code").toString());
                    response.put("msg", item.get("msg").toString());
                    return ResponseEntity.ok(response);

                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        Map<String, String> response = new HashMap<>();
        response.put("code", "1");
        response.put("msg", item.get("msg").toString());
        return ResponseEntity.ok(response);
    }




    @Override
    public OrderTugeEsimDto selecTugeItemWithIccid(OrderTugeEsimDto param) {
        return orderMapper.selecTugeItemWithIccid(param);
    }

    @Override
    public Map<String, String> insertReTransMailInfo(Map<String, Object> params) {
        Map<String, String> result = new HashMap<>();
        OrderRetransMailInfoDto orderRetransMailInfoDto = new OrderRetransMailInfoDto();

        orderRetransMailInfoDto.setMail(params.get("mail").toString());
        orderRetransMailInfoDto.setOrderId(params.get("orderId").toString());
        try{
            orderMapper.insertReTransMailInfo(orderRetransMailInfoDto);
        }catch (DuplicateKeyException e){
            result.put("result", "exist");
            return result;
        }
        result.put("result", "success");
        return result;
    }

    @Override
    public List<OrderRetransMailInfoDto> fetchRetransMailInfoDtoAll() {
        return orderMapper.fetchRetransMailInfoDtoAll();
    }

    @Override
    public void updateRetransMailInfoComfirm(OrderRetransMailInfoDto orderRetransMailInfoDto) {
        orderMapper.updateRetransMailInfoComfirm(orderRetransMailInfoDto);
    }

    @Override
    public OrderTugeEsimDto selectListOrderTugeEsimByOrderNo(OrderTugeEsimDto orderTugeEsimDtoParam) {
        return orderMapper.selectListOrderTugeEsimByOrderNo(orderTugeEsimDtoParam);
    }

    @Override
    public List<OrderRenewTugeEsimDto> selectListOrderRenewTugeEsimList(OrderRenewTugeEsimDto orderRenewTugeEsimParam) {
        return orderMapper.selectListOrderRenewTugeEsimList(orderRenewTugeEsimParam);
    }

    @Override
    public OrderRenewTugeEsimDto selectOrderRenewTugeEsimByOrderNo(OrderRenewTugeEsimDto orderRenewTugeEsimDtoParam) {

        return orderMapper.selectOrderRenewTugeEsimByOrderNo(orderRenewTugeEsimDtoParam);
    }

    @Override
    public ApiResult<Void> updateOrderMailReTrans(Map<String, Object> paramMap) throws Exception {

        OrderDto orderDto = orderMapper.findById(paramMap);
        Map<String, Object> selectStore = new HashMap<>();
        selectStore.put("id",orderDto.getStoreId());

        this.sendReEmailMessage(orderDto,paramMap.get("reEmail").toString());
        return ApiResult.succeed(null, null);
    }




    @Override
    public ApiResult<OrderDto> fetchOrder(Map<String, Object> map) {
        return ApiResult.succeed(orderMapper.fetchOrder(map), null);
    }

    @Override
    public OrderDto fetchOrderOnly(Map<String, Object> map) {
        return orderMapper.fetchOrderOnly(map);
    }


    @Override
    public ApiResult<Void> updateOrderStatus(Map<String, Object> paramMap) {
        if(paramMap.get("sendStatus").toString().equals("4")){
            paramMap.put("changeUnitPrice","-1000");
            paramMap.put("totalPaymentAmount","-1000");
            orderMapper.updateOrderStatus(paramMap);
        }else{
            OrderDto orderDto = orderMapper.adSelectOrder(paramMap);
            if(orderDto.getChangeUnitPrice()!=null && !orderDto.getChangeUnitPrice().equals("")){
                orderMapper.updateOrderStatus(paramMap);
            }else{

                orderMapper.updateOrderOnlyStatus(paramMap);
            }
        }
        return ApiResult.succeed(null, null);
    }




    public void sendReEmailMessage(OrderDto orderDto, String mail ) throws Exception {
        MailContentsDto mailContentsDto = new MailContentsDto();
        mailContentsDto.setOrderId(orderDto.getId());
        List<MailContentsDto> mailContentsDtoList = mailService.selectMailContentsList(mailContentsDto);

        for(MailContentsDto mailContentsDto1 : mailContentsDtoList){
            int perCnt = 0;
            String method = mailContentsDto1.getEsimYn().equals("Y")?"E-EMAIL":"EMAIL";
            Map<String,Object> param = new HashMap<>();
            param.put("id",mailContentsDto1.getStoreId());
            StoreDto storeDto = storeMapper.selectStoreDetail(param);
            Map<String, Object> sendMap = new HashMap<>();
            sendMap.put("emailContents",mailContentsDto1.getEmailContents());
            sendMap.put("emailSubject",mailContentsDto1.getEmailSubject());
            sendMap.put("storeId",mailContentsDto1.getStoreId());
            sendMap.put("originProductNo",mailContentsDto1.getOriginProductNo());
            sendMap.put("optionId",mailContentsDto1.getOptionId());
            try {
                perCnt = mailService.sendEmail(sendMap, storeDto,mail);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (perCnt > 0) {
                if(orderDto.getReSendMethod()!=null && !orderDto.getReSendMethod().equals("")){
                    orderDto.setReSendMethod(orderDto.getReSendMethod() + "," + method);
                }else{

                    orderDto.setReSendMethod(method);
                }
            } else {
                if(orderDto.getReFailMethod()!=null && !orderDto.getReFailMethod().equals("")){
                    orderDto.setReFailMethod(orderDto.getReFailMethod() + "," + method);
                }else{

                    orderDto.setReFailMethod(method);
                }
            }
        }

        orderMapper.updateOrderMethod(orderDto);



    }

    @Override
    public ApiResult<Void> updateOrderKakaoReTrans(Map<String, Object> paramMap) throws Exception {
        KakaoContentsDto kakaoContentsDto = kakaoContentsMapper.findById(paramMap);
        Gson gson = new Gson();
        Map<String, Object> sendMap = gson.fromJson(kakaoContentsDto.getKakaoParameter(),Map.class);
        Map<String,Object> storeParam = new HashMap<>();
        storeParam.put("id",kakaoContentsDto.getStoreId());
        StoreDto storeDto = storeMapper.selectStoreDetail(storeParam);
        int perCnt = kakaoService.requestSendKakaoMsg(sendMap, kakaoContentsDto.getKakaoTemplateKey(),storeDto,null,kakaoContentsDto.getEsimFlag(),null,true, paramMap.get("rePhone").toString());
        if(perCnt<1){
            throw new CustomException("카카오 재발송에 실패하였습니다.");
        }
        return ApiResult.succeed(null, null);
    }

    public String refundPayment(String transactionId) throws Exception {
        String accessToken = getAccessToken();
        if(accessToken==null){
            throw new Exception();
        }
        RestTemplate restTemplate = new RestTemplate();
        try {
            String url =  baseUrl  + cancelURI;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.add("Authorization",accessToken);

            Map<String,Object> jsonObject = new HashMap<>();
            jsonObject.put("transactionId",transactionId);
            jsonObject.put("merchantId",merchantId);
            jsonObject.put("cancelReason","충전실패");
            HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


            ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

            Map<String, Object> result = response.getBody();
            return  result.get("status").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getAccessToken(){
        RestTemplate restTemplate = new RestTemplate();
        try {
            String url =  baseUrl  + authTokenURI;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            Map<String,Object> jsonObject = new HashMap<>();
            jsonObject.put("merchantId",merchantId);
            jsonObject.put("apiKey",apiKey);
            HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


            ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

            Map<String, Object> result = response.getBody();
            if(result.get("status").toString().equals("SUCCESS")){
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                return  data.get("accessToken").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
