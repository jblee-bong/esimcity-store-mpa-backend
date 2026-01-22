package com.naver.naverspabackend.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.naver.naverspabackend.dto.ApiPurchaseItemDto;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.TopupOrderDto;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.service.apipurchaseitem.ApiPurchaseItemService;
import com.naver.naverspabackend.service.esimapiingsteplogs.EsimApiIngStepLogsService;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EsimAccessUtil {


    public static String clientId;

    static String clientSecret;

    static String baseUrl;

    static String active;

    public static EsimApiIngStepLogsService esimApiIngStepLogsService;


    public static ApiPurchaseItemService apiPurchaseItemService;
    public static OrderMapper orderMapper;
    public EsimAccessUtil(String clientId, String clientSecret, String baseUrl, EsimApiIngStepLogsService esimApiIngStepLogsService,ApiPurchaseItemService apiPurchaseItemService, OrderMapper orderMapper){
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseUrl = baseUrl;
        this.esimApiIngStepLogsService = esimApiIngStepLogsService;
        this.apiPurchaseItemService = apiPurchaseItemService;
        this.orderMapper = orderMapper;
    }



    public static HashMap contextLoads2( String esimProductId,String esimProductDays, Long orderId) throws Exception {

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);
        System.setProperty("https.protocols","TLSv1.2");
        return  getEsimPackagePurchase(esimProductId, esimProductDays, orderId);


    }
    public static HashMap contextLoads3(String activationRequestId, Long orderId) throws Exception {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);
        System.setProperty("https.protocols","TLSv1.2");
        return getEsimPurchaseStatus(activationRequestId,orderId);
    }

    public static void contextLoads4(String esimTranNo,String realOrderId, String iccid, Model model) throws Exception {

        System.setProperty("https.protocols","TLSv1.2");
        HashMap resultStatus = getEsimPurchaseStatus2(iccid);

        if( (Boolean) resultStatus.get("success")){
            Map<String, Object> resultStatusObj = (Map<String, Object>) resultStatus.get("obj");
            List<HashMap> esimList = (List<HashMap>) resultStatusObj.get("esimList");
            HashMap esimStatus = null;
            if(esimList.size()>0){
                esimStatus = esimList.get(0);
            }else{
                throw new Exception();
            }

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id",realOrderId);
            OrderDto esimaccessOrder = orderMapper.findById(paramMap);
            if(esimaccessOrder==null){
                throw new Exception();
            }
            ApiPurchaseItemDto param = new ApiPurchaseItemDto();
            param.setApiPurchaseItemProcutId(esimaccessOrder.getEsimProductId());
            param.setApiPurchaseItemType(ApiType.ESIMACCESS.name());
            ApiPurchaseItemDto apiPurchaseItemDto = apiPurchaseItemService.findById(param);
            if(apiPurchaseItemDto==null){
                throw new Exception();
            }

            DateTimeFormatter newDtFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
            // String 타입을 Date 타입으로 변환
            int dataType = (int) esimStatus.get("activeType");
            boolean isDaily = dataType!=1;
            long bytes = Long.parseLong(esimStatus.get("totalVolume").toString());
            double totalDataMb = bytes / (1024.0 * 1024.0);
            //전체용량
            model.addAttribute("totalUsage",totalDataMb);
            //사용량

            long usageBytes = Long.parseLong(esimStatus.get("orderUsage").toString());
            double dataUsageMb = Math.round(usageBytes / (1024.0 * 1024.0) * 100.0) / 100.0;
            model.addAttribute("usage",dataUsageMb);
            model.addAttribute("iccid",iccid);
            model.addAttribute("isDaily",isDaily);//매일데이터 리셋 유부
            model.addAttribute("resetTxt",MakeResetTimeUtil.makeTsimEsimAccessText(apiPurchaseItemDto));//종료일 - 매일일경우 리셋일

            if((esimStatus.get("esimStatus").toString().equals("IN_USE") || esimStatus.get("esimStatus").toString().equals("USED_UP"))
                    && esimStatus.get("activateTime")!=null && !esimStatus.get("activateTime").toString().equals("")){

                // 1. 문자열을 ZonedDateTime 객체로 파싱
                ZonedDateTime utcTime = ZonedDateTime.parse(esimStatus.get("activateTime").toString(), newDtFormat);
                // 2. 한국 시간대(Asia/Seoul)로 변환
                ZonedDateTime kstTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                // 3. 원하는 출력 형식 지정 (yyyy-MM-dd HH:mm)
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                // 6. 결과 출력
                model.addAttribute("useStartDate",kstTime.format(outputFormatter));//활성화시작일

                //몇일동안쓸수 있는지,
                Integer totalDuration = (Integer) esimStatus.get("totalDuration");
                ZonedDateTime endKst = kstTime.plusDays(totalDuration);
                model.addAttribute("useEndDate", endKst.format(outputFormatter));     // 7일 후 종료일

                //현재 시간과 비교하여 만료 여부 확인 (옵션)
                boolean isEnded = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).isAfter(endKst);
                model.addAttribute("end", isEnded);
            }


            //이거아무래 expiredTime이 chage 종료시간같은데,, 현재 사이클 종료시간은 따로없는거같긴한데 확인 필요
            if(esimStatus.get("expiredTime")!=null && !esimStatus.get("expiredTime").toString().equals("")){
                // 1. 문자열을 ZonedDateTime 객체로 파싱
                ZonedDateTime utcTime = ZonedDateTime.parse(esimStatus.get("expiredTime").toString(), newDtFormat);
                // 2. 한국 시간대(Asia/Seoul)로 변환
                ZonedDateTime kstTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                // 3. 원하는 출력 형식 지정 (yyyy-MM-dd HH:mm)
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                // 6. 결과 출력
                model.addAttribute("useEndDate",kstTime.format(outputFormatter));//활성화시작일

                ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

                if((Integer) esimStatus.get("supportTopUpType")==2 && !nowKst.isAfter(kstTime)){

                    List<Map<String,Object>> topupList  = getEsimTopupPackages(iccid,apiPurchaseItemDto.getApiPurchaseCoverDomainCode(), null);

                    List<Map<String,String>> apiPurchaseItemList = new ArrayList<>();
                    //총데이터만 충전가능. 충전시 기존거 + 새로운거로 데이터 확인가능
                    for(Map<String,Object> topup : topupList){
                        Map<String,String> apiPurchaseItem = new HashMap<>();
                        apiPurchaseItem.put("channel_dataplan_id",topup.get("packageCode").toString() + "|^|"+topup.get("volume").toString()+ "|^|"+topup.get("price").toString()+ "|^|"+topup.get("duration").toString());
                        apiPurchaseItem.put("channel_dataplan_day",((Integer) topup.get("duration")) +"일");

                        long chargeBytes = Long.parseLong(topup.get("volume").toString());
                        long gbBoundary = 1024L * 1024 * 1024;
                        if (chargeBytes >= gbBoundary) {
                            // GB로 변환 (소수점 둘째자리까지)
                            double gbValue = (double) chargeBytes / gbBoundary;
                            apiPurchaseItem.put("channel_dataplan_data",String.format("%.2f GB", gbValue) );
                        } else {
                            // MB로 변환 (소수점 둘째자리까지)
                            double mbValue = (double) chargeBytes / (1024 * 1024);
                            apiPurchaseItem.put("channel_dataplan_data",String.format("%.2f MB", mbValue) );
                        }

                        apiPurchaseItemList.add(apiPurchaseItem);
                    }

                    model.addAttribute("apiPurchaseItemList",apiPurchaseItemList);//충전 가능리스트

                    // 현재 시간과의 차이 계산
                    long limitTimeMillis = kstTime.toInstant().toEpochMilli(); // 이제 이 시간은 '진짜 만료' 10분 전 시간입니다.
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


                    } else {
                        model.addAttribute("chargeYN","N");//충전불가
                    }
                }else{
                    model.addAttribute("chargeYN","N");
                }
            }

        }else{
            throw new Exception();
        }
    }
    public HashMap contextLoads5(TopupOrderDto topupOrderDto) throws Exception {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), topupOrderDto.getOrderId());
        System.setProperty("https.protocols","TLSv1.2");
        return getEsimTopup(topupOrderDto);
    }




    public static List<Map<String, Object>> getEsimTopupPackages(String iccid, String locationCode, String packageCode) throws Exception {


        String url = baseUrl  + "/v1/open/package/list";

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        /// create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));

        headers.add("RT-AccessCode",clientId);

        Gson gson = new Gson();
        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("packageCode",packageCode);
        jsonObject.put("locationCode",locationCode);
        jsonObject.put("iccid", iccid);
        jsonObject.put("type","topup");


        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        if((Boolean) response.getBody().get("success")){
            Map<String,Object> obj = (Map<String, Object>) response.getBody().get("obj");
            return  (List<Map<String, Object>>) obj.get("packageList");
        }else{
            throw new Exception();
        }

    }

    private static HashMap getEsimTopup(TopupOrderDto topupOrderDto) throws Exception{
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.TOPUP.getExplain(), topupOrderDto.getOrderId());
        String url = baseUrl + "/v1/open/esim/topup";
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간
        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("RT-AccessCode",clientId);
        Gson gson = new Gson();
        HashMap<String,Object> topupParam = gson.fromJson(topupOrderDto.getTopupParamJson(), HashMap.class);

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("iccid",topupOrderDto.getEsimIccid());
        jsonObject.put("packageCode",topupParam.get("apiPurchaseItemProcutId"));
        jsonObject.put("transactionId",topupOrderDto.getTokenId());


        esimApiIngStepLogsService.insertRest(headers,jsonObject, topupOrderDto.getOrderId());

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.TOPUP_END.getExplain() + response.getBody(), topupOrderDto.getOrderId());

        return response.getBody();

    }

    public static void contextLoads4UsageUse(String esimTranNo,String realOrderId, String iccid, Model model) throws Exception {
        //TODO usage 사용

        System.setProperty("https.protocols","TLSv1.2");
        HashMap result = getEsimStatus(esimTranNo);
        HashMap resultStatus = getEsimPurchaseStatus2(iccid);

        if((Boolean) result.get("success") && (Boolean) resultStatus.get("success")){
            Map<String, Object> resultStatusObj = (Map<String, Object>) resultStatus.get("obj");
            List<HashMap> esimList = (List<HashMap>) resultStatusObj.get("esimList");
            HashMap esimStatus = null;
            if(esimList.size()>0){
                esimStatus = esimList.get(0);
            }else{
                throw new Exception();
            }

            Map<String, Object> obj = (Map<String, Object>) result.get("obj");
            List<Map<String,Object>> esimUsageList = (List<Map<String, Object>>) obj.get("esimUsageList");

            if(esimUsageList.size()==0){
                throw new Exception();
            }
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id",realOrderId);
            OrderDto esimaccessOrder = orderMapper.findById(paramMap);
            if(esimaccessOrder==null){
                throw new Exception();
            }
            ApiPurchaseItemDto param = new ApiPurchaseItemDto();
            param.setApiPurchaseItemProcutId(esimaccessOrder.getEsimProductId());
            param.setApiPurchaseItemType(ApiType.ESIMACCESS.name());
            ApiPurchaseItemDto apiPurchaseItemDto = apiPurchaseItemService.findById(param);
            if(apiPurchaseItemDto==null){
                throw new Exception();
            }

            Map<String,Object> esimUsage = esimUsageList.get(0);
            DateTimeFormatter newDtFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
            // String 타입을 Date 타입으로 변환
            Calendar cal = Calendar.getInstance();
            int dataType = (int) esimStatus.get("activeType");
            boolean isDaily = dataType!=1;
            String totalData = esimUsage.get("totalData").toString();

            // String을 long으로 변환 (Convert String to long)
            long bytes = Long.parseLong(totalData);
            double totalDataMb = bytes / (1024.0 * 1024.0);
            //전체용량
            model.addAttribute("totalUsage",totalDataMb);
            //사용량
            String dataUsage = esimUsage.get("dataUsage").toString();
            long usageBytes = Long.parseLong(dataUsage);
            double dataUsageMb = usageBytes / (1024.0 * 1024.0);
            model.addAttribute("usage",dataUsageMb);
            model.addAttribute("iccid",iccid);
            model.addAttribute("isDaily",isDaily);//매일데이터 리셋 유부
            if(isDaily){
                model.addAttribute("resetAt","");//종료일 - 매일일경우 리셋일
                model.addAttribute("resetTxt",MakeResetTimeUtil.makeTsimEsimAccessText(apiPurchaseItemDto));//종료일 - 매일일경우 리셋일
            }
            if(esimStatus.get("activeType").toString().equals("2") && esimStatus.get("activateTime")!=null && !esimStatus.get("activateTime").toString().equals("")){

                // 1. 문자열을 ZonedDateTime 객체로 파싱
                ZonedDateTime utcTime = ZonedDateTime.parse(esimStatus.get("activateTime").toString(), newDtFormat);
                // 2. 한국 시간대(Asia/Seoul)로 변환
                ZonedDateTime kstTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                // 3. 원하는 출력 형식 지정 (yyyy-MM-dd HH:mm)
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                // 6. 결과 출력
                model.addAttribute("useStartDate",kstTime.format(outputFormatter));//활성화시작일

                //몇일동안쓸수 있는지,
                Integer totalDuration = (Integer) esimStatus.get("totalDuration");
                ZonedDateTime endKst = kstTime.plusDays(totalDuration);
                model.addAttribute("useEndDate", endKst.format(outputFormatter));     // 7일 후 종료일

                //현재 시간과 비교하여 만료 여부 확인 (옵션)
                boolean isEnded = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).isAfter(endKst);
                model.addAttribute("end", isEnded);
            }


            //이거아무래 expiredTime이 chage 종료시간같은데,, 현재 사이클 종료시간은 따로없는거같긴한데 확인 필요 TODO
            if(esimStatus.get("expiredTime")!=null && !esimStatus.get("expiredTime").toString().equals("")){
                // 1. 문자열을 ZonedDateTime 객체로 파싱
                ZonedDateTime utcTime = ZonedDateTime.parse(esimStatus.get("expiredTime").toString(), newDtFormat);
                // 2. 한국 시간대(Asia/Seoul)로 변환
                ZonedDateTime kstTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                // 3. 원하는 출력 형식 지정 (yyyy-MM-dd HH:mm)
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                // 6. 결과 출력
                model.addAttribute("useEndDate",kstTime.format(outputFormatter));//활성화시작일

                ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                model.addAttribute("end", nowKst.isAfter(kstTime));

                if(apiPurchaseItemDto.isApiPurchaseIsCharge()){
                    List<Map<String,String>> apiPurchaseItemList = new ArrayList<>();
                    model.addAttribute("apiPurchaseItemList",apiPurchaseItemList);//충전 가능리스트

                    // 현재 시간과의 차이 계산
                    long limitTimeMillis = kstTime.toInstant().toEpochMilli(); // 이제 이 시간은 '진짜 만료' 10분 전 시간입니다.
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


                    } else {
                        model.addAttribute("chargeYN","N");//충전불가
                    }
                }
            }

        }else{
            throw new Exception();
        }
    }
    private static HashMap getEsimStatus(String esimTransNo) {
        String url = baseUrl + "/v1/open/esim/usage/query";
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간
        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("RT-AccessCode",clientId);
        List<String> esimTranNoList = new ArrayList<>();
        esimTranNoList.add(esimTransNo);
        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("esimTranNoList",esimTranNoList);

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);
        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

        return response.getBody();
    }

    private static HashMap getEsimPurchaseStatus(String activationRequestId, Long orderId) {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS.getExplain(), orderId);

        String url = baseUrl + "/v1/open/esim/query";
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간
        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("RT-AccessCode",clientId);
        Map<String,Object> pager = new HashMap<>();
        pager.put("pageSize",10);
        pager.put("pageNum",1);

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("orderNo",activationRequestId);
        jsonObject.put("pager",pager);

        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS_END.getExplain() + response.getBody(), orderId);

        return response.getBody();
    }


    private static HashMap getEsimPurchaseStatus2(String iccid) {

        String url = baseUrl + "/v1/open/esim/query";
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간
        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("RT-AccessCode",clientId);
        Map<String,Object> pager = new HashMap<>();
        pager.put("pageSize",10);
        pager.put("pageNum",1);

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("iccid",iccid);
        jsonObject.put("pager",pager);


        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

        return response.getBody();
    }


    private static HashMap getEsimPackagePurchase(String esimProductId,  String esimProductDays, Long orderId) {

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE.getExplain(), orderId);
        String url = baseUrl + "/v1/open/esim/order";
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("RT-AccessCode",clientId);

        List<Map<String,Object>> packageInfoList = new ArrayList<>();
        Map<String,Object> packageInfo = new HashMap<>();


        ApiPurchaseItemDto param = new ApiPurchaseItemDto();
        param.setApiPurchaseItemProcutId(esimProductId);
        param.setApiPurchaseItemType(ApiType.ESIMACCESS.name());
        ApiPurchaseItemDto apiPurchaseItemDto = apiPurchaseItemService.findById(param);

        packageInfo.put("packageCode",esimProductId);
        packageInfo.put("count",1);//몇개구매할껀지
        packageInfo.put("price",Double.parseDouble(apiPurchaseItemDto.getApiPurchasePrice()) * 10000); //구매가격을 넣어줌 가격이 틀리면 실패 (가격변동막아줌)
        if(esimProductDays!=null && !esimProductDays.trim().equals(""))
            packageInfo.put("periodNum",Integer.parseInt(esimProductDays));

        packageInfoList.add(packageInfo);


        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("transactionId",UUID.randomUUID().toString().substring(0,5) + new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date()));


        jsonObject.put("amount",getAmount(apiPurchaseItemDto.getApiPurchasePrice(),esimProductDays));
        jsonObject.put("packageInfoList",packageInfoList);

        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE_END.getExplain() + response.getBody(), orderId);


        return response.getBody();
    }
    private static Long getAmount(String amount, String esimProductDays){
        //double finalValue =  Double.parseDouble(amount) * 10000 * discountRatio(esimProductDays); 할인율 뺸 금액인가봄
        double finalValue =  Double.parseDouble(amount) * 10000;

        if(esimProductDays==null || esimProductDays.trim().equals("")){
            return  Math.round(finalValue);
        }
        return Math.round(finalValue) * Integer.parseInt(esimProductDays);
    }
    private static Double discountRatio(String daily){
        if(daily==null || daily.trim().equals("")){
            return  1.0;
        }
        Integer dailyCount = Integer.parseInt(daily);
        if(dailyCount>=1 && dailyCount<=4){
            return (Double) 0.96;
        }else if(dailyCount>=5 && dailyCount<=9){
            return (Double) 0.92;
        }else if(dailyCount>=10 && dailyCount<=19){
            return (Double) 0.89;
        }else if(dailyCount>=20 && dailyCount<=29){
            return (Double) 0.85;
        }else{
            return (Double) 0.82;
        }
    }


}
