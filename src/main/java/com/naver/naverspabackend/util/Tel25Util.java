package com.naver.naverspabackend.util;

import com.google.gson.JsonObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class Tel25Util {


    //realInfo
    static String TEL25_USER_ID = "";
    static String TEL25_SECURITY_TOKEN = "s1EC95TpEOvCkMYq";
    static String TEL25_OAUTH2_CLIENT_ID = "RfazyjxHhN4LIttI2KxRWKMnbUZ6OJcb";
    static String TEL25_OAUTH2_CLIENT_SECURITY = "PkCuCULx16zkpqMfjwxByonSqUGEK0c4";
    static String baseUrl = "https://prov-api.tel25.com";

    static String currentBasicOauth2AccessToken = "";
    static String currentOauth2AccessToken = "";



    public static List<HashMap<String, String>> contextLoads1() throws NoSuchAlgorithmException {
        currentBasicOauth2AccessToken = makeOauth2AccessToken();

        System.setProperty("https.protocols", "TLSv1.2");

        //accessToken 얻기
        HashMap accessToken = getAccessToken();

        currentOauth2AccessToken = accessToken.get("token_type").toString() + " " + accessToken.get("access_token").toString();

        //esim package list
        HashMap esimPackages = getEsimPackages();
        List<HashMap<String, String>> esimPagekageList = (List<HashMap<String, String>>) esimPackages.get("package_list");

        return esimPagekageList;
    }

    public static HashMap contextLoads2(String userId, String esimProductId) throws NoSuchAlgorithmException {
        currentBasicOauth2AccessToken = makeOauth2AccessToken();
        System.setProperty("https.protocols","TLSv1.2");
        //accessToken 얻기
        HashMap accessToken = getAccessToken();
        currentOauth2AccessToken  = accessToken.get("token_type").toString() + " " + accessToken.get("access_token").toString();

        //esim package 구매
        HashMap esimResult =  getEsimPackagePurchase(esimProductId,userId);

        //accessToken 삭제
        setAccessTokenRevoke(accessToken.get("access_token").toString());

        return esimResult;
    }



    public static HashMap getEsimPackagePurchase(String packageId, String userId /*구매자 아이디*/) throws NoSuchAlgorithmException {

        String url = baseUrl + "/api/v1/esims/packages/"+ packageId + "/purchase/sync";
        RestTemplate restTemplate = new RestTemplate();

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.add("X-Tel25-Access-Token",makeXTel25AccessToken());
        headers.add("Authorization",currentOauth2AccessToken);
        headers.add("user-agent", "Application");

        // create param
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("customer_order_id",userId);

        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);

        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);


        return response.getBody();

    }

    public static HashMap getEsimPackages() throws NoSuchAlgorithmException {

        String url = baseUrl + "/api/v1/esims/packages";
        RestTemplate restTemplate = new RestTemplate();

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.add("X-Tel25-Access-Token",makeXTel25AccessToken());
        headers.add("Authorization",currentOauth2AccessToken);
        headers.add("user-agent", "Application");


        HttpEntity entity = new HttpEntity<>(headers); // http entity에 header 담아줌

        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.GET, entity, HashMap.class);

        return response.getBody();

    }

    public static String setAccessTokenRevoke(String access_token) throws NoSuchAlgorithmException {

        String url = baseUrl + "/api/v1/auth/token/revoke";
        RestTemplate restTemplate = new RestTemplate();

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.add("X-Tel25-Access-Token",makeXTel25AccessToken());
        headers.add("Authorization",currentOauth2AccessToken);
        headers.add("user-agent", "Application");

        // create param
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("access_token",access_token);

        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();

    }

    public static HashMap getAccessToken() throws NoSuchAlgorithmException {

        String url = baseUrl + "/api/v1/auth/token";
        RestTemplate restTemplate = new RestTemplate();

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.add("X-Tel25-Access-Token",makeXTel25AccessToken());

        headers.add("Authorization",currentBasicOauth2AccessToken);
        headers.add("user-agent", "Application");
        // create param
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("grant_type","client_credentials");

        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);

        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

        return response.getBody();

    }

    public static String makeOauth2AccessToken() throws NoSuchAlgorithmException {

        String base64Token = EncodeBase64(TEL25_OAUTH2_CLIENT_ID+":"+TEL25_OAUTH2_CLIENT_SECURITY);
        String basicToken = "Basic " +  base64Token;
        return basicToken;

    }


    public static String makeXTel25AccessToken() throws NoSuchAlgorithmException {
        Date date = new Date();
        long timestamp = date.getTime();
        String encodedToken = ComputeSHA256(timestamp + TEL25_SECURITY_TOKEN);
        String mergedToken = TEL25_USER_ID + "/" + timestamp + "/" + encodedToken;
        String accessToken = EncodeBase64(mergedToken);
        String xTel25AccessToken = "Bearer " + accessToken;
        return xTel25AccessToken;
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
