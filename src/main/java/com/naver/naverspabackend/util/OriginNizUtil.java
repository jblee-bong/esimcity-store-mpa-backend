package com.naver.naverspabackend.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.service.esimapiingsteplogs.EsimApiIngStepLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
public class OriginNizUtil {

    public static String nizId;
    @Value("${api.niz.nizId}")
    public void setNizId(String nizId) {
        this.nizId = nizId;
    }

    public static String nizPass;
    @Value("${api.niz.nizPass}")
    public void setNizPass(String nizPass) {
        this.nizPass = nizPass;
    }

    public static String nizPartnerCode;
    @Value("${api.niz.nizPartnerCode}")
    public void setNizPartnerCode(String nizPartnerCode) {
        this.nizPartnerCode = nizPartnerCode;
    }

    public static String baseUrl;
    @Value("${api.niz.baseUrl}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }


    public static EsimApiIngStepLogsService esimApiIngStepLogsService;
    @Autowired
    public void setEsimApiIngStepLogsService(EsimApiIngStepLogsService esimApiIngStepLogsService) {
        this.esimApiIngStepLogsService = esimApiIngStepLogsService;
    }



    static String currentBasicOauthAccessToken =  null;
    static String accessToken =  null;



    public static List<HashMap<String, Object>> contextLoads1() throws NoSuchAlgorithmException {

        currentBasicOauthAccessToken = makeOauthAccessToken();

        System.setProperty("https.protocols", "TLSv1.2");


        //accessToken 얻기
        HashMap accessTokenMap = getAccessToken(null);


        accessToken =  "Bearer " +  accessTokenMap.get("value").toString();

        HashMap esimPackages = getEsimPackages();


        List<HashMap<String, Object>> esimPagekageList = (List<HashMap<String, Object>>) esimPackages.get("products");

        return esimPagekageList;
    }

    public static HashMap contextLoads2(String userId, String esimProductId, String esimProductDays, Long orderId) throws NoSuchAlgorithmException {

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);
        currentBasicOauthAccessToken = makeOauthAccessToken();

        System.setProperty("https.protocols","TLSv1.2");
        //accessToken 얻기
        HashMap accessTokenMap = getAccessToken(orderId);
        accessToken =  "Bearer " +  accessTokenMap.get("value").toString();

        //esim package 구매
        HashMap esimResult =  getEsimPackagePurchase(esimProductId,userId,esimProductDays,orderId);


        return esimResult;
    }


    public static HashMap contextLoads3(String activationRequestId, Long orderId) throws NoSuchAlgorithmException {

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);

        currentBasicOauthAccessToken = makeOauthAccessToken();

        System.setProperty("https.protocols","TLSv1.2");
        //accessToken 얻기
        HashMap accessTokenMap = getAccessToken(orderId);
        accessToken =  "Bearer " +  accessTokenMap.get("value").toString();

        return getEsimPurchaseStatus(activationRequestId,orderId);
    }


    private static HashMap getEsimPurchaseStatus(String activationRequestId, Long orderId) {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS.getExplain(), orderId);
        String url = baseUrl + "/api/resim/activation/"+activationRequestId;
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(60000 * 1000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(60000); // 커넥션 최대 시간
        factory.setReadTimeout(60000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization",accessToken);
        headers.add("user-agent", "Application");


        // create param
        JsonObject jsonObject = new JsonObject();

        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);


        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);

        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.GET, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS_END.getExplain() + response.getBody(), orderId);
        return response.getBody();
    }

    private static HashMap getEsimPackagePurchase(String esimProductId, String userId, String esimProductDays, Long orderId) {

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE.getExplain(), orderId);
        String url = baseUrl + "/api/resim/activate";
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(60000 * 1000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(60000); // 커넥션 최대 시간
        factory.setReadTimeout(60000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization",accessToken);
        headers.add("user-agent", "Application");


        List<Map<String,String>> items = new ArrayList<>();
        Map<String,String> item = new HashMap<>();

        item.put("Fullname",userId);
        item.put("productID",esimProductId);
        if(esimProductDays!=null && !esimProductDays.trim().equals(""))
            item.put("days",esimProductDays);

        items.add(item);

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("agentID",nizPartnerCode);
        jsonObject.put("activations",items);

        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE_END.getExplain() + response.getBody(), orderId);


        return response.getBody();
    }



    public static HashMap getEsimPackages(){

        String url = baseUrl + "/api/resim/product";
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(60000 * 1000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(60000); // 커넥션 최대 시간
        factory.setReadTimeout(60000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization",accessToken);
        headers.add("user-agent", "Application");


        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("agentID",nizPartnerCode);

        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);

        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.GET, entity, HashMap.class);


        return response.getBody();

    }

    public static String makeOauthAccessToken() throws NoSuchAlgorithmException {

        String base64Token = EncodeBase64(nizId+":"+nizPass);
        String basicToken = "Basic " +  base64Token;
        return basicToken;

    }

    public static String EncodeBase64(String plainText)
    {
        String encodedString = Base64.getEncoder().encodeToString(plainText.getBytes());
        return encodedString;
    }


    public static HashMap getAccessToken(Long orderId) {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.ACCESS_TOKEN.getExplain(), orderId);
        String url = baseUrl + "/api/token/basic";
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(60000 * 1000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(60000); // 커넥션 최대 시간
        factory.setReadTimeout(60000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.add("Authorization",currentBasicOauthAccessToken);
        headers.add("user-agent", "Application");
        // create param
        JsonObject jsonObject = new JsonObject();

        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);

        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.ACCESS_TOKEN_END.getExplain() + response.getBody(), orderId);


        return response.getBody();

    }


    public static String ComputeSHA256(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(text.getBytes());

        return bytesToHex(md.digest());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }




}
