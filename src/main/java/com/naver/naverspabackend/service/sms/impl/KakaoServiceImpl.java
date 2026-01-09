package com.naver.naverspabackend.service.sms.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.naver.naverspabackend.dto.KakaoContentsDto;
import com.naver.naverspabackend.dto.KakaoDto;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.KakaoContentsMapper;
import com.naver.naverspabackend.mybatis.mapper.KakaoMsgMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.sms.KakaoService;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.PagingUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KakaoServiceImpl implements KakaoService {

    @Value(value="${kakao.api.domain}")
    private String kakaoDomain;



    @Value(value="${kakao.api.messagesUrl}")
    private String kakaoMessagesUrl;

    @Value(value="${kakao.api.templateUrl}")
    private String kakaoTemplateUrl;

    @Autowired
    private KakaoMsgMapper kakaoMsgMapper;

    @Autowired
    private KakaoContentsMapper kakaoContentsMapper;

    @Value("${testmode}")
    private String testMode;

    @Value("${testTel}")
    private String testTel;







    @Override
    public int requestSendKakaoMsg(Map<String, Object> kakaoParameters, String templateKey, StoreDto storeDto, OrderDto orderDto,String esimFlag,String kakaoResendFlag, boolean retrans, String... noList) throws Exception {

        if("true".equals(testMode)){
            noList = new String[]{testTel};
        }
        try{
            Gson gson = new Gson();
            if(!retrans){
                KakaoContentsDto kakaoContentsDto = new KakaoContentsDto();
                kakaoContentsDto.setKakaoParameter(gson.toJson(kakaoParameters));
                kakaoContentsDto.setKakaoTemplateKey(templateKey);
                kakaoContentsDto.setEsimFlag(esimFlag);
                kakaoContentsDto.setStoreId(storeDto.getId());
                kakaoContentsDto.setOrderId(orderDto.getId());
                kakaoContentsMapper.insertKakaoContents(kakaoContentsDto);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        kakaoParameters.put("iccid", Objects.toString(kakaoParameters.get("iccid"), ""));
        kakaoParameters.put("ipActivityCode", Objects.toString(kakaoParameters.get("activation_code"), ""));
        kakaoParameters.put("usageUrl", Objects.toString(kakaoParameters.get("usageUrl"), ""));
        kakaoParameters.put("eSimResetInfo", Objects.toString(kakaoParameters.get("eSimResetInfo"), ""));
        kakaoParameters.put("eSimMResetInfo", Objects.toString(kakaoParameters.get("eSimMResetInfo"), ""));
        kakaoParameters.put("eSimChargeInfo", Objects.toString(kakaoParameters.get("eSimChargeInfo"), ""));
        kakaoParameters.put("eSimMChargeInfo", Objects.toString(kakaoParameters.get("eSimMChargeInfo"), ""));
        kakaoParameters.put("eSimApnInfo", Objects.toString(kakaoParameters.get("eSimApnInfo"), ""));
        kakaoParameters.put("eSimMApnInfo", Objects.toString(kakaoParameters.get("eSimMApnInfo"), ""));






        kakaoParameters.put("usage2Url", Objects.toString(kakaoParameters.get("usage2Url"), ""));
        /* 카카오 발송시, $앞에 \ 가들어가는 증상이 발생 Matcher.quoteReplacement때문으로 해석됨. 제거 */
        try{
            kakaoParameters.put("gaActivityCode", Objects.toString(kakaoParameters.get("lpaCode"), ""));
        } catch (Exception e) {
            kakaoParameters.put("gaActivityCode", Matcher.quoteReplacement(Objects.toString(kakaoParameters.get("lpaCode"), "")));
        }
        HashMap<String, Object> copyKakaoParameters = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : kakaoParameters.entrySet()) {
            if(!entry.getKey().equals("ios_esim_install_link") &&
                    !entry.getKey().equals("qrcode") &&
                    !entry.getKey().equals("lpa_str") &&
                    !entry.getKey().equals("operator_iccids") &&
                    !entry.getKey().equals("device_ids") &&
                    !entry.getKey().equals("pin_puk") &&
                    !entry.getKey().equals("sharing") &&
                    !entry.getKey().equals("is_roaming") &&
                    !entry.getKey().equals("apn") &&
                    !entry.getKey().equals("ios_esim_install_link") &&
                    !entry.getKey().equals("emailSubject") &&
                    !entry.getKey().equals("emailContents"))
            copyKakaoParameters.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        // 수신자 정보 list
        List<Map<String, Object>> recipientList = new ArrayList<Map<String, Object>>();

        HashMap<String, Object> resendParameter = new HashMap<String, Object>();
        resendParameter.put("isResend",true);
        for (String no : noList) {
            Map<String, Object> recipientInfoMap = new HashMap<String, Object>();
            recipientInfoMap.put("recipientNo", no);
            recipientInfoMap.put("templateParameter", copyKakaoParameters);

            if(kakaoResendFlag!=null && kakaoResendFlag.equals("Y")){
                recipientInfoMap.put("resendParameter", resendParameter);
            }
            recipientList.add(recipientInfoMap);
        }

        Map<String, Object> body = buildRequestBody(recipientList, templateKey,storeDto);
        int cnt = 0;

        Map<String, Object> res = new HashMap<>();
        try {
            // 메세지 발송
            Map<String, String> paramHeader = new HashMap<>();
            paramHeader.put("X-Secret-Key", storeDto.getNhnKakaoSecretKey());

            Map<String, Object> pathParam = new HashMap<>();
            pathParam.put("appkey", storeDto.getNhnKakaoAppKey());

            String post = ApiUtil.post(kakaoDomain + kakaoMessagesUrl, paramHeader,  body, pathParam, okhttp3.MediaType.parse("application/json; charset=UTF-8"));
            ObjectMapper objectMapper = new ObjectMapper();
            res = objectMapper.readValue(post, new TypeReference<Map<String, Object>>() {
            });
            cnt = insertKakaoMsgLog(res, templateKey, copyKakaoParameters);
        }catch (Exception e){
            throw e;
        }

        return cnt;
    }

    @Override
    public ApiResult<List<KakaoDto>> fetchKakaoSendList(Map<String, Object> paramMap, PagingUtil pagingUtil) {
        CommonUtil.setPageIntoMap(paramMap, pagingUtil, kakaoMsgMapper.adSelectKakaoListCnt(paramMap));
        return ApiResult.succeed(kakaoMsgMapper.adSelectKakaoList(paramMap), pagingUtil);
    }

    @Override
    public ApiResult<?> fetchTemplateList(StoreDto storeDto) {

        Map<String, Object> pathMap = new HashMap<>();
        pathMap.put("appkey", storeDto.getNhnKakaoAppKey());
        pathMap.put("senderKey", storeDto.getNhnKakaoSenderKey());

        String url = ApiUtil.buildPathParameter(kakaoDomain + kakaoTemplateUrl + "?pageSize=1000", pathMap);

        Map<String, Object> result = new HashMap<>();
        try {
            // 메세지 발송
            Map<String, String> paramHeader = new HashMap<>();
            paramHeader.put("X-Secret-Key", storeDto.getNhnKakaoSecretKey());


            String post = ApiUtil.get(url, paramHeader);
            ObjectMapper objectMapper = new ObjectMapper();
            result = objectMapper.readValue(post, new TypeReference<Map<String, Object>>() {
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        return ApiResult.succeed(result, null);
    }

    public Map<String,Object> buildRequestBody(List<Map<String,Object>> recipientList, String templateKey,  StoreDto storeDto){
        Map<String, Object> requestBody = new HashMap<String, Object>();
        // 수신자 정보 list
        requestBody.put("recipientList", recipientList);
        // 발신 프로필 키
        requestBody.put("senderKey", storeDto.getNhnKakaoSenderKey());
        // 발신 템플릿 코드
        requestBody.put("templateCode", templateKey);

        return requestBody;
    }

    int insertKakaoMsgLog(Map<String, Object> res, String templateKey, Map<String, Object> kakaoParameters) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> resHeader = (Map<String, Object>) res.get("header");
        Map<String, Object> resBody = (Map<String, Object>) res.get("message");

        String resultMessage = (String) resHeader.get("resultMessage");
        int resultCode = (Integer) resHeader.get("resultCode");
        boolean isSuccessful = (Boolean) resHeader.get("isSuccessful");
        int cnt = 0;

        if("success".equals(resultMessage) && 0 == resultCode && isSuccessful){
            List<LinkedHashMap> sendResults = (ArrayList) resBody.get("sendResults");
            Map<String, Object> kakaoLogParamMap = new HashMap();
            for (LinkedHashMap sendResult : sendResults) {
                kakaoLogParamMap.put("requestId", resBody.get("requestId"));
                kakaoLogParamMap.put("header_resultMessage", resultMessage);
                kakaoLogParamMap.put("header_resultCode", resultCode);
                kakaoLogParamMap.put("header_isSuccessful", isSuccessful);
                kakaoLogParamMap.put("message_recipientSeq", sendResult.get("recipientSeq"));
                kakaoLogParamMap.put("message_recipientNo", sendResult.get("recipientNo"));
                kakaoLogParamMap.put("message_resultCode", sendResult.get("resultCode"));
                kakaoLogParamMap.put("message_resultMessage", sendResult.get("resultMessage"));
                kakaoLogParamMap.put("templateKey", templateKey);


                kakaoLogParamMap.put("storeId", kakaoParameters.get("storeId"));
                kakaoLogParamMap.put("originProductNo", kakaoParameters.get("originProductNo"));
                kakaoLogParamMap.put("optionId", kakaoParameters.get("optionId"));
                if(sendResult.get("resultCode")!=null && sendResult.get("resultCode").toString().equals("0")){
                    cnt++;
                }
                kakaoMsgMapper.insertKakaoLog(kakaoLogParamMap);
            }
        }
        return cnt;
    }

}
