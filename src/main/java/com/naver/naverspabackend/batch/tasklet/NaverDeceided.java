package com.naver.naverspabackend.batch.tasklet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.naver.naverspabackend.batch.writer.NaverWritter;
import com.naver.naverspabackend.dto.MailContentsDto;
import com.naver.naverspabackend.dto.MatchInfoDto;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.MatchInfoMapper;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.service.sms.KakaoService;
import com.naver.naverspabackend.service.sms.MailService;
import com.naver.naverspabackend.service.sms.SmsService;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 메인 DB에서 batch 관련 설정값을 불러와 ExcuteContext에 저장하는 Step
 *
 */

@Component
@Slf4j
public class NaverDeceided {

    @Autowired
    private NaverRedisRepository naverRedisRepository;

    @Value("${naver-api.base}")
    private String baseUrl;

    @Value("${naver-api.order-list-info}")
    private String orderListInfoUrl;

    @Autowired
    private SmsService smsService;

    @Autowired
    private KakaoService kakaoService;

    @Autowired
    private MailService mailService;

    // 검색할 diff 시간
    private final int DIFF_TIME = -1440;
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MatchInfoMapper matchInfoMapper;

    public void processNaverStore(StoreDto storeDto) {
        try {
            Gson gson = new Gson();
            Optional<NaverRedisToken> byStoreId = naverRedisRepository.findById(storeDto.getId());

            if (!byStoreId.isPresent()){
                return;
            }

            NaverRedisToken naverRedisToken = byStoreId.get();

            Map<String, String> headerMap = naverRedisToken.returnHeaderMap();

            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("lastChangedFrom", URLEncoder.encode(CommonUtil.naverFormatDate(DIFF_TIME, ChronoUnit.MINUTES), StandardCharsets.UTF_8.name()));

            paramMap.put("lastChangedType", "PURCHASE_DECIDED");

            Map<String, Object> resMap = new HashMap<>();

            try {
                resMap = requestOrderNoList(headerMap, paramMap);
            } catch (Exception e) {
                log.error(storeDto.getStoreName()+" "   +  e.getMessage());
                return;
            }


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

                for(Map<String,Object> orderMap:orderListMap){
                    OrderDto param = new OrderDto();
                    param.setProductOrderId(orderMap.get("productOrderId").toString());
                    param.setOrderId(orderMap.get("orderId").toString());

                    OrderDto orderDto = orderMapper.selectOrderWithProductOrderIdAndOrderId(param);

                    if(orderDto==null){
                        throw new Exception();
                    }
                    if(orderDto.getOrderDecidedStatus().equals("1")){
                        continue;
                    }

                    //먼저완료처리함 2번가면 안되기떄문에,
                    orderDto.setOrderDecidedStatus("1");

                    String lastChangedDate = Objects.toString(orderMap.get("lastChangedDate"), "");
                    if(!"".equals(lastChangedDate)){
                        orderDto.setOrderDecidedDate(CommonUtil.parseNaverFormatDate(lastChangedDate));
                    }
                    orderMapper.updateOrderDecided(orderDto);

                    String email = "";
                    try {
                        email = mailService.formatStringToEmail(Objects.toString(orderDto.getShippingMemo(), ""));
                    } catch (Exception e) {
                    }

                    MatchInfoDto matchInfoDto = matchInfoMapper.selectMatchInfoByOrder(orderDto);

                    if (matchInfoDto == null || matchInfoDto.getComfirmFlag() == null || matchInfoDto.getComfirmFlag().equals("N")) {
                        continue;
                    }

                    String sendMethod = matchInfoDto.getSendMethod();
                    String ordererTel = orderDto.getOrdererTel();
                    String shippingTel1 = orderDto.getShippingTel1();
                    String shippingName = orderDto.getShippingName();
                    Map<String, Object> sendMap = new HashMap<>();

                    sendMap.put("esimProductId", matchInfoDto.getEsimProductId());
                    sendMap.put("esimDescription", matchInfoDto.getEsimDescription());
                    sendMap.put("esimProductDays", matchInfoDto.getEsimProductDays());

                    sendMap.put("storeId", orderDto.getStoreId());
                    sendMap.put("originProductNo", orderDto.getOriginProductNo());
                    sendMap.put("optionId", orderDto.getOptionId());


                    sendMap.put("shippingName", shippingName);
                    sendMap.put("shippingTel1", shippingTel1);

                    sendMap.put("ordererTel", ordererTel);
                    sendMap.put("ordererName", orderDto.getOrdererName());
                    String productName = "";
                    if(orderDto.getOptionName2()!=null){
                        productName = orderDto.getOptionName2();
                    }
                    if(orderDto.getOptionName3()!=null && !productName.equals("")){
                        productName += " / " + orderDto.getOptionName3();
                    }else{
                        productName = orderDto.getOptionName3();
                    }
                    sendMap.put("productName", productName);
                    String orderRealName = "";
                    if(orderDto.getOptionName1()!=null){
                        orderRealName = orderDto.getOptionName1();
                        sendMap.put("optionName1", orderDto.getOptionName1());
                    }
                    if(orderDto.getOptionName2()!=null){
                        if(!orderRealName.equals(""))
                            orderRealName += " ";
                        orderRealName += orderDto.getOptionName2();
                        sendMap.put("optionName2", orderDto.getOptionName2());

                    }
                    if(orderDto.getOptionName3()!=null){
                        sendMap.put("optionName3", orderDto.getOptionName3());
                    }
                    if(orderDto.getOptionName4()!=null){
                        sendMap.put("optionName4", orderDto.getOptionName4());
                    }
                    sendMap.put("orderRealName", orderRealName);

                    sendMap.put("body", matchInfoDto.getBody());
                    sendMap.put("smsBody", matchInfoDto.getSmsBody());
                    sendMap.put("title", matchInfoDto.getTitle());

                    // mail
                    sendMap.put("emailContents", matchInfoDto.getMailContents());
                    sendMap.put("emailSubject", matchInfoDto.getMailTitle());
                    sendMap.put("shippingMemo", orderDto.getShippingMemo());

                    String[] sendMethods = sendMethod.split(",");

                    int totalCnt = 0;
                    String separator = "";
                    String failSeparator = "";
                    String sucMethod = "";
                    String failMethod = "";

                    for (String method : sendMethods) {
                        if(method.indexOf("COMFIRM-")==-1)
                            continue;
                        sucMethod += separator;
                        failMethod += failSeparator;

                        int perCnt = 0;
                        sendMap.put("body", CommonUtil.replaceVariableString(matchInfoDto.getBody(), orderDto, sendMap, false));
                        sendMap.put("smsBody", CommonUtil.replaceVariableString(matchInfoDto.getSmsBody(), orderDto, sendMap, false));
                        sendMap.put("title", CommonUtil.replaceVariableString(matchInfoDto.getTitle(), orderDto, sendMap, false));
                        sendMap.put("emailContents", CommonUtil.replaceVariableString(matchInfoDto.getMailContents(), orderDto, sendMap, false));
                        sendMap.put("emailSubject", CommonUtil.replaceVariableString(matchInfoDto.getMailTitle(), orderDto, sendMap, false));


                        if ("COMFIRM-SMS".equals(method)) {
                            try {
                                ArrayList<String> smsBodyList =  gson.fromJson(sendMap.get("smsBody").toString(),ArrayList.class);
                                for(String smsBody : smsBodyList){
                                    sendMap.put("smsBody",smsBody);
                                    perCnt = smsService.insertSms(sendMap,storeDto);
                                }
                            } catch (Exception e) {
                                log.error("sms 발송 오류 =======================", e);
                                e.printStackTrace();
                            }

                            totalCnt += perCnt;

                            if (perCnt > 0) {
                                sucMethod += method;
                            } else {
                                failMethod += method;
                            }

                        } else if ("COMFIRM-MMS".equals(method)) {
                            try {
                                ArrayList<String> titleList =  gson.fromJson(sendMap.get("title").toString(),ArrayList.class);
                                ArrayList<String> bodyList =  gson.fromJson(sendMap.get("body").toString(),ArrayList.class);
                                for(int k=0;k<titleList.size();k++){
                                    sendMap.put("title",titleList.get(k));
                                    sendMap.put("body",bodyList.get(k));
                                    perCnt = smsService.insertMms(sendMap,storeDto);
                                }
                            } catch (Exception e) {
                                log.error("mms 발송 오류 =======================", e);
                                e.printStackTrace();
                            }
                            totalCnt += perCnt;

                            if (perCnt > 0) {
                                sucMethod += method;
                            } else {
                                failMethod += method;

                            }
                        } else if ("COMFIRM-KAKAO".equals(method)) {
                            try {
                                ArrayList<String> kakaoTemplateKeyList =  gson.fromJson(matchInfoDto.getComfirmKakaoTemplateKey(),ArrayList.class);
                                for(String kakaoTemplateKey : kakaoTemplateKeyList){
                                    perCnt = kakaoService.requestSendKakaoMsg(sendMap, kakaoTemplateKey,storeDto,orderDto,matchInfoDto.getEsimFlag(),null,false, shippingTel1);
                                }
                            } catch (Exception e) {
                                log.error("kakao 발송 오류 =======================", e);
                                e.printStackTrace();
                            }
                            totalCnt += perCnt;

                            if (perCnt > 0) {
                                sucMethod += method;
                            } else {
                                failMethod += method;
                            }

                        } else if ("COMFIRM-EMAIL".equals(method)) {
                            try {

                                ArrayList<String> tilteList = gson.fromJson(Objects.toString(sendMap.get("emailSubject"), ""),ArrayList.class);
                                ArrayList<String> contentsList = gson.fromJson(Objects.toString(sendMap.get("emailContents"), ""),ArrayList.class);
                                for(int k=0;k<tilteList.size();k++){
                                    sendMap.put("emailSubject",tilteList.get(k));
                                    sendMap.put("emailContents",contentsList.get(k));
                                    perCnt = mailService.sendEmail(sendMap, storeDto,email);
                                }
                            } catch (Exception e) {
                                log.error("email 발송 오류 =======================", e);
                                e.printStackTrace();
                            }
                            totalCnt += perCnt;

                            if (perCnt > 0) {
                                sucMethod += method;
                            } else {
                                failMethod += method;
                            }

                        }

                        if (perCnt > 0) {
                            separator = ",";
                            failSeparator = "";
                        } else {
                            separator = "";
                            failSeparator = ",";
                        }


                    }

                    orderDto.setSendDecidedMethod(sucMethod);
                    orderDto.setFailDecidedMethod(failMethod);

                    orderMapper.updateOrderDecidedMethod(orderDto);




                }

            }

            //naverWritter.orderWrite(orderDtoList);
        } catch (Exception e) {
            log.error("네이버 스토어 오더 처리 중 오류 발생 - Store ID: {}, Store Name: {}, Error: {}", storeDto.getId(), storeDto.getStoreName(), e.getMessage());
        }
    }


    public Map<String, Object> requestOrderNoList(Map<String, String> headerMap, Map<String, Object> paramMap) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        String res = ApiUtil.get(baseUrl + orderListInfoUrl, paramMap, headerMap, true);
        return objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});

    }



}
