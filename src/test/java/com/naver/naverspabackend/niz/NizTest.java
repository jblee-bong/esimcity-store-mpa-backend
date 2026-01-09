package com.naver.naverspabackend.niz;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class NizTest {


    static String NIZ_ID = "X1XK8II40NENI4PN0MM19S";
    static String NIZ_PASS= "4NZG5JPOSY2FKKK5UP66D1";
    static String NIZ_PARTNER_CODE = "000200000";


    static String baseUrl = "https://simapi.simgonow.com";
    static String currentBasicOauthAccessToken =  null;
    static String accessToken =  null;



    @Test
    void formatStringToEmail() throws Exception{
        // 검색할 문자열
        String shippingMemo = "배송 전에 미리 연락 바랍니다. 111222qw@@naver.com";


        // 패턴 지정 이메일
        String patternEmail = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
        // 패턴 객체 생성
        Pattern e = Pattern.compile(patternEmail);
        // 문자열에서 패턴 검색
        Matcher m2 = e.matcher(shippingMemo);
        // 검색된 패턴 추출
        String email= null;
        while (m2.find()){
            email=m2.group();
        }
        if(email == null){
            throw new Exception("이메일이 유효하지않음");
        }

    }

    @Test
    void contextLoads1() throws NoSuchAlgorithmException {
        currentBasicOauthAccessToken = makeOauthAccessToken();

        System.setProperty("https.protocols", "TLSv1.2");

        //accessToken 얻기
        HashMap accessTokenMap = getAccessToken();


        System.out.println(accessTokenMap);
        accessToken =  "Bearer " +  accessTokenMap.get("value").toString();

        HashMap esimPackages = getEsimPackages();


        List<HashMap<String, Object>> esimPagekageList = (List<HashMap<String, Object>>) esimPackages.get("products");

        System.out.println(esimPagekageList);
        /*
        //이심구매 시작
        HashMap esimResult =  getEsimPackagePurchase("759","test@test.com","3");


        List<Map<String,Object>> resultList = (List<Map<String, Object>>) esimResult.get("activations");
        Map<String,Object> result = resultList.get(0);

        if(result.get("result").toString().equals("0")){
            System.out.println("성공");
            Map<String,Object>activationReqeust = (Map<String, Object>) result.get("activationReqeust");
            System.out.println(activationReqeust.get("id"));
        }

        //이심 구매 종료

         */

        //이심 조회
        getEsimPurchaseStatus("177571");

    }

    private static HashMap getEsimPurchaseStatus(String activationRequestId) {
        String url = baseUrl + "/api/resim/activation/"+activationRequestId;
        RestTemplate restTemplate = new RestTemplate();

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization",accessToken);
        headers.add("user-agent", "Application");


        // create param
        JsonObject jsonObject = new JsonObject();

        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);



        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.GET, entity, HashMap.class);

        System.out.println(response.getBody());
        System.out.println(response.getStatusCodeValue());

        return response.getBody();
    }


    private static HashMap getEsimPackagePurchase(String esimProductId, String userId, String esimProductDays) {

        String url = baseUrl + "/api/resim/activate";
        RestTemplate restTemplate = new RestTemplate();

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
        jsonObject.put("agentID",NIZ_PARTNER_CODE);
        jsonObject.put("activations",items);


        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

        System.out.println(response.getBody());
        System.out.println(response.getStatusCodeValue());

        return response.getBody();
    }


    public static HashMap getEsimPackages(){

        String url = baseUrl + "/api/resim/product";
        RestTemplate restTemplate = new RestTemplate();

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization",accessToken);
        headers.add("user-agent", "Application");


        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("agentID",NIZ_PARTNER_CODE);

        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.GET, entity, HashMap.class);

        System.out.println(response.getBody());
        System.out.println(response.getStatusCodeValue());

        return response.getBody();

    }

    public static HashMap getAccessToken() throws NoSuchAlgorithmException {

        String url = baseUrl + "/api/token/basic";
        RestTemplate restTemplate = new RestTemplate();

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.add("Authorization",currentBasicOauthAccessToken);
        headers.add("user-agent", "Application");
        // create param
        JsonObject jsonObject = new JsonObject();

        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);

        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

        System.out.println(response.getBody());
        System.out.println(response.getStatusCodeValue());

        return response.getBody();

    }

    public static String makeOauthAccessToken() throws NoSuchAlgorithmException {

        String base64Token = EncodeBase64(NIZ_ID+":"+NIZ_PASS);
        String basicToken = "Basic " +  base64Token;
        System.out.println(basicToken);
        return basicToken;

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


    public static String EncodeBase64(String plainText)
    {
        String encodedString = Base64.getEncoder().encodeToString(plainText.getBytes());
        return encodedString;
    }
}