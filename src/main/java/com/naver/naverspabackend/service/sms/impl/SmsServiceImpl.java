package com.naver.naverspabackend.service.sms.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.MailMapper;
import com.naver.naverspabackend.mybatis.mapper.SmsMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.sms.SmsService;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.PagingUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Value(value = "${sms.api.domain}")
    private String smsDomain;


    @Value(value = "${sms.api.mmsSendUrl}")
    private String mmsSendUrl;

    @Value(value = "${sms.api.smsSendUrl}")
    private String smsSendUrl;


    @Autowired
    private SmsMapper smsMapper;

    @Autowired
    private MailMapper mailMapper;


    @Value("${testmode}")
    private String testMode;

    @Value("${testTel}")
    private String testTel;


    /**
     * SMS 본문	255자	90바이트(한글 45자, 영문 90자)
     * MMS 제목	120자	40바이트(한글 20자, 영문 40자)
     * MMS 본문	4,000자	2,000바이트(한글 1,000자, 영문 2,000자)
     *
     * 표준: 90바이트, 최대: 255자
     * @param searchMap
     * @param sendPhone
     * @return
     * @throws Exception
     */
    @Override
    public int insertSms(Map searchMap, StoreDto storeDto) throws Exception {
        Map<String, Object> recipientInfoMap = new HashMap<String, Object>();
        recipientInfoMap.put("body", searchMap.get("smsBody"));

        String[] phone = null;
        if("true".equals(testMode)){
            phone = new String[]{testTel};
        }else{
            phone = searchMap.get("shippingTel1").toString().split(",");
        }
        List<Map<String, Object>> recipientList = new ArrayList<>();

        int cnt = 0;
        for (int i=0; i < phone.length; i ++) {
            if (phone[i] != null && phone[i].length() >= 10) {
                Map<String, Object> recipientInfo = new HashMap<>();
                recipientInfo.put("recipientNo",  phone[i]);
                recipientList.add(recipientInfo);
            }
        }
        recipientInfoMap = buildRequestBody(recipientInfoMap,recipientList,storeDto.getSendPhone() );
        try {
            // 메세지 발송
            Map<String, String> paramHeader = new HashMap<>();
            paramHeader.put("X-Secret-Key", storeDto.getNhnSmsSecretKey());

            Map<String, Object> pathParam = new HashMap<>();
            pathParam.put("appKey",  storeDto.getNhnSmsAppKey());

            String post = ApiUtil.post(smsDomain + smsSendUrl, paramHeader,  recipientInfoMap, pathParam, okhttp3.MediaType.parse("application/json; charset=UTF-8"));

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> res = objectMapper.readValue(post, new TypeReference<Map<String, Object>>() {});



            recipientInfoMap.put("storeId", searchMap.get("storeId"));
            recipientInfoMap.put("originProductNo", searchMap.get("originProductNo"));
            recipientInfoMap.put("optionId", searchMap.get("optionId"));


            cnt = insertMMsResponse(res,recipientInfoMap, "SMS",storeDto );
        }catch (Exception e){
            throw e;
        }
        return cnt;
    }

    /**
     * title	String	120	    O	제목
     * body	    String	4000	O	본문
     * @param searchMap
     * @param sendPhone
     * @return
     * @throws Exception
     */
    @Override
    public int insertMms(Map searchMap, StoreDto storeDto) throws Exception {
        Map<String, Object> recipientInfoMap = new HashMap<String, Object>();
        recipientInfoMap.put("title", searchMap.get("title"));
        recipientInfoMap.put("body", searchMap.get("body"));

        String[] phone = null;
        if("true".equals(testMode)){
            phone = new String[]{testTel};
        }else{
            phone = searchMap.get("shippingTel1").toString().split(",");
        }

        List<Map<String, Object>> recipientList = new ArrayList<>();

        int cnt = 0;
        for (int i=0; i < phone.length; i ++) {
            if (phone[i] != null && phone[i].length() >= 10) {
                Map<String, Object> recipientInfo = new HashMap<>();
                recipientInfo.put("recipientNo",  phone[i]);
                recipientList.add(recipientInfo);
            }
        }
        recipientInfoMap = buildRequestBody(recipientInfoMap,recipientList, storeDto.getSendPhone());
        try {
            // 메세지 발송
            Map<String, String> paramHeader = new HashMap<>();
            paramHeader.put("X-Secret-Key", storeDto.getNhnSmsSecretKey());

            Map<String, Object> pathParam = new HashMap<>();
            pathParam.put("appKey", storeDto.getNhnSmsAppKey());

            String post = ApiUtil.post(smsDomain + mmsSendUrl, paramHeader,  recipientInfoMap, pathParam, okhttp3.MediaType.parse("application/json; charset=UTF-8"));

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> res = objectMapper.readValue(post, new TypeReference<Map<String, Object>>() {});


            recipientInfoMap.put("storeId", searchMap.get("storeId"));
            recipientInfoMap.put("originProductNo", searchMap.get("originProductNo"));
            recipientInfoMap.put("optionId", searchMap.get("optionId"));


            cnt = insertMMsResponse(res,recipientInfoMap, "MMS", storeDto);
        }catch (Exception e){
            throw e;
        }
        return cnt;
    }

    @Override
    public ApiResult<?> fetchSmsSendList(Map<String, Object> paramMap, PagingUtil pagingUtil) {
        CommonUtil.setPageIntoMap(paramMap, pagingUtil, smsMapper.adSelectSmsListCnt(paramMap));
        return ApiResult.succeed(smsMapper.adSelectSmsList(paramMap), pagingUtil);
    }

    @Override
    public ApiResult<?> fetchMailSendList(Map<String, Object> paramMap, PagingUtil pagingUtil) {
        CommonUtil.setPageIntoMap(paramMap, pagingUtil, mailMapper.adSelectMailListCnt(paramMap));
        return ApiResult.succeed(mailMapper.adSelectMailList(paramMap), pagingUtil);
    }

    public Map<String,Object> buildRequestBody(Map<String,Object> recipientInfoMap, List<Map<String,Object>> recipientList, String sendPhone){
        // 수신자 정보 list
        recipientInfoMap.put("recipientList", recipientList);
        // 발신 프로필 키
        recipientInfoMap.put("sendNo", sendPhone);
        return recipientInfoMap;
    }

    /**
     * MMS 또는 SMS 결과 테이블에 저장함
     * @param recipientInfoMap
     * @param type
     * @return
     * @throws IOException
     */
    public int insertMMsResponse(Map<String, Object> res, Map<String, Object> recipientInfoMap, String type, StoreDto storeDto) throws IOException {

        String sessionSender = "관리자";

        Map<String, Object> resHeader = (Map<String, Object>) res.get("header");
        Map<String, Object> resBody = (Map<String, Object>) res.get("body");

        String resultMessage = (String) resHeader.get("resultMessage");
        int resultCode = (Integer) resHeader.get("resultCode");
        boolean isSuccessful = (Boolean) resHeader.get("isSuccessful");

        int cnt = 0;
        if("SUCCESS".equals(resultMessage) && 0 == resultCode && isSuccessful){
            Map<String, Object> resBodyData = (Map<String, Object>) resBody.get("data");
            List<LinkedHashMap> sendResults = (ArrayList) resBodyData.get("sendResultList");
            Map<String, Object> smsLogMap = new HashMap<String, Object>();
            for (LinkedHashMap sendResult : sendResults) {
                smsLogMap.put("header_resultMessage", resultMessage);
                smsLogMap.put("header_resultCode", resultCode);
                smsLogMap.put("header_isSuccessful", isSuccessful);
                smsLogMap.put("message_request_id", resBodyData.get("requestId"));
                smsLogMap.put("message_status_code", resBodyData.get("statusCode"));
                smsLogMap.put("message_recipientSeq", sendResult.get("recipientSeq"));
                smsLogMap.put("message_recipientNo", sendResult.get("recipientNo"));
                smsLogMap.put("message_resultMessage", sendResult.get("resultMessage"));
                smsLogMap.put("message_resultCode", sendResult.get("resultCode"));
                smsLogMap.put("template_contents", recipientInfoMap.get("body"));
                smsLogMap.put("sender", sessionSender);
                smsLogMap.put("senderNo", storeDto.getSendPhone());
                smsLogMap.put("type", type);
                smsLogMap.put("template_title", recipientInfoMap.get("title"));


                smsLogMap.put("storeId", recipientInfoMap.get("storeId"));
                smsLogMap.put("originProductNo", recipientInfoMap.get("originProductNo"));
                smsLogMap.put("optionId", recipientInfoMap.get("optionId"));

                if(sendResult.get("resultCode")!=null && sendResult.get("resultCode").toString().equals("0")){
                    cnt++;
                }

                smsMapper.insertSmsResponse(smsLogMap);
            }
        }
        return cnt;
    }

}
