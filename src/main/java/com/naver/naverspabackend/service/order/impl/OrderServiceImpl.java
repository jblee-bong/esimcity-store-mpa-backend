package com.naver.naverspabackend.service.order.impl;

import com.google.gson.Gson;
import com.naver.naverspabackend.dto.*;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.exception.CustomException;
import com.naver.naverspabackend.mybatis.mapper.EsimApiIngStepLogsMapper;
import com.naver.naverspabackend.mybatis.mapper.KakaoContentsMapper;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.order.OrderService;
import com.naver.naverspabackend.service.sms.KakaoService;
import com.naver.naverspabackend.service.sms.MailService;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.PagingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MailService mailService;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private KakaoContentsMapper kakaoContentsMapper;

    @Autowired
    private EsimApiIngStepLogsMapper esimApiIngStepLogsMapper;
    @Autowired
    private KakaoService kakaoService;

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
            if(item.get("code").toString().equals("0000")){
                HashMap<String,Object> data = (HashMap<String, Object>) item.get("data");
                HashMap<String,Object> orderInfo = (HashMap<String, Object>) data.get("orderInfo");
                OrderTugeEsimDto orderTugeEsimDto = new OrderTugeEsimDto();

                OrderDto orderDto = new OrderDto();
                orderDto.setEsimApiRequestId(orderInfo.get("orderNo").toString());
                OrderDto result = orderMapper.selectOrderWithEsimApiRequestId(orderDto);
                if(result == null){
                    Map<String, String> response = new HashMap<>();
                    response.put("code", "1");
                    response.put("msg", item.get("msg").toString());
                    return ResponseEntity.ok(response);
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
            }else{
                Map<String, String> response = new HashMap<>();
                response.put("code", "1");
                response.put("msg", item.get("msg").toString());
                return ResponseEntity.ok(response);
            }
        }catch (Exception e){
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("code", "1");
            response.put("msg", item.get("msg").toString());
            return ResponseEntity.ok(response);
        }
    }

    @Override
    public OrderTugeEsimDto selecTugeItemWithIccid(OrderTugeEsimDto param) {
        return orderMapper.selecTugeItemWithIccid(param);
    }

    @Override
    public void insertReTransMailInfo(Map<String, Object> params) {
        OrderRetransMailInfoDto orderRetransMailInfoDto = new OrderRetransMailInfoDto();

        orderRetransMailInfoDto.setMail(params.get("mail").toString());
        orderRetransMailInfoDto.setOrderId(params.get("orderId").toString());
        orderMapper.insertReTransMailInfo(orderRetransMailInfoDto);
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

}
