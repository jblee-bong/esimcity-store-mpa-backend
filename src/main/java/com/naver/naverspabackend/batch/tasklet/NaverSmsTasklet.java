package com.naver.naverspabackend.batch.tasklet;


import com.google.gson.Gson;
import com.naver.naverspabackend.dto.*;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.mybatis.mapper.ApiPurchaseItemMapper;
import com.naver.naverspabackend.mybatis.mapper.MatchInfoMapper;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.service.esimapiingsteplogs.EsimApiIngStepLogsService;
import com.naver.naverspabackend.service.sms.KakaoService;
import com.naver.naverspabackend.service.sms.MailService;
import com.naver.naverspabackend.service.sms.SmsService;
import com.naver.naverspabackend.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * 스마트 스토어 item select 상품 목록 조회
 *
 * @author gil
 */
@Slf4j
@Component
@StepScope
public class NaverSmsTasklet implements Tasklet {


    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MatchInfoMapper matchInfoMapper;

    @Autowired
    private SmsService smsService;

    @Autowired
    private KakaoService kakaoService;

    @Autowired
    private EsimApiIngStepLogsService esimApiIngStepLogsService;


    @Autowired
    private ApiPurchaseItemMapper apiPurchaseItemMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private FileUtil fileUtil;

    @Value("${spring.profiles.active}")
    private String active;

    @Value("${server-origin}")
    private String serverOrigin;

    @Value("${usage-uri}")
    private String usageUri;


    @Value("${usage2-uri}")
    private String usage2Uri;


    @Value("${testmode}")
    private String testMode;

    private final String notExistEmail = "sim";
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        if(testMode.equals("true"))
        {
            return null;
        }
        log.error("*************************************************** 이심 구매 및 발송 4 ***************************************************");

        Map<String,Object> param = new HashMap<>();
        param.put("userAuthority","ROLE_ADMIN");
        List<StoreDto> storeDtos = storeMapper.selectStoreList(param);

        for (StoreDto storeDto : storeDtos) {

            List<OrderDto> orderDtoList = orderMapper.selectOrderForSms(storeDto);

            for (OrderDto orderDto : orderDtoList) {
                try {
                    sendMessage(orderDto,storeDto);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 실패
                    orderDto.setSendStatus("3");
                    orderMapper.updateOrderSms(orderDto);
                }
            }

        }
        return RepeatStatus.FINISHED;
    }

    public void sendMessage(OrderDto orderDto, StoreDto storeDto) throws Exception {
        Gson gson = new Gson();
        TsimUtil tsimUtil = EsimUtil.getTsimUtil(null, storeDto, null);
        TugeUtil tugeUtil = EsimUtil.getTugeUtil(storeDto,null);
        WorldMoveUtil worldMoveUtil =  EsimUtil.getWorldMoveUtil(storeDto);
        NizUtil nizUtil =  EsimUtil.getNizUtil(storeDto);
        AirAloUtil airAloUtil  =  EsimUtil.getAirAloUtil(storeDto);


        MatchInfoDto matchInfoDto = matchInfoMapper.selectMatchInfoByOrder(orderDto);

        String email = "";
        if (matchInfoDto == null) {
            log.info("matchInfo 없음");
            return;
        }

        String sendMethod = matchInfoDto.getSendMethod();
        String ordererTel = orderDto.getOrdererTel();
        String shippingTel1 = orderDto.getShippingTel1();
        String shippingName = orderDto.getShippingName();

        List<Map<String, Object>> sendMapList = new ArrayList<>();
        for(int i=0;i<orderDto.getQuantity();i++){
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

            if ("Y".equals(matchInfoDto.getEsimFlag())) {
                try {
                    email = mailService.formatStringToEmail(Objects.toString(orderDto.getShippingMemo(), ""));
                } catch (Exception e) {
                    email = notExistEmail;
                }


                HashMap esimMap = new HashMap();

                if(matchInfoDto.getEsimType().equals("01")){
                    try {
                        esimMap = Tel25Util.contextLoads2(email, matchInfoDto.getEsimProductId());
                        fileUtil.makeQrCode(orderDto, esimMap);
                    } catch (NoSuchAlgorithmException e) {
                        matchInfoDto.setEsimFlag("N");
                    }

                    orderDto.setEsimCorp("TEL25");
                }

                else if(matchInfoDto.getEsimType().equals("02")){
                    orderDto.setEsimCorp("니즈");
                    try {
                        if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){ //이심 요청 전

                            for(int j=0;j<orderDto.getQuantity();j++){
                                esimMap = nizUtil.contextLoads2(email, matchInfoDto.getEsimProductId(), matchInfoDto.getEsimProductDays(),orderDto.getId()); //이심 요청

                                List<Map<String,Object>> resultList = (List<Map<String, Object>>) esimMap.get("activations");
                                Map<String,Object> result = resultList.get(0);

                                if(result.get("result").toString().equals("0")){
                                    Map<String,Object>activationReqeust = (Map<String, Object>) result.get("activationReqeust");

                                    if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){
                                        orderDto.setEsimApiRequestId((activationReqeust.get("id").toString()));
                                    }else{
                                        orderDto.setEsimApiRequestId(orderDto.getEsimApiRequestId() + ","+ activationReqeust.get("id").toString());
                                    }
                                }else{
                                    try{
                                        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE_END_FAIL.getExplain() + result.get("message").toString(), orderDto.getId());
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    };
                                }
                            }
                            orderMapper.updateOrderSmsForEsim(orderDto);
                            return;
                        }else{
                            //여기서 상태 체크해서 개통 완료일 경우 다음 스탭 진행
                            //0 : 개통 요청 상태
                            //1 : 개통 완료
                            //2 : 개통 오류
                            //3 : 개통 보류
                            //4, 5, 6 : 개통 처리 중
                            String[] esimApiRequestIds = orderDto.getEsimApiRequestId().split(",");
                            esimMap =  nizUtil.contextLoads3(esimApiRequestIds[i],orderDto.getId());
                            if(esimMap!=null)
                                log.error("니즈 개통 상태 체크 : " + esimMap.toString());
                            if(esimMap.get("status").toString().equals("1")){
                                fileUtil.makeQrCodeNiz(esimMap);
                            }else{
                                return;
                            }
                            try{
                                log.error("updateOrderSmsForEsimIccid : " + esimMap.toString());
                                orderDto.setEsimIccid(esimMap.get("iccid")!=null?esimMap.get("iccid").toString():null);
                                orderMapper.updateOrderSmsForEsimIccid(orderDto);


                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            esimMap.put("eEsimId","");
                            esimMap.put("eEsimType","");
                            esimMap.put("eEsimOrderId","");
                            esimMap.put("eEsimIccid","");
                            esimMap.put("eEsimRcode","");

                            esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.OPEN_END.getExplain(), orderDto.getId());
                        }
                    } catch (Exception e) {
                        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.ERROR.getExplain() + e.getMessage(), orderDto.getId());
                        matchInfoDto.setEsimFlag("N");
                    }
                }else if(matchInfoDto.getEsimType().equals("03")){
                    orderDto.setEsimCorp("Tsim");
                    //TSIM
                    try {
                        if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){ //이심 요청 전

                            for(int j=0;j<orderDto.getQuantity();j++){
                                esimMap = tsimUtil.contextLoads2(email, matchInfoDto.getEsimProductId(),orderDto.getId()); //이심 요청

                                Map<String,String> result = (Map<String, String>) esimMap.get("result");

                                if(esimMap.get("msg").toString().equals("Success")){
                                    String topup_id =  result.get("topup_id");

                                    if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){
                                        orderDto.setEsimApiRequestId(topup_id);
                                    }else{
                                        orderDto.setEsimApiRequestId(orderDto.getEsimApiRequestId() + ","+ topup_id);
                                    }
                                }
                            }
                            orderMapper.updateOrderSmsForEsim(orderDto);
                            return;
                        }else{
                            //여기서 상태 체크해서 개통 완료일 경우 다음 스탭 진행

                            String[] esimApiRequestIds = orderDto.getEsimApiRequestId().split(",");
                            Map<String,Object> result =  tsimUtil.contextLoads3(esimApiRequestIds[i], orderDto.getId());

                            if(result.get("msg").toString().equals("Success")){
                                esimMap =(HashMap) result.get("result");
                                fileUtil.makeQrCodeTsim(esimMap);
                            }else{
                                return;
                            }
                            try{
                                orderDto.setEsimIccid(esimMap.get("iccid")!=null?esimMap.get("iccid").toString():null);
                                orderMapper.updateOrderSmsForEsimIccid(orderDto);

                                orderDto.setEsimApn(esimMap.get("apn")!=null?esimMap.get("apn").toString():null);
                                orderMapper.updateOrderSmsForEsimApn(orderDto);
                            }catch (Exception e){
                                e.printStackTrace();
                            }




                            String id = CommonUtil.stringToBase64Encode((orderDto.getId()+""));
                            String type = CommonUtil.stringToBase64Encode(ApiType.TSIM.name());
                            String orderId = CommonUtil.stringToBase64Encode(esimApiRequestIds[i]);
                            String iccid = CommonUtil.stringToBase64Encode(esimMap.get("iccid").toString());

                            Map<String, String> exitem = new HashMap<>();
                            exitem.put("id",orderDto.getId()+"");
                            exitem.put("type",ApiType.TSIM.name());
                            exitem.put("orderId",esimApiRequestIds[i]);
                            exitem.put("iccid",esimMap.get("iccid").toString());
                            String esitemEncode = CommonUtil.stringToBase64Encode(gson.toJson(exitem));
                            esimMap.put("usage2Url",serverOrigin+usage2Uri+"?exitem="+ esitemEncode);

                            esimMap.put("usageUrl",serverOrigin+usageUri+"?id="+ id +"&type="+ type+"&orderId="+orderId+"&iccid="+iccid);

                            esimMap.put("exitem",CommonUtil.stringToBase64Encode(gson.toJson(exitem)));
                            esimMap.put("eEsimId",id);
                            esimMap.put("eEsimType",type);
                            esimMap.put("eEsimOrderId",orderId);
                            esimMap.put("eEsimIccid",iccid);
                            esimMap.put("eEsimRcode","");


                            esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.OPEN_END.getExplain(), orderDto.getId());
                        }




                    } catch (Exception e) {
                        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.ERROR.getExplain() + e.getMessage(), orderDto.getId());
                        matchInfoDto.setEsimFlag("N");
                    }
                }else if(matchInfoDto.getEsimType().equals("04")){
                    orderDto.setEsimCorp("AirAlo");
                    try {
                        Map<String,Object> param = new HashMap<>();
                        param.put("orderId", orderDto.getId());
                        if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){ //이심 요청 전

                            for(int j=0;j<orderDto.getQuantity();j++){
                                esimMap = airAloUtil.contextLoads2(email, matchInfoDto.getEsimProductId(), orderDto.getId()); //이심 요청

                                Map<String,Object> result = (Map<String, Object>) esimMap.get("meta");
                                Map<String,Object> data = (Map<String, Object>) esimMap.get("data");

                                if(result.get("message").toString().equals("success")){
                                    String orderId =  data.get("id").toString();
                                    if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){
                                        orderDto.setEsimApiRequestId(orderId);
                                    }else{
                                        orderDto.setEsimApiRequestId(orderDto.getEsimApiRequestId() + ","+ orderId);
                                    }

                                    List<HashMap<String,Object>> sims = (List<HashMap<String, Object>>) data.get("sims");
                                    String iccid =  sims.get(0).get("iccid").toString();
                                    if(orderDto.getEsimIccid()==null || orderDto.getEsimIccid().equals("")){
                                        orderDto.setEsimIccid(iccid);
                                    }else{
                                        orderDto.setEsimIccid(orderDto.getEsimIccid() + ","+ iccid);
                                    }
                                }
                            }


                            orderMapper.updateOrderSmsForEsimIccidAll(orderDto);
                            orderMapper.updateOrderSmsForEsim(orderDto);
                            return;
                        }else{
                            //여기서 상태 체크해서 개통 완료일 경우 다음 스탭 진행
                            String[] esimIccids = orderDto.getEsimIccid().split(",");
                            esimMap =  airAloUtil.contextLoads3(esimIccids[i], orderDto.getId());

                            Map<String,Object> result = (Map<String, Object>) esimMap.get("meta");
                            esimMap = (HashMap) esimMap.get("data");

                            if(esimMap!=null && result.get("message").toString().equals("success")){
                                fileUtil.makeQrCodeAirAlo(esimMap,orderDto);
                            }else{
                                return;
                            }


                            String id = CommonUtil.stringToBase64Encode((orderDto.getId()+""));
                            String type = CommonUtil.stringToBase64Encode(ApiType.AIRALO.name());
                            String iccid = CommonUtil.stringToBase64Encode(esimIccids[i]);

                            Map<String, String> exitem = new HashMap<>();
                            exitem.put("id",orderDto.getId()+"");
                            exitem.put("type",ApiType.AIRALO.name());
                            exitem.put("iccid",esimIccids[i]);
                            String esitemEncode = CommonUtil.stringToBase64Encode(gson.toJson(exitem));
                            esimMap.put("usage2Url",serverOrigin+usage2Uri+"?exitem="+ esitemEncode);


                            esimMap.put("usageUrl",serverOrigin+usageUri+"?id="+ id +"&type="+ type+"&iccid="+iccid);

                            esimMap.put("exitem",CommonUtil.stringToBase64Encode(gson.toJson(exitem)));
                            esimMap.put("eEsimId",id);
                            esimMap.put("eEsimType",type);
                            esimMap.put("eEsimOrderId","");
                            esimMap.put("eEsimIccid",iccid);
                            esimMap.put("eEsimRcode","");


                            esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.OPEN_END.getExplain(), orderDto.getId());
                        }




                    } catch (Exception e) {
                        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.ERROR.getExplain() + e.getMessage(), orderDto.getId());
                        matchInfoDto.setEsimFlag("N");
                    }
                }else if(matchInfoDto.getEsimType().equals("05")){
                    orderDto.setEsimCorp("TUGE");
                    //Tuge
                    try {
                        Map<String,Object> param = new HashMap<>();
                        param.put("orderId", orderDto.getId());

                        List<OrderTugeEsimDto> orderTugeEsimDtoList = orderMapper.selectListOrderTugeEsim(param);

                        if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){ //이심 요청 전
                            List<String> orderIdList = new ArrayList<>();
                            for(int j=0;j<orderDto.getQuantity();j++){
                                String orederNo = tugeUtil.contextLoads2(email, matchInfoDto.getEsimProductId(),orderDto.getId()+"number"+j, orderDto.getId()); //이심 요청
                                if(orederNo!=null){
                                    String orderId =  orederNo;
                                    orderIdList.add(orderId);
                                    if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){
                                        orderDto.setEsimApiRequestId(orderId);
                                    }else{
                                        orderDto.setEsimApiRequestId(orderDto.getEsimApiRequestId() + ","+ orderId);
                                    }
                                }
                            }


                            orderMapper.updateOrderSmsForEsim(orderDto);
                            return;
                        }else if(orderDto.getEsimApiRequestId().split(",").length == orderTugeEsimDtoList.size() ){ // 3건구매시 3건이 다들어왔다.

                            //여기서 상태 체크해서 개통 완료일 경우 다음 스탭 진행
                            String[] esimApiRequestIds = orderDto.getEsimApiRequestId().split(",");

                            OrderTugeEsimDto orderTugeEsimDto = orderTugeEsimDtoList.get(i);
                            orderDto.setEsimIccid(orderTugeEsimDto.getIccid());
                            orderMapper.updateOrderSmsForEsimIccid(orderDto);
                            esimMap.put("iccid",orderTugeEsimDto.getIccid());
                            esimMap.put("downloadUrl",orderTugeEsimDto.getQrCode());

                            fileUtil.makeQrCodeTuge(esimMap,orderDto);

                            String id = CommonUtil.stringToBase64Encode((orderDto.getId()+""));
                            String type = CommonUtil.stringToBase64Encode(ApiType.TUGE.name());
                            String orderId = CommonUtil.stringToBase64Encode(esimApiRequestIds[i]);
                            String iccid = CommonUtil.stringToBase64Encode(esimMap.get("iccid").toString());

                            Map<String, String> exitem = new HashMap<>();
                            exitem.put("id",orderDto.getId()+"");
                            exitem.put("type",ApiType.TUGE.name());
                            exitem.put("orderId",esimApiRequestIds[i]);
                            exitem.put("iccid",esimMap.get("iccid").toString());
                            String esitemEncode = CommonUtil.stringToBase64Encode(gson.toJson(exitem));
                            esimMap.put("usage2Url",serverOrigin+usage2Uri+"?exitem="+ esitemEncode);
                            esimMap.put("usageUrl",serverOrigin+usageUri+"?id="+ id +"&type="+ type+ "&orderId=" + orderId +"&iccid="+iccid);

                            esimMap.put("exitem",CommonUtil.stringToBase64Encode(gson.toJson(exitem)));
                            esimMap.put("eEsimId",id);
                            esimMap.put("eEsimType",type);
                            esimMap.put("eEsimOrderId",orderId);
                            esimMap.put("eEsimIccid",iccid);
                            esimMap.put("eEsimRcode","");
                            esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.OPEN_END.getExplain(), orderDto.getId());

                        }else{
                            return;
                        }
                    } catch (Exception e) {
                        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.ERROR.getExplain() + e.getMessage(), orderDto.getId());
                        matchInfoDto.setEsimFlag("N");
                    }
                }else if(matchInfoDto.getEsimType().equals("06")){
                    // WorldMoveUItil
                    orderDto.setEsimCorp("WorldMove");
                    try {
                        Map<String,Object> param = new HashMap<>();
                        param.put("orderId", orderDto.getId());
                        int countOrderWorldMoveEsim = orderMapper.selectCountOrderWorldMoveEsim(param);
                        if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){ //이심 요청 전

                            List<String> orderIdList = new ArrayList<>();
                            for(int j=0;j<orderDto.getQuantity();j++){
                                esimMap = worldMoveUtil.contextLoads2(email, matchInfoDto.getEsimProductId(), 0, orderDto.getId()); //이심 요청


                                if(esimMap.get("code").toString().equals("0")){
                                    String orderId =  esimMap.get("orderId").toString();
                                    orderIdList.add(orderId);
                                    if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){
                                        orderDto.setEsimApiRequestId(orderId);
                                    }else{
                                        orderDto.setEsimApiRequestId(orderDto.getEsimApiRequestId() + ","+ orderId);
                                    }
                                }
                            }


                            orderMapper.updateOrderSmsForEsim(orderDto);
                            return;
                        }else if(orderDto.getEsimIccid()==null ||
                                (orderDto.getEsimIccid().split(",").length != orderDto.getEsimApiRequestId().split(",").length) ||
                                (countOrderWorldMoveEsim < orderDto.getEsimIccid().split(",").length )
                        ){

                            String iccid = "";
                            String rcode = "";
                            for(String orderId : orderDto.getEsimApiRequestId().split(",")){
                                HashMap result = null;
                                try{
                                    result = worldMoveUtil.contextLoads3(orderId, orderDto.getId());
                                }catch (Exception e){
                                    e.printStackTrace();
                                    return;
                                }
                                if(result==null){
                                    return;
                                }
                                if(iccid.equals("")){
                                    iccid = result.get("iccid").toString();
                                }else{
                                    iccid = iccid + "," + result.get("iccid").toString();
                                }

                                if(rcode.equals("")){
                                    rcode = result.get("rcode").toString();
                                }else{
                                    rcode = rcode + "," + result.get("rcode").toString();
                                }
                            }
                            orderDto.setEsimIccid(iccid);
                            orderDto.setEsimRcode(rcode);
                            orderMapper.updateOrderSmsForEsimIccidWordMove(orderDto);
                            return;
                        }else{
                            //여기서 상태 체크해서 개통 완료일 경우 다음 스탭 진행
                            //WORLDMOVE TB_ORDER_WORDMOVE_ESIM 테이블에서 해당 오더에 해당하는 데이터들 꺼내서 파라미터 생성하면됨

                            String[] esimIccids = orderDto.getEsimIccid().split(",");
                            String[] esimRcodes = orderDto.getEsimRcode().split(",");

                            OrderWorldmoveEsimDto orderWroldMoveEsimParam = new OrderWorldmoveEsimDto();
                            orderWroldMoveEsimParam.setOrderId(orderDto.getId());
                            orderWroldMoveEsimParam.setEsimType("image");
                            orderWroldMoveEsimParam.setEsimIccid( esimIccids[i]);
                            OrderWorldmoveEsimDto orderWroldMoveEsimUrl = orderMapper.selectOrderWorldMoveEsim(orderWroldMoveEsimParam);
                            orderWroldMoveEsimParam.setEsimType("text");
                            OrderWorldmoveEsimDto orderWroldMoveEsimText = orderMapper.selectOrderWorldMoveEsim(orderWroldMoveEsimParam);


                            fileUtil.makeQrCodeWorldMove(esimMap,orderWroldMoveEsimUrl,orderWroldMoveEsimText,esimIccids[i] ,orderDto );



                            String id = CommonUtil.stringToBase64Encode((orderDto.getId()+""));
                            String type = CommonUtil.stringToBase64Encode(ApiType.WORLDMOVE.name());
                            String rcode = CommonUtil.stringToBase64Encode(esimRcodes[i]);
                            String iccid = CommonUtil.stringToBase64Encode(esimIccids[i]);

                            Map<String, String> exitem = new HashMap<>();
                            exitem.put("id",orderDto.getId()+"");
                            exitem.put("type",ApiType.WORLDMOVE.name());
                            exitem.put("rcode",esimRcodes[i]);
                            exitem.put("iccid",esimIccids[i]);
                            esimMap.put("usage2Url",serverOrigin+usage2Uri+"?exitem="+ CommonUtil.stringToBase64Encode(gson.toJson(exitem)));



                            esimMap.put("usageUrl",serverOrigin+usageUri+"?id="+ id +"&type="+ type + "&rcode=" + rcode + "&iccid="+iccid);

                            esimMap.put("exitem",CommonUtil.stringToBase64Encode(gson.toJson(exitem)));
                            esimMap.put("eEsimId",id);
                            esimMap.put("eEsimType",type);
                            esimMap.put("eEsimOrderId","");
                            esimMap.put("eEsimIccid",iccid);
                            esimMap.put("eEsimRcode",rcode);


                            esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.OPEN_END.getExplain(), orderDto.getId());
                        }




                    } catch (Exception e) {
                        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.ERROR.getExplain() + e.getMessage(), orderDto.getId());

                        matchInfoDto.setEsimFlag("N");
                    }
                }else if(matchInfoDto.getEsimType().equals("99")){
                    orderDto.setEsimCorp("벌크");
                    try {
                        esimMap = BulkUtil.contextLoads2( matchInfoDto.getEsimProductId(), orderDto, orderDto.getId());

                        fileUtil.makeQrCodeBulk(esimMap, orderDto);

                        orderDto.setEsimIccid(esimMap.get("bulkIccid")!=null?esimMap.get("bulkIccid").toString():null);
                        orderMapper.updateOrderSmsForEsimIccid(orderDto);
                        orderDto.setEsimActivationCode(esimMap.get("bulkActiveCode")!=null?esimMap.get("bulkActiveCode").toString():null);
                        orderMapper.updateOrderSmsForEsimActivationCode(orderDto);

                        if(orderDto.getEsimApiRequestId()==null || orderDto.getEsimApiRequestId().equals("")){
                            orderDto.setEsimApiRequestId(esimMap.get("id")!=null?esimMap.get("id").toString():null);
                        }else{
                            orderDto.setEsimApiRequestId(orderDto.getEsimApiRequestId() + ","+ (esimMap.get("id")!=null?esimMap.get("id").toString():null));
                        }
                        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.OPEN_END.getExplain(), orderDto.getId());



                        esimMap.put("eEsimId","");
                        esimMap.put("eEsimType","");
                        esimMap.put("eEsimOrderId","");
                        esimMap.put("eEsimIccid","");
                        esimMap.put("eEsimRcode","");
                    } catch (Exception e) {
                        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.ERROR.getExplain() + e.getMessage(), orderDto.getId());

                        e.printStackTrace();
                        matchInfoDto.setEsimFlag("N");
                    }

                }
                try{
                    if(matchInfoDto.getEsimType()!=null && !matchInfoDto.getEsimType().equals("")){
                        String type="";
                        if(matchInfoDto.getEsimType().equals("01"))
                            type = "tel25";
                        else if(matchInfoDto.getEsimType().equals("02"))
                            type = "NIZ";
                        else if(matchInfoDto.getEsimType().equals("03"))
                            type = "TSIM";
                        else if(matchInfoDto.getEsimType().equals("04"))
                            type = "AIRALO";
                        else if(matchInfoDto.getEsimType().equals("05"))
                            type = "TUGE";
                        else if(matchInfoDto.getEsimType().equals("06"))
                            type = "WORLDMOVE";
                        ApiPurchaseItemDto apiPurchaseItemDto = new ApiPurchaseItemDto();
                        apiPurchaseItemDto.setApiPurchaseItemProcutId(matchInfoDto.getEsimProductId());
                        apiPurchaseItemDto.setApiPurchaseItemType(type);
                        ApiPurchaseItemDto apiPurchaseItem = apiPurchaseItemMapper.findById(apiPurchaseItemDto);

                        ApiCardTypeDto apiCardTypeDto = null;
                        if(type.equals("TUGE")){
                            ApiCardTypeDto param = new ApiCardTypeDto();
                            param.setCardType(apiPurchaseItem.getApiPurchaseItemCardType());
                            apiCardTypeDto = apiPurchaseItemMapper.selectCardTypeFindByCardType(param);
                        }



                        if(apiPurchaseItem.getApiPurchasePrice()!=null && !apiPurchaseItem.getApiPurchasePrice().equals("") && apiPurchaseItem.getApiPurchaseCurrency()!=null && !apiPurchaseItem.getApiPurchaseCurrency().equals("")){
                            Double allprice = Double.parseDouble(apiPurchaseItem.getApiPurchasePrice()) * orderDto.getQuantity().doubleValue();
                            orderDto.setOrderAllPrice(allprice+"");
                            orderDto.setOrderPriceCurrency(apiPurchaseItem.getApiPurchaseCurrency());
                            orderMapper.updateOrderAllPrice(orderDto);
                        }


                        try{


                            if(type.equals("TUGE") || type.equals("TSIM")){
                                esimMap.put("eSimApnInfo",apiPurchaseItem.getApiPurchaseApn()!=null && !apiPurchaseItem.getApiPurchaseApn().equals("")?("<li style=\"margin-bottom: 8px;\"><strong style=\"color: #dc2626;\">APN값은 "+apiPurchaseItem.getApiPurchaseApn()+"입니다. 현지에서 인터넷 오류시에만 확인 부탁드립니다.</strong></li>"):"");
                                esimMap.put("eSimMApnInfo",apiPurchaseItem.getApiPurchaseApn()!=null && !apiPurchaseItem.getApiPurchaseApn().equals("")?("* APN 값은 " +apiPurchaseItem.getApiPurchaseApn() + " 입니다. 현지에서 인터넷 오류시에만 확인 부탁드립니다."):"");
                            }else if(type.equals("WORLDMOVE")){
                                OrderWorldmoveEsimDto orderWroldMoveEsimParam = new OrderWorldmoveEsimDto();
                                orderWroldMoveEsimParam.setOrderId(orderDto.getId());
                                OrderWorldmoveEsimDto orderWroldMoveEsimText = orderMapper.selectOrderWorldMoveEsimFirst(orderWroldMoveEsimParam);
                                esimMap.put("eSimApnInfo",orderWroldMoveEsimText.getApnExplain()!=null && !orderWroldMoveEsimText.getApnExplain().equals("")?("<li style=\"margin-bottom: 8px;\"><strong style=\"color: #dc2626;\">APN값은 "+orderWroldMoveEsimText.getApnExplain()+"입니다. 현지에서 인터넷 오류시에만 확인 부탁드립니다.</strong></li>"):"");
                                esimMap.put("eSimMApnInfo",orderWroldMoveEsimText.getApnExplain()!=null && !orderWroldMoveEsimText.getApnExplain().equals("")?("* APN 값은 " +orderWroldMoveEsimText.getApnExplain() + " 입니다. 현지에서 인터넷 오류시에만 확인 부탁드립니다."):"");
                            }
                            if(esimMap.get("eSimApnInfo")== null || esimMap.get("eSimApnInfo").toString().equals("")){
                                esimMap.put("eSimApnInfo",("<li style=\"margin-bottom: 8px;\"><strong style=\"color: #dc2626;\">APN 정보가 필요하신 경우 알림톡이나 톡톡을 통해 문의주세요.</strong></li>"));
                                esimMap.put("eSimMApnInfo",("* APN 값은 정보가 필요하신 경우 알림톡이나 톡톡을 통해 문의주세요."));
                            }

                            if(apiPurchaseItem.isApiPurchaseIsCharge()){
                                esimMap.put("eSimChargeInfo","<li style=\"margin-bottom: 8px;\"><strong style=\"color: #dc2626;\">이상품은 데이터 충전이 가능한 상품입니다. 아래 버튼을 통해 충전해주세요.</strong></li>");
                                esimMap.put("eSimMChargeInfo","* 이 상품은 데이터 충전이 가능한 상품입니다. 아래 버튼을 통해 충전해주세요.");
                            }else{
                                esimMap.put("eSimChargeInfo","");
                                esimMap.put("eSimMChargeInfo","");
                            }




                            esimMap.put("eSimResetInfo",MakeResetTimeUtil.makeResetInfoText(apiPurchaseItem,apiCardTypeDto));
                            esimMap.put("eSimMResetInfo",MakeResetTimeUtil.makeMResetInfoText(apiPurchaseItem,apiCardTypeDto));
                        }catch (Exception e){
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }




                for (Object key : esimMap.keySet()) {
                    if(!((String) key).equals("productName"))
                        sendMap.put((String) key, esimMap.get(key));
                }

            }


            sendMap.put("body", matchInfoDto.getBody());
            sendMap.put("smsBody", matchInfoDto.getSmsBody());
            sendMap.put("title", matchInfoDto.getTitle());

            // mail
            sendMap.put("emailContents", matchInfoDto.getMailContents());
            sendMap.put("emailSubject", matchInfoDto.getMailTitle());
            sendMap.put("shippingMemo", orderDto.getShippingMemo());
            sendMap.put("orderTitle", orderDto.getProductName());
            sendMapList.add(sendMap);
        }





        String[] sendMethods = sendMethod.split(",");
        int totalCnt = 0;
        String separator = "";
        String failSeparator = "";
        String sucMethod = "";
        String failMethod = "";
        for( int i=0;i<sendMapList.size();i++){
            boolean onlyOneTrans = i == 0;
            Map<String, Object> sendMap = sendMapList.get(i);

            for (String method : sendMethods) {
                if(method.indexOf("COMFIRM-")>-1)
                    continue;
                sucMethod += separator;
                failMethod += failSeparator;

                int perCnt = 0;
                boolean esimFlagInfo = "Y".equals(matchInfoDto.getEsimFlag()) && method.indexOf("E-") > -1;
                if(esimFlagInfo){
                    sendMap.put("body", CommonUtil.replaceVariableString(matchInfoDto.geteBody(), orderDto, sendMap, esimFlagInfo));
                    sendMap.put("smsBody", CommonUtil.replaceVariableString(matchInfoDto.geteSmsBody(), orderDto, sendMap, esimFlagInfo));
                    sendMap.put("title", CommonUtil.replaceVariableString(matchInfoDto.geteTitle(), orderDto, sendMap, esimFlagInfo));
                    sendMap.put("emailContents", CommonUtil.replaceVariableString(matchInfoDto.geteMailContents(), orderDto, sendMap, esimFlagInfo));
                    sendMap.put("emailSubject", CommonUtil.replaceVariableString(matchInfoDto.geteMailTitle(), orderDto, sendMap, esimFlagInfo));
                }else{
                    sendMap.put("body", CommonUtil.replaceVariableString(matchInfoDto.getBody(), orderDto, sendMap, esimFlagInfo));
                    sendMap.put("smsBody", CommonUtil.replaceVariableString(matchInfoDto.getSmsBody(), orderDto, sendMap, esimFlagInfo));
                    sendMap.put("title", CommonUtil.replaceVariableString(matchInfoDto.getTitle(), orderDto, sendMap, esimFlagInfo));
                    sendMap.put("emailContents", CommonUtil.replaceVariableString(matchInfoDto.getMailContents(), orderDto, sendMap, esimFlagInfo));
                    sendMap.put("emailSubject", CommonUtil.replaceVariableString(matchInfoDto.getMailTitle(), orderDto, sendMap, esimFlagInfo));
                }
                if( method.equals("E-EMAIL")){
                    MailContentsDto mailContentsDto = new MailContentsDto();
                    mailContentsDto.setEmailContents(Objects.toString(sendMap.get("emailContents"), ""));
                    mailContentsDto.setEmailSubject(Objects.toString(sendMap.get("emailSubject"), ""));
                    mailContentsDto.setStoreId(Long.parseLong(Objects.toString(sendMap.get("storeId"),"0")));
                    mailContentsDto.setOriginProductNo(Long.parseLong(Objects.toString(sendMap.get("originProductNo"),"0")));
                    mailContentsDto.setOptionId(Long.parseLong(Objects.toString(sendMap.get("optionId"),"0")));
                    mailContentsDto.setOrderId(orderDto.getId());
                    mailContentsDto.setEsimYn(esimFlagInfo?"Y":"N");
                    mailService.insertMailContents(mailContentsDto);
                }
                if(method.equals("EMAIL") && onlyOneTrans){

                    ArrayList<String> tilteList = gson.fromJson(Objects.toString(sendMap.get("emailSubject"), ""),ArrayList.class);
                    ArrayList<String> contentsList = gson.fromJson(Objects.toString(sendMap.get("emailContents"), ""),ArrayList.class);
                    for(int k = 0;k<tilteList.size();k++){
                        MailContentsDto mailContentsDto = new MailContentsDto();
                        mailContentsDto.setEmailContents(contentsList.get(k));
                        mailContentsDto.setEmailSubject(tilteList.get(k));
                        mailContentsDto.setStoreId(Long.parseLong(Objects.toString(sendMap.get("storeId"),"0")));
                        mailContentsDto.setOriginProductNo(Long.parseLong(Objects.toString(sendMap.get("originProductNo"),"0")));
                        mailContentsDto.setOptionId(Long.parseLong(Objects.toString(sendMap.get("optionId"),"0")));
                        mailContentsDto.setOrderId(orderDto.getId());
                        mailContentsDto.setEsimYn(esimFlagInfo?"Y":"N");
                        mailService.insertMailContents(mailContentsDto);
                    }
                }

                if ("SMS".equals(method)  && onlyOneTrans) {
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

                } else if ("MMS".equals(method) && onlyOneTrans) {
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
                } else if ("KAKAO".equals(method) && onlyOneTrans) {
                    try {
                        ArrayList<String> kakaoTemplateKeyList =  gson.fromJson(matchInfoDto.getKakaoTemplateKey(),ArrayList.class);
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

                } else if ("EMAIL".equals(method) && onlyOneTrans) {
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
                // E-SIM
                if (esimFlagInfo) {

                    if ("E-SMS".equals(method)) {
                        try {
                            perCnt = smsService.insertSms(sendMap, storeDto);
                        } catch (Exception e) {
                            log.error("e-sms 발송 오류 =======================", e);
                            e.printStackTrace();
                        }
                        totalCnt += perCnt;

                        if (perCnt > 0) {
                            sucMethod += method;
                        } else {
                            failMethod += method;
                        }

                    } else if ("E-MMS".equals(method)) {
                        try {
                            perCnt = smsService.insertMms(sendMap, storeDto);
                        } catch (Exception e) {
                            log.error("e-mms 발송 오류 =======================", e);
                            e.printStackTrace();
                        }
                        totalCnt += perCnt;

                        if (perCnt > 0) {
                            sucMethod += "E-MMS";
                        } else {
                            failMethod += method;
                        }

                    } else if ("E-KAKAO".equals(method)) {
                        try {
                            perCnt = kakaoService.requestSendKakaoMsg(sendMap, matchInfoDto.geteKakaoTemplateKey(),storeDto,orderDto,matchInfoDto.getEsimFlag(), matchInfoDto.getEKakaoResendFlag(),false, shippingTel1);
                        } catch (Exception e) {
                            log.error("e-kakao 발송 오류 =======================", e);
                            e.printStackTrace();
                        }
                        totalCnt += perCnt;

                        if (perCnt > 0) {
                            sucMethod += method;
                        } else {
                            failMethod += method;
                        }

                    } else if ("E-EMAIL".equals(method)) {
                        try {
                            perCnt = mailService.sendEmail(sendMap, storeDto,email);
                        } catch (Exception e) {
                            log.error("e-email 발송 오류 =======================", e);
                            e.printStackTrace();
                        }
                        totalCnt += perCnt;

                        if (perCnt > 0) {
                            sucMethod += method;
                        } else {
                            failMethod += method;
                        }

                    }
                } else if (method.indexOf("E-") > -1) {
                    failMethod += method;
                }

                if (perCnt > 0) {
                    separator = ",";
                    failSeparator = "";
                } else {
                    separator = "";
                    failSeparator = ",";
                }


            }

            orderDto.setSendMethod(sucMethod);

            if (totalCnt > 0) {
                if ("".equals(failMethod)) {
                    // 모두 정상 1
                    orderDto.setSendStatus("1");
                } else if (!"".equals(failMethod)) {
                    // 부분 비정상 2
                    orderDto.setFailMethod(failMethod);
                    orderDto.setSendStatus("2");
                }
            }else{
                // 모두 실패 3
                orderDto.setFailMethod(failMethod);
                orderDto.setSendStatus("3");
            }

            if(sendMap.get("activation_code")!=null){
                if(orderDto.getEsimActivationCode()!=null && !orderDto.getEsimActivationCode().equals("")){
                    orderDto.setEsimActivationCode(orderDto.getEsimActivationCode() + "," + sendMap.get("activation_code").toString());
                }else{
                    orderDto.setEsimActivationCode(sendMap.get("activation_code").toString());
                }
            }
            if(sendMap.get("esimDescription")!=null)
                orderDto.setEsimDescription(sendMap.get("esimDescription").toString());

            if(sendMap.get("esimProductId")!=null)
                orderDto.setEsimProductId(sendMap.get("esimProductId").toString());

            if(sendMap.get("esimProductDays")!=null)
                orderDto.setEsimProductDays(sendMap.get("esimProductDays").toString());


        }

        orderDto.setTransUseYn(matchInfoDto.getTransUseYn());

        orderMapper.updateOrderSms(orderDto);

        /*
        try{
            boolean esimFlagInfo = "Y".equals(matchInfoDto.getEsimFlag());
            if(esimFlagInfo && email.equals(notExistEmail)){
                Map<String, Object> kakaoParameters = new HashMap<>();
                Map<String, String> exitem = new HashMap<>();
                exitem.put("id",orderDto.getId()+"");
                kakaoParameters.put("exitem",CommonUtil.stringToBase64Encode(gson.toJson(exitem)));
                kakaoParameters.put("orderTitle", orderDto.getProductName());
                String orderRealName = "";
                if(orderDto.getOptionName1()!=null){
                    orderRealName = orderDto.getOptionName1();
                }
                if(orderDto.getOptionName2()!=null){
                    if(!orderRealName.equals(""))
                        orderRealName += " ";
                    orderRealName += orderDto.getOptionName2();
                }
                kakaoParameters.put("orderRealName", orderRealName);
                kakaoParameters.put("ordererName", orderDto.getOrdererName());
                kakaoService.requestSendKakaoMsg(kakaoParameters, "ESIM_MAIL_RETRANS",storeDto,orderDto, "N", matchInfoDto.getEKakaoResendFlag(),false, shippingTel1);
            }
        }catch (Exception e){
        }*/


    }


    public static void main(String[] args) throws Exception {
        String id = CommonUtil.stringToBase64Encode((106352547+""));
        String type = CommonUtil.stringToBase64Encode(ApiType.WORLDMOVE.name());
        String orderId = CommonUtil.stringToBase64Encode("SE20250811090820580001");
        String iccid = CommonUtil.stringToBase64Encode("89852342022277656046");

        System.out.println("?id="+ id +"&type="+ type+ "&orderId=" + orderId +"&iccid="+iccid);


    }


}