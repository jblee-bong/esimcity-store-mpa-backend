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
public class OriginTugeUtil {

    public static String accountId;

    @Value("${api.tuge.accountId}")
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public static String signKey;
    @Value("${api.tuge.signKey}")
    public void setSignKey(String signKey) {
        this.signKey = signKey;
    }

    public static String secretkey;
    @Value("${api.tuge.secretkey}")
    public void setSecretkey(String secretkey) {
        this.secretkey = secretkey;
    }

    public static String vector;
    @Value("${api.tuge.vector}")
    public void setVector(String vector) {
        this.vector = vector;
    }



    public static String base2Url;
    @Value("${api.tuge.base2Url}")
    public void setBase2Url(String base2Url) {
        this.base2Url = base2Url;
    }

    public static String version;
    @Value("${api.tuge.version}")
    public void setVersion(String version) {
        this.version = version;
    }

    public static EsimApiIngStepLogsService esimApiIngStepLogsService;
    @Autowired
    public void setEsimApiIngStepLogsService(EsimApiIngStepLogsService esimApiIngStepLogsService) {
        this.esimApiIngStepLogsService = esimApiIngStepLogsService;
    }


    public static TugeRedisRepository tugeRedisRepository;
    @Autowired
    public void setTugeRedisRepository(TugeRedisRepository tugeRedisRepository) {
        this.tugeRedisRepository = tugeRedisRepository;
    }



    public static List<HashMap<String, Object>> contextLoads1() throws Exception {

        System.setProperty("https.protocols", "TLSv1.2");

        List<HashMap<String, Object>> result = new ArrayList<>();
        boolean existFlag = true;
        int page = 1;
        while (existFlag){

            HashMap esimPackages = getEsimPackages(page);
            Gson gson = new Gson();
            if(esimPackages.get("code").toString().equals("0000")){
                HashMap<String, Object> data = (HashMap<String, Object>) esimPackages.get("data");

                List<HashMap<String, Object>> item = (List<HashMap<String, Object>>) data.get("list");
                if(item.size()==0){
                    existFlag = false;
                }else{
                    result.addAll(item);
                }
            }else{
                return null;
            }
            page++;
        }
        return result;
    }


    public static HashMap<String, Object>  contextLoads6(String cardType) throws Exception {

        System.setProperty("https.protocols", "TLSv1.2");

        HashMap esimPackages = getEsimCardType(cardType);
        Gson gson = new Gson();
        if(esimPackages.get("code").toString().equals("0000")){
            return  (HashMap<String, Object>) esimPackages.get("data");

        }else{
            return null;
        }
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
            String url = base2Url +  serviceName;
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
            factory.setConnectTimeout(5000); // 커넥션 최대 시간
            factory.setReadTimeout(45000); // 읽기 최대 시간

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





    public static HashMap getEsimPackages(int page) throws Exception {

        String serviceName = "eSIMApi/v2/products/list";

        String url = base2Url  + serviceName;

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = getToken(tugeRedisRepository);


        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("pageSize",100);
        jsonObject.put("pageNum",page);
        jsonObject.put("lang","en");

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        return response.getBody();

    }


    public static HashMap getEsimCardType(String cardType) throws Exception {

        String serviceName = "eSIMApi/v2/card";

        String url = base2Url  + serviceName;

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = getToken(tugeRedisRepository);


        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("cardType",cardType);

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        return response.getBody();

    }











}
