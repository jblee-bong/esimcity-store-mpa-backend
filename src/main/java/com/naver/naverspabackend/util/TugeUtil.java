package com.naver.naverspabackend.util;

import com.beust.ah.A;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.naver.naverspabackend.dto.ApiCardTypeDto;
import com.naver.naverspabackend.dto.ApiPurchaseItemDto;
import com.naver.naverspabackend.dto.OrderTugeEsimDto;
import com.naver.naverspabackend.dto.TopupOrderDto;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.security.TugeRedisRepository;
import com.naver.naverspabackend.security.token.TugeRedisToken;
import com.naver.naverspabackend.service.apipurchaseitem.ApiPurchaseItemService;
import com.naver.naverspabackend.service.esimapiingsteplogs.EsimApiIngStepLogsService;
import com.naver.naverspabackend.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TugeUtil {

    public static String accountId;

    public static String signKey;

    public static String secretkey;

    public static String vector;

    public static String version;

    public static String baseUrl;

    public static String active;

    public static EsimApiIngStepLogsService esimApiIngStepLogsService;

    public static TugeRedisRepository tugeRedisRepository;

    public static ApiPurchaseItemService apiPurchaseItemService;
    public static OrderService orderService;
    public TugeUtil(String accountId,String signKey, String secretkey, String vector, String version, String baseUrl, EsimApiIngStepLogsService esimApiIngStepLogsService, TugeRedisRepository tugeRedisRepository,OrderService orderService,ApiPurchaseItemService apiPurchaseItemService,String active){
        this.accountId = accountId;
        this.signKey = signKey;
        this.secretkey = secretkey;
        this.vector = vector;
        this.version = version;
        this.baseUrl = baseUrl;
        this.esimApiIngStepLogsService = esimApiIngStepLogsService;
        this.tugeRedisRepository = tugeRedisRepository;
        this.orderService = orderService;
        this.apiPurchaseItemService = apiPurchaseItemService;
        this.active = active;

    }







    public static String contextLoads2(String userId, String esimProductId, String orderIdWithQuantityNumber, Long orderId) throws Exception {

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);

        System.setProperty("https.protocols","TLSv1.2");


        HashMap esimResult =  getEsimPackagePurchase(esimProductId,userId,orderIdWithQuantityNumber, orderId);

        Gson gson = new Gson();
        if(esimResult.get("code").toString().equals("0000")){
            HashMap<String,Object> data = (HashMap<String, Object>) esimResult.get("data");
            esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE_END0.getExplain() + data, orderId);

            return    data.get("orderNo").toString();
        }

        return null;
    }


    public static void contextLoads4(String orderId, String iccid, Model model, ApiPurchaseItemDto apiPurchaseItemDto) throws Exception {
        System.setProperty("https.protocols","TLSv1.2");

        HashMap esimResult =  getEsimStatus(orderId,iccid);
        HashMap esimResult2 =  getEsimStatus2(orderId);

        Gson gson = new Gson();
        if(esimResult.get("code").toString().equals("0000") && esimResult2.get("code").toString().equals("0000") ){
            HashMap<String,Object> esimMap = (HashMap<String, Object>) esimResult.get("data");
            HashMap<String,Object> esimMap2 = (HashMap<String, Object>) esimResult2.get("data");


            List<Map<String,Object>> esimMap2List = (List<Map<String, Object>>) esimMap2.get("list");

            Map<String,Object> orderData = esimMap2List.get(0);

            if(orderData.get("orderStatus")!=null && !orderData.get("orderStatus").equals("NOTACTIVE")){
                // ISO 8601 문자열을 한국 시간 객체로 바로 변환
                ZonedDateTime activatedStartTime = ZonedDateTime.parse(orderData.get("activatedStartTime").toString()).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                // DB 저장용 포맷 (2026-12-09 23:26)
                model.addAttribute("useStartDate",activatedStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));//활성화시작일
                // ISO 8601 문자열을 한국 시간 객체로 바로 변환
                ZonedDateTime activatedEndTime = ZonedDateTime.parse(orderData.get("activatedEndTime").toString()).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                // DB 저장용 포맷 (2026-12-09 23:26)
                model.addAttribute("useEndDate",activatedEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));//활성화시작일
            }else{
                model.addAttribute("useStartDate","");
                model.addAttribute("useEndDate","");
            }

            ApiCardTypeDto param = new ApiCardTypeDto();
            param.setCardType(apiPurchaseItemDto.getApiPurchaseItemCardType());
            ApiCardTypeDto apiCardTypeDto = apiPurchaseItemService.selectCardTypeFindByCardType(param);
            if(apiCardTypeDto!=null && apiCardTypeDto.isRenewYn() && orderData.get("renewExpirationTime")!=null ){
                ZonedDateTime renewExpirationTime = ZonedDateTime.parse(orderData.get("renewExpirationTime").toString()).withZoneSameInstant(ZoneId.of("Asia/Seoul"));

                // 3. 현재 시간과의 차이 계산
                long limitTimeMillis = renewExpirationTime.toInstant().toEpochMilli(); // 이제 이 시간은 '진짜 만료' 10분 전 시간입니다.
                long currentTimeMillis = System.currentTimeMillis();
                long diffMillis = limitTimeMillis - currentTimeMillis;

                if (diffMillis > 0) {
                    // 10분 전 시점까지 아직 시간이 남은 경우
                    long days = diffMillis / (24 * 60 * 60 * 1000);
                    long hours = (diffMillis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
                    long minutes = (diffMillis % (60 * 60 * 1000)) / (60 * 1000);
                    String displayTime = null;
                    if (days > 0) {
                        // 1일 이상 남았을 때: "1일 5시간 남음"
                        displayTime = String.format("%d일 %d시간", days, hours);
                    } else if (hours > 0) {
                        // 1일 미만, 1시간 이상: "5시간 30분 남음"
                        displayTime = String.format("%d시간 %d분", hours, minutes);
                    } else {
                        // 1시간 미만: "25분 남음"
                        displayTime = String.format("%d분", minutes);
                    }

                    model.addAttribute("chargeBtnTxt","충전하기 (남은 시간: " + displayTime + ")");//충전버튼
                    model.addAttribute("chargeYN","Y");//충전가능
                    //model.addAttribute("deviceId",deviceId);//충전파라미터


                    List<ApiPurchaseItemDto> apiPurchaseItemDtoList = apiPurchaseItemService.selectApiPurchaseItemListForTopupWithTuge(apiPurchaseItemDto);
                    List<Map<String,String>> apiPurchaseItemList = new ArrayList<>();
                    for(ApiPurchaseItemDto data : apiPurchaseItemDtoList){
                        Map<String,String> apiPurchaseItem = new HashMap<>();
                        apiPurchaseItem.put("channel_dataplan_id",data.getApiPurchaseItemProcutId());
                        apiPurchaseItem.put("channel_dataplan_day",data.getApiPurchaseItemDays() +"일" );
                        apiPurchaseItem.put("channel_dataplan_data",
                                data.getApiPurchaseDataTotal().equals("Unlimited") ?"무제한":
                                        (
                                            (data.isApiPurchaseItemIsDaily()?"매일 ":"총 ") + data.getApiPurchaseDataTotal() +
                                            (apiPurchaseItemDto.getApiPurchaseSlowSpeed()!=null?" + 저속 무제한":"")
                                    )
                        );
                        apiPurchaseItemList.add(apiPurchaseItem);
                    }
                    model.addAttribute("apiPurchaseItemList",apiPurchaseItemList);//충전 가능리스트



                } else {
                    model.addAttribute("chargeYN","N");//충전불가
                }
            } else {
                model.addAttribute("chargeYN","N");//충전불가
            }
            model.addAttribute("end",orderData.get("orderStatus").toString().equals("EXPIRED"));

            String[] apiPurchaseItemProcutIds = apiPurchaseItemDto.getApiPurchaseItemProcutId().split("-");

            model.addAttribute("isDaily",apiPurchaseItemDto.isApiPurchaseItemIsDaily());//매일데이터 리셋 유부


            if(apiPurchaseItemDto.getApiPurchaseItemPeriodType()==0){
                model.addAttribute("resetTxt","데이터 충전: 활성화 시점부터 24시간<br/>이용 일수: 활성화 시점부터 24시간");
            }else {
                if (apiPurchaseItemDto.getApiPurchaseItemCardType() != null) {
                    if(apiCardTypeDto!=null && apiCardTypeDto.getTimeZone()!=null){
                        ZonedDateTime sourceTime = LocalDate.of(2024, 1, 1)
                                .atTime(LocalTime.MIN) // 00:00
                                .atZone(ZoneId.of(apiCardTypeDto.getTimeZone()));

                        // 2. 한국 시간(UTC+9)으로 변환
                        ZonedDateTime kstTime = sourceTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));

                        // 3. 결과에서 시간만 추출
                        LocalTime resultTime = kstTime.toLocalTime();
                        String resetTime = resultTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                        // 결과 출력 포맷 설정
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        model.addAttribute("resetTxt","데이터 충전: "+resetTime+"(한국 시간)<br/>이용 일수: "+resetTime+"(한국 시간)");

                    }
                }
            }



            if(apiPurchaseItemDto.isApiPurchaseItemIsDaily()){
                String dailySize = apiPurchaseItemProcutIds[apiPurchaseItemProcutIds.length-1];
                if(dailySize.equals("U") || dailySize.equals("Unlimited") ){
                    model.addAttribute("totalUsageTxt", "unlimited");
                    model.addAttribute("totalUsage","unlimited");//현재 사이클 전체데이터
                }else{
                    //refuelingTotal 아직 안됨
                    //double dataTotal = (double) esimMap.get("refuelingTotal");
                    double dataTotal = 0d;
                    if (dailySize.indexOf("GB") > -1) {
                        // "GB"를 제거하고 숫자로 바꾼 뒤 1024를 곱함
                        dataTotal = Double.parseDouble(dailySize.replace("GB", "")) * 1024;
                    } else if (dailySize.indexOf("MB") > -1 || dailySize.indexOf("M") > -1) {
                        // "MB"를 제거하고 숫자로 바꿈
                        dataTotal = Double.parseDouble(dailySize.replace("MB", "").replace("M", ""));
                    }

                    if(dataTotal/1024>1){
                        model.addAttribute("totalUsageTxt",(Math.round((dataTotal/1024) * 100) / 100.0) + "GB");
                    }else{
                        model.addAttribute("totalUsageTxt",(dataTotal) + "MB");
                    }
                    model.addAttribute("totalUsage",dataTotal);//현재 사이클 전체데이터
                }
            }else{
                double dataTotal = esimMap.get("dataTotal")!=null?(Double.parseDouble(esimMap.get("dataTotal").toString())):0;
                if(dataTotal/1024>1){
                    model.addAttribute("totalUsageTxt",(Math.round((dataTotal/1024) * 100) / 100.0) + "GB");
                }else{
                    model.addAttribute("totalUsageTxt",(dataTotal) + "MB");
                }
                model.addAttribute("totalUsage",dataTotal);//현재 사이클 전체데이터
            }
            double usageData = 0D;
            if(esimMap.get("dataUsage")!=null)
                 usageData = Double.parseDouble(esimMap.get("dataUsage").toString());
            if(usageData/1024>1){
                model.addAttribute("usageTxt",(Math.round((usageData/1024) * 100) / 100.0) + "GB");
            }else{
                model.addAttribute("usageTxt",(usageData) + "MB");
            }
            model.addAttribute("usage",usageData); //현재 사이클 사용량



            model.addAttribute("iccid",iccid);
            //TODO 충전기능 오픈전까지 충전불가 아래삭제
            model.addAttribute("chargeYN","N");//충전불가
        }else{
            throw new Exception();
        }

    }

    public HashMap contextLoads5(TopupOrderDto topupOrderDto) {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), topupOrderDto.getOrderId());

        System.setProperty("https.protocols","TLSv1.2");


        return   getEsimPackageRenew(topupOrderDto);


    }


    private static HashMap getEsimPackagePurchase(String esimProductId, String userId, String orderIdWithQuantityNumber, Long orderId) throws Exception{
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE.getExplain(), orderId);

        String serviceName = "eSIMApi/v2/order/create";
        String url = baseUrl +serviceName;

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(60000 * 1000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(60000); // 커넥션 최대 시간
        factory.setReadTimeout(60000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = getToken(tugeRedisRepository);
        String requestId = UUID.randomUUID().toString();
        Gson gson = new Gson();
        Map<String,Object> data = new HashMap<>();


        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("productCode",esimProductId);
        jsonObject.put("channelOrderNo",orderIdWithQuantityNumber);
        jsonObject.put("idempotencyKey",requestId);

        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);

        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE_END0.getExplain() + response.getBody(), orderId);

        return response.getBody();
    }



    private HashMap getEsimPackageRenew(TopupOrderDto topupOrderDto) {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.TOPUP.getExplain(), topupOrderDto.getOrderId());

        String serviceName = "eSIMApi/v2/order/renew";
        String url = baseUrl +serviceName;

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(60000 * 1000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(60000); // 커넥션 최대 시간
        factory.setReadTimeout(60000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        Gson gson = new Gson();
        HashMap<String,Object> topupParam = gson.fromJson(topupOrderDto.getTopupParamJson(), HashMap.class);
        // create headers
        HttpHeaders headers = getToken(tugeRedisRepository);
        String requestId = UUID.randomUUID().toString();


        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("productCode",topupParam.get("apiPurchaseItemProcutId"));
        jsonObject.put("iccid",topupOrderDto.getEsimIccid());
        jsonObject.put("channelOrderNo","renewOrderNo"+topupOrderDto.getId());
        jsonObject.put("idempotencyKey",requestId);

        esimApiIngStepLogsService.insertRest(headers,jsonObject, topupOrderDto.getOrderId());

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);

        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.TOPUP_END.getExplain() + response.getBody(), topupOrderDto.getOrderId());

        return response.getBody();
    }





    private static HashMap getEsimStatus(String orderId, String iccid) throws Exception {

        String serviceName = "eSIMApi/v2/order/usage";

        String url = baseUrl +  serviceName;

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(60000 * 1000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(60000); // 커넥션 최대 시간
        factory.setReadTimeout(60000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = getToken(tugeRedisRepository);


        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("orderNo",orderId);


        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        return response.getBody();

    }




    private static HashMap getEsimStatus2(String orderId) throws Exception {

        String serviceName = "eSIMApi/v2/order/orders";

        String url = baseUrl  + serviceName;

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(60000 * 1000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(60000); // 커넥션 최대 시간
        factory.setReadTimeout(60000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = getToken(tugeRedisRepository);


        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("orderNo", orderId);


        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        return response.getBody();

    }

    public static HttpHeaders getToken(TugeRedisRepository tugeRedisRepository){
        Optional<TugeRedisToken> byAccountId = tugeRedisRepository.findById(accountId);
        boolean existToken = true;
        if (byAccountId.isPresent()){
            long buffer = 600000;//10분유효기간
            TugeRedisToken tugeRedisToken = byAccountId.orElse(null);
            // mills
            Long timeStamp = tugeRedisToken.getTimeStamp();

            if(tugeRedisToken.getExpiresIn() !=null){
                Long expiresIn = tugeRedisToken.getExpiresIn() * 1000;
                Date now = new Date();
                long time = now.getTime();
                // 인증시간 만료
                if((timeStamp + expiresIn) < time + buffer){
                    existToken = false;
                }else{
                    return tugeRedisToken.returnHeaderMap();
                }
            }else{
                existToken = false;
            }

        }else{
            existToken = false;
        }
        if(!existToken){
            String serviceName = "oauth/token";
            String url = baseUrl +  serviceName;
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setConnectionRequestTimeout(60000 * 1000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
            factory.setConnectTimeout(60000); // 커넥션 최대 시간
            factory.setReadTimeout(60000); // 읽기 최대 시간

            RestTemplate restTemplate = new RestTemplate(factory);

            // create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            Map<String,Object> jsonObject = new HashMap<>();
            jsonObject.put("accountId",accountId);
            jsonObject.put("secret",secretkey);
            HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


            ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

            HashMap result =  response.getBody();
            if(result.get("code").toString().equals("0000")){
                HashMap<String, Object> item = (HashMap<String, Object>) result.get("data");

                Date now = new Date();
                Long timeStamp = now.getTime();
                TugeRedisToken tugeRedisToken = new TugeRedisToken();
                tugeRedisToken.setAccountId(accountId);
                tugeRedisToken.setTimeStamp(timeStamp);
                tugeRedisToken.setAccessToken(item.get("token").toString());
                tugeRedisToken.setExpiresIn(Long.parseLong(item.get("expires").toString()));

                TugeRedisToken save = tugeRedisRepository.save(tugeRedisToken);

                return save.returnHeaderMap();
            }else{
                return null;
            }
        }
        return null;
    }


}
