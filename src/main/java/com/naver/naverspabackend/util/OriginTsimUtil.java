package com.naver.naverspabackend.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.service.esimapiingsteplogs.EsimApiIngStepLogsService;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class OriginTsimUtil {


    public static String tsimAccount;
    @Value("${api.tsim.account}")
    public void setTsimAccount(String tsimAccount) {
        this.tsimAccount = tsimAccount;
    }

    static String tsimSecret;
    @Value("${api.tsim.tsimSecret}")
    public void setTsimSecret(String tsimSecret) {
        this.tsimSecret = tsimSecret;
    }

    static String baseUrl;
    @Value("${api.tsim.baseUrl}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static EsimApiIngStepLogsService esimApiIngStepLogsService;
    @Autowired
    public void setEsimApiIngStepLogsService(EsimApiIngStepLogsService esimApiIngStepLogsService) {
        this.esimApiIngStepLogsService = esimApiIngStepLogsService;
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
            SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // String 타입을 Date 타입으로 변환
            Calendar cal = Calendar.getInstance();
            if(esimMap.get("msg").toString().equals("Success")){
                result = (Map<String, Object>) esimMap.get("result");

                boolean isDaily = (boolean) result.get("is_daily");
                double m = 0;




                if(isDaily){
                    DecimalFormat dec = new DecimalFormat("0.00");
                    List<Map<String, Object>> dataUsageDaily = (List<Map<String, Object>>) result.get("data_usage_daily");
                    Double size = (double) 0;
                    for(int i=0;i<dataUsageDaily.size();i++){
                        if(i==0){
                            size += Double.parseDouble(dataUsageDaily.get(i).get("total_usage_kb").toString());
                        }
                    }
                    m = size/1024;

                    if(result.get("daily_reset_time").toString()!=null && !result.get("daily_reset_time").toString().equals("")){

                        Date restDate = newDtFormat.parse(result.get("daily_reset_time").toString());
                        cal.setTime(restDate);
                        cal.add(Calendar.HOUR, 1);//중국시간이라 +1시간해줘야 한국시간
                        String resetDateString = newDtFormat.format(cal.getTime());
                        model.addAttribute("resetAt",resetDateString);//종료일 - 매일일경우 리셋일
                    }else{
                        model.addAttribute("resetAt","");//종료일 - 매일일경우 리셋일
                    }

                }else{
                    model.addAttribute("resetAt","");//종료일 - 매일일경우 리셋일
                    m = Double.parseDouble(result.get("data_usage").toString());


                }
                Date expiredDate = newDtFormat.parse(result.get("expire_time").toString());
                cal.setTime(expiredDate);
                cal.add(Calendar.HOUR, 1); //중국시간이라 +1시간해줘야 한국시간
                String expiredDateString = newDtFormat.format(cal.getTime());
                model.addAttribute("expiredAt",expiredDateString);//종료일 - 매일일경우 리셋일

                Date expiredAt =  newDtFormat.parse(expiredDateString);
                Date now = new Date();
                model.addAttribute("end",now.after(expiredAt));

                Map<String,Object> item = new HashMap<>();
                model.addAttribute("esimDescription",result.get("channel_dataplan_name"));
                model.addAttribute("iccid",iccid);
                model.addAttribute("isDaily",isDaily);//매일데이터 리셋 유부
                /*
                Date installDate = newDtFormat.parse(result.get("active_time").toString());
                cal.setTime(installDate);
                cal.add(Calendar.HOUR, 1);
                String installDateString = newDtFormat.format(cal.getTime());
                model.addAttribute("installAt",installDateString);//설치일*/
                model.addAttribute("usage",Math.round(m * 100) / 100.0);
                //item.put("remaining",Integer.parseInt(esimMap.get("totalUsage").toString()) - Integer.parseInt(item.get("usage").toString()));
            }
        }

    }


    private static HashMap getEsimStatus(String activationRequestId) throws Exception{
        String url = baseUrl + "/tsim/v1/getOrderInfoByCustomOrderNo";
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
        jsonObject.put("custom_order_no",activationRequestId);

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);


        return response.getBody();
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
