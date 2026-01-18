package com.naver.naverspabackend.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.naver.naverspabackend.dto.ApiPurchaseItemDto;
import com.naver.naverspabackend.dto.TopupOrderDto;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
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
import java.util.*;
public class TsimUtil {


    public static String tsimAccount;

    static String tsimSecret;

    static String baseUrl;

    static String active;

    public static EsimApiIngStepLogsService esimApiIngStepLogsService;

    public static ApiPurchaseItemService apiPurchaseItemService;
    public TsimUtil(ApiPurchaseItemService apiPurchaseItemService, String tsimAccount, String tsimSecret, String baseUrl, EsimApiIngStepLogsService esimApiIngStepLogsService, String active){
        this.tsimAccount = tsimAccount;
        this.tsimSecret = tsimSecret;
        this.baseUrl = baseUrl;
        this.esimApiIngStepLogsService = esimApiIngStepLogsService;
        this.active = active;
        this.apiPurchaseItemService = apiPurchaseItemService;
    }


    public static List<HashMap<String, Object>> contextLoads1() throws Exception {

        System.setProperty("https.protocols", "TLSv1.2");


        HashMap esimPackages = getEsimPackages();


        List<HashMap<String, Object>> esimPagekageList = (List<HashMap<String, Object>>) esimPackages.get("result");

        return esimPagekageList;
    }

    public static HashMap contextLoads2(String userId, String esimProductId, Long orderId) throws Exception {

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);
        System.setProperty("https.protocols","TLSv1.2");


        HashMap esimResult =  getEsimPackagePurchase(esimProductId,userId, orderId);


        return esimResult;
    }


    public static HashMap contextLoads3(String activationRequestId, Long orderId) throws Exception {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);
        System.setProperty("https.protocols","TLSv1.2");
        return getEsimPurchaseStatus(activationRequestId,orderId);
    }

    public static void contextLoads4(String activationRequestId, String iccid, Model model) throws Exception {
        System.setProperty("https.protocols","TLSv1.2");
        HashMap esimMap =  getEsimPurchaseStatus(activationRequestId, null);

        Map<String,Object> result = (Map<String, Object>) esimMap.get("result");

        if(esimMap.get("msg").toString().equals("Success")){
            String topupId =  result.get("topup_id").toString();
            List<String> deviceIds = (List<String>) result.get("device_ids");
            String deviceId = deviceIds.get(0).toString();
            esimMap =  getEsimStatus2(topupId,deviceId);
            SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            // String 타입을 Date 타입으로 변환
            Calendar cal = Calendar.getInstance();
            if(esimMap.get("msg").toString().equals("Success")){
                result = (Map<String, Object>) esimMap.get("result");

                boolean isDaily = (boolean) result.get("is_daily");
                double m = 0;

                if(result.get("channel_dataplan_name")!=null){
                    Integer dataIndex = null;
                    String[] channelDataplanNames = result.get("channel_dataplan_name").toString().split(" ");
                    if(isDaily){
                        if(result.get("channel_dataplan_name").toString().indexOf("Unlimited")>-1){
                            esimMap.put("totalUsage","unlimited");
                            model.addAttribute("totalUsage","unlimited");
                        }else{
                            for(int i=0;i< channelDataplanNames.length;i++){
                                if(channelDataplanNames[i].equals("Daily")){
                                    dataIndex = i+1;
                                    break;
                                }
                            }
                        }
                    }else{
                        if(result.get("channel_dataplan_name").toString().indexOf("Unlimited")>-1){
                            esimMap.put("totalUsage","unlimited");
                            model.addAttribute("totalUsage","unlimited");
                        }else{
                            for(int i=0;i< channelDataplanNames.length;i++){
                                if(channelDataplanNames[i].equals("Days")){
                                    dataIndex = i+1;
                                    break;
                                }
                            }
                        }
                    }
                    if(dataIndex!=null){
                        String dailySize = channelDataplanNames[dataIndex];
                        if(dailySize.indexOf("MB")>-1){

                            model.addAttribute("totalUsage",dailySize.replace("MB",""));//현재 사이클 전체데이터
                        }
                        else if(dailySize.indexOf("GB")>-1){
                            model.addAttribute("totalUsage",Double.parseDouble(dailySize.replace("GB",""))*1024);//현재 사이클 전체데이터
                        }
                        else
                            model.addAttribute("totalUsage","unlimited");//현재 사이클 전체데이터

                    }else {
                        esimMap.put("dataTotal","NONE");
                    }
                }

                if(isDaily){
                    DecimalFormat dec = new DecimalFormat("0.00");
                    List<Map<String, Object>> dataUsageDaily = (List<Map<String, Object>>) result.get("data_usage_daily");
                    Double size = (double) 0;
                    for(int i=0;i<dataUsageDaily.size();i++){
                        if(i==0){
                            size += Double.parseDouble(dataUsageDaily.get(i).get("total_usage_kb").toString());
                        }
                    }
                    if(result.get("today_usage")==null || result.get("today_usage").toString().equals("")){
                        m = size/1024;
                    }else{
                        m = Double.parseDouble(result.get("today_usage").toString());
                    }

                    if(result.get("daily_reset_time").toString()!=null && !result.get("daily_reset_time").toString().equals("")){
                        Date restDate = newDtFormat.parse(result.get("daily_reset_time").toString());
                        cal.setTime(restDate);
                        cal.add(Calendar.HOUR, 1);//중국시간이라 +1시간해줘야 한국시간
                        String resetDateString = newDtFormat.format(cal.getTime());
                        model.addAttribute("resetAt",resetDateString);//종료일 - 매일일경우 리셋일
                        model.addAttribute("resetTxt","데이터 충전: "+resetDateString + "(한국 시간)<br/>이용 일수: 활성화 시점부터 24시간");

                    }else{
                        ApiPurchaseItemDto param = new ApiPurchaseItemDto();
                        param.setApiPurchaseItemProcutId(result.get("channel_dataplan_id").toString());
                        param.setApiPurchaseItemType(ApiType.TSIM.name());
                        model.addAttribute("resetAt","");//종료일 - 매일일경우 리셋일
                        model.addAttribute("resetTxt",MakeResetTimeUtil.makeTsimResetText(apiPurchaseItemService.findById(param)));//종료일 - 매일일경우 리셋일
                    }
                    model.addAttribute("chargeYN","NONE");//DAILY상품
                }else{


                    ApiPurchaseItemDto param = new ApiPurchaseItemDto();
                    param.setApiPurchaseItemProcutId(result.get("channel_dataplan_id").toString());
                    param.setApiPurchaseItemType(ApiType.TSIM.name());
                    List<ApiPurchaseItemDto> apiPurchaseItemDtoList = apiPurchaseItemService.selectApiPurchaseItemListForTopup(param);
                    List<Map<String,String>> apiPurchaseItemList = new ArrayList<>();
                    for(ApiPurchaseItemDto apiPurchaseItemDto : apiPurchaseItemDtoList){
                        Map<String,String> apiPurchaseItem = new HashMap<>();
                        apiPurchaseItem.put("channel_dataplan_id",apiPurchaseItemDto.getApiPurchaseItemProcutId());
                        apiPurchaseItem.put("channel_dataplan_day",apiPurchaseItemDto.getApiPurchaseItemDays() +"일" );
                        apiPurchaseItem.put("channel_dataplan_data",apiPurchaseItemDto.getApiPurchaseDataTotal() );
                        apiPurchaseItemList.add(apiPurchaseItem);
                    }
                    model.addAttribute("apiPurchaseItemList",apiPurchaseItemList);//충전 가능리스트

                    // 1. 기존 코드: 중국 시간을 한국 시간으로 변환 (+1시간)
                    Date topupExpiredDate = newDtFormat.parse(result.get("esim_topup_expire_time").toString());
                    cal.setTime(topupExpiredDate);
                    cal.add(Calendar.HOUR, 1); //중국시간이라 +1시간해줘야 한국시간
                    // 2. ★ 핵심: 충전 가능 시간을 만료 10분 전으로 당김
                    cal.add(Calendar.MINUTE, -10);
                    // 3. 현재 시간과의 차이 계산
                    long limitTimeMillis = cal.getTimeInMillis(); // 이제 이 시간은 '진짜 만료' 10분 전 시간입니다.
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
                        model.addAttribute("deviceId",deviceId);//충전파라미터


                    } else {
                        model.addAttribute("chargeYN","N");//충전불가
                    }



                    model.addAttribute("resetAt","");//종료일 - 매일일경우 리셋일
                    m = Double.parseDouble(result.get("data_usage").toString());
                }
                if(result.get("active_time")!=null && !result.get("active_time").toString().equals("")){
                    Date useStartDate = newDtFormat.parse(result.get("active_time").toString());
                    cal.setTime(useStartDate);
                    cal.add(Calendar.HOUR, 1); //중국시간이라 +1시간해줘야 한국시간
                    String useStartDateString = newDtFormat.format(cal.getTime());
                    model.addAttribute("useStartDate",useStartDateString);//활성화시작일
                }
                if(result.get("expire_time")!=null && !result.get("expire_time").toString().equals("")){
                    Date useEndDate = newDtFormat.parse(result.get("expire_time").toString());
                    cal.setTime(useEndDate);
                    cal.add(Calendar.HOUR, 1); //중국시간이라 +1시간해줘야 한국시간
                    String expiredDateString = newDtFormat.format(cal.getTime());

                    model.addAttribute("useEndDate",expiredDateString);//사용종료일
                    Date expiredAt =  newDtFormat.parse(expiredDateString);
                    Date now = new Date();
                    model.addAttribute("end",now.after(expiredAt));
                }




                Map<String,Object> item = new HashMap<>();
                model.addAttribute("iccid",iccid);

                model.addAttribute("isDaily",isDaily);//매일데이터 리셋 유부
                model.addAttribute("usage",Math.round(m * 100) / 100.0);


                //TODO 충전기능 오픈전까지 충전불가 아래삭제
                model.addAttribute("chargeYN","N");//충전불가
            }
        }else{
            throw new Exception();
        }

    }


    public HashMap contextLoads5(TopupOrderDto topupOrderDto) throws Exception{
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), topupOrderDto.getOrderId());
        System.setProperty("https.protocols","TLSv1.2");
        return getEsimTopup(topupOrderDto);
    }




    private static HashMap getEsimStatus2(String topupId, String deviceId) throws Exception{
        String url = baseUrl + "/tsim/v1/deviceDetail";
        Map<String,String> computedHeader = ComputeSHA256();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간
        RestTemplate restTemplate = new RestTemplate(factory);
        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("TSIM-ACCOUNT",tsimAccount);
        headers.add("TSIM-NONCE",computedHeader.get("TSIM_NONCE"));
        headers.add("TSIM-TIMESTAMP",computedHeader.get("TSIM_TIMESTAMP"));
        headers.add("TSIM-SIGN",computedHeader.get("TSIM_SIGN"));

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("device_id",deviceId);
        jsonObject.put("topup_id",topupId);

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);


        return response.getBody();
    }
    private static HashMap getEsimPurchaseStatus(String activationRequestId, Long orderId) throws Exception{
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS.getExplain(), orderId);

        String url = baseUrl + "/tsim/v1/topupDetail";
        Map<String,String> computedHeader = ComputeSHA256();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간
        RestTemplate restTemplate = new RestTemplate(factory);
        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("TSIM-ACCOUNT",tsimAccount);
        headers.add("TSIM-NONCE",computedHeader.get("TSIM_NONCE"));
        headers.add("TSIM-TIMESTAMP",computedHeader.get("TSIM_TIMESTAMP"));
        headers.add("TSIM-SIGN",computedHeader.get("TSIM_SIGN"));

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("topup_id",activationRequestId);

        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS_END.getExplain() + response.getBody(), orderId);

        return response.getBody();
    }
    private static HashMap getEsimTopup(TopupOrderDto topupOrderDto) throws Exception{
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.TOPUP.getExplain(), topupOrderDto.getOrderId());
        String url = baseUrl + "/tsim/v1/topup";
        Map<String,String> computedHeader = ComputeSHA256();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        Gson gson = new Gson();
        HashMap<String,Object> topupParam = gson.fromJson(topupOrderDto.getTopupParamJson(), HashMap.class);
        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("TSIM-ACCOUNT",tsimAccount);
        headers.add("TSIM-NONCE",computedHeader.get("TSIM_NONCE"));
        headers.add("TSIM-TIMESTAMP",computedHeader.get("TSIM_TIMESTAMP"));
        headers.add("TSIM-SIGN",computedHeader.get("TSIM_SIGN"));
        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("device_ids",topupParam.get("deviceId"));
        jsonObject.put("channel_dataplan_id",topupParam.get("apiPurchaseItemProcutId"));

        esimApiIngStepLogsService.insertRest(headers,jsonObject, topupOrderDto.getOrderId());


        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);
        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.TOPUP_END.getExplain() + response.getBody(), topupOrderDto.getOrderId());

        return response.getBody();
    }

    private static HashMap getEsimPackagePurchase(String esimProductId, String userId, Long orderId) throws Exception{
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE.getExplain(), orderId);

        String url = baseUrl + "/tsim/v1/esimSubscribe";
        Map<String,String> computedHeader = ComputeSHA256();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("TSIM-ACCOUNT",tsimAccount);
        headers.add("TSIM-NONCE",computedHeader.get("TSIM_NONCE"));
        headers.add("TSIM-TIMESTAMP",computedHeader.get("TSIM_TIMESTAMP"));
        headers.add("TSIM-SIGN",computedHeader.get("TSIM_SIGN"));
        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("number",1);
        jsonObject.put("channel_dataplan_id",esimProductId);
        jsonObject.put("remark","userId : " + userId);

        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);
        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE_END.getExplain() + response.getBody(), orderId);

        return response.getBody();
    }


    public static HashMap getEsimPackages() throws Exception {

        String url = baseUrl + "/tsim/v1/esimDataplanList";
        Map<String,String> computedHeader = ComputeSHA256();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("TSIM-ACCOUNT",tsimAccount);
        headers.add("TSIM-NONCE",computedHeader.get("TSIM_NONCE"));
        headers.add("TSIM-TIMESTAMP",computedHeader.get("TSIM_TIMESTAMP"));
        headers.add("TSIM-SIGN",computedHeader.get("TSIM_SIGN"));


        JsonObject jsonObject = new JsonObject();

        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);

        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.GET, entity, HashMap.class);


        return response.getBody();

    }






    public static Map<String,String> ComputeSHA256() throws Exception{


        Date nowdate = new Date();
        long timestampnumber = nowdate.getTime()/1000;
        String TSIM_NONCE = UUID.randomUUID().toString().substring(0,10);
        String TSIM_TIMESTAMP = timestampnumber+"";
        String sign = tsimAccount + TSIM_NONCE + TSIM_TIMESTAMP;

        SecretKeySpec keySpec = new SecretKeySpec(tsimSecret.getBytes("UTF-8"), "HMACSHA256");
        Mac mac = Mac.getInstance(keySpec.getAlgorithm());
        mac.init(keySpec);
        Map<String,String> result = new HashMap<>();
        result.put("TSIM_NONCE",TSIM_NONCE);
        result.put("TSIM_TIMESTAMP",TSIM_TIMESTAMP);
        result.put("TSIM_SIGN",Hex.encodeHexString(mac.doFinal(sign.getBytes("UTF-8"))));

        return result;
    }


}
