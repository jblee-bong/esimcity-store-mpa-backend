package com.naver.naverspabackend.util;

import com.google.gson.Gson;
import com.naver.naverspabackend.security.TugeRedisRepository;
import com.naver.naverspabackend.security.token.TugeRedisToken;
import com.naver.naverspabackend.service.esimapiingsteplogs.EsimApiIngStepLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class OriginEsimAccessUtil {

    public static String clientId;
    @Value("${api.esimaccess.clientId}")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public static String clientSecret;
    @Value("${api.esimaccess.clientSecret}")
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public static String baseUrl;
    @Value("${api.esimaccess.baseUrl}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static EsimApiIngStepLogsService esimApiIngStepLogsService;
    @Autowired
    public void setEsimApiIngStepLogsService(EsimApiIngStepLogsService esimApiIngStepLogsService) {
        this.esimApiIngStepLogsService = esimApiIngStepLogsService;
    }



    public static List<Map<String, Object>> contextLoads1() throws Exception {

        System.setProperty("https.protocols", "TLSv1.2");

        List<HashMap<String, Object>> result = new ArrayList<>();
        return getEsimPackages();
    }






    public static List<Map<String, Object>> getEsimPackages() throws Exception {


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


        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        if((Boolean) response.getBody().get("success")){
            Map<String,Object> obj = (Map<String, Object>) response.getBody().get("obj");
            return  (List<Map<String, Object>>) obj.get("packageList");
        }else{
            throw new Exception();
        }

    }











}
