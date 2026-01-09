package com.naver.naverspabackend.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.service.esimapiingsteplogs.EsimApiIngStepLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;


@Component
public class OriginAirAloUtil {

    public static OrderMapper orderMapper;


    @Autowired
    public void AirAloUtil(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }
    static String clientId;
    @Value("${api.airalo.clientId}")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    static String clientSecret;
    @Value("${api.airalo.clientSecret}")
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }


    static String baseUrl;
    @Value("${api.airalo.baseUrl}")
    public void setVaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static EsimApiIngStepLogsService esimApiIngStepLogsService;
    @Autowired
    public void setEsimApiIngStepLogsService(EsimApiIngStepLogsService esimApiIngStepLogsService) {
        this.esimApiIngStepLogsService = esimApiIngStepLogsService;
    }

    static String accessToken =  null;



    public static List<HashMap<String, Object>> contextLoads1() throws NoSuchAlgorithmException {

        accessToken = makeOauthAccessToken(null);
        List<HashMap<String, Object>> result = new ArrayList<>();
        System.setProperty("https.protocols", "TLSv1.2");
        HashMap esimPackages = getEsimPackages();
        List<HashMap<String, Object>> dataList = (List<HashMap<String, Object>>) esimPackages.get("data");

        for(Map<String,Object> data : dataList){
            List<HashMap<String, Object>> operatorsList = (List<HashMap<String, Object>>) data.get("operators");
            for(Map<String,Object> operator : operatorsList){
                List<HashMap<String, Object>> packageList = (List<HashMap<String, Object>>) operator.get("packages");
                for(HashMap<String, Object> packages : packageList){
                    packages.put("title", data.get("title").toString() + " " + packages.get("title").toString() );
                    result.add(packages);
                }



            }
        }

        return result;
    }

    public static HashMap contextLoads2(String userId, String esimProductId, Long orderId) throws NoSuchAlgorithmException {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);

        accessToken = makeOauthAccessToken(orderId);

        System.setProperty("https.protocols","TLSv1.2");

        //esim package 구매
        HashMap esimResult =  getEsimPackagePurchase(esimProductId,userId, orderId);


        return esimResult;
    }


    public static HashMap contextLoads3(String esimIccid, Long orderId) throws NoSuchAlgorithmException {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);

        accessToken = makeOauthAccessToken(orderId);

        System.setProperty("https.protocols","TLSv1.2");

        return getEsimPurchaseStatus(esimIccid,orderId);
    }


    public static void contextLoads4(String iccid, Model model) throws Exception {
        accessToken = makeOauthAccessToken(null);

        System.setProperty("https.protocols","TLSv1.2");
        HashMap esimMap  =  getEsimStatus(iccid);
        Map<String,Object> result = (Map<String, Object>) esimMap.get("meta");
        esimMap = (HashMap) esimMap.get("data");

        Map<String,Object> item = new HashMap<>();
        if(esimMap!=null && result.get("message").toString().equals("success")){ //데일리 충전 확인 기능 없음 아래로 해야함.



            model.addAttribute("totalUsage",Math.round(Float.parseFloat(esimMap.get("total").toString()) * 100) / 100.0);
            model.addAttribute("usage",(Math.round(Float.parseFloat(esimMap.get("total").toString()) -Float.parseFloat(esimMap.get("remaining").toString()))* 100) / 100.0 );

            model.addAttribute("totalVoiceUsage",esimMap.get("total_voice") );
            model.addAttribute("voiceUsage",Integer.parseInt(esimMap.get("total_voice").toString())- Integer.parseInt(esimMap.get("remaining_voice").toString()) );

            model.addAttribute("totalTextUsage",esimMap.get("total_text"));
            model.addAttribute("textUsage",Integer.parseInt(esimMap.get("total_text").toString())- Integer.parseInt(esimMap.get("remaining_text").toString())) ;

            if(esimMap.get("expired_at")!=null){

                SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // String 타입을 Date 타입으로 변환
                Calendar cal = Calendar.getInstance();
                Date expiredDate = newDtFormat.parse(esimMap.get("expired_at").toString());
                cal.setTime(expiredDate);
                cal.add(Calendar.HOUR, 9);//UTC+0 시간이라 +9시간해줘야 한국시간
                String expiredDateString = newDtFormat.format(cal.getTime());
                model.addAttribute("expiredAt",expiredDateString);

                Date expiredAt =  newDtFormat.parse(expiredDateString);
                Date now = new Date();
                model.addAttribute("end",now.after(expiredAt));
            }else{

                model.addAttribute("expiredAt","");
                model.addAttribute("end",false);
            }

            if(esimMap.get("status").equals("FINISHED"))
                model.addAttribute("end",true);
        }
        model.addAttribute("iccid",iccid);
    }


    private static HashMap getEsimPurchaseStatus(String esimIccid, Long orderId) {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS.getExplain(), orderId);

        String url = baseUrl + "/v2/sims/"+esimIccid;
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


        // create param
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("include","order,order.status,order.user,share");

        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);


        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);

        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.GET, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS_END.getExplain() + response.getBody(), orderId);


        return response.getBody();
    }


    private static HashMap getEsimStatus(String esimIccid) {
        String url = baseUrl + "/v2/sims/"+esimIccid + "/usage";
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


        // create param
        JsonObject jsonObject = new JsonObject();

        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);



        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.GET, entity, HashMap.class);


        return response.getBody();
    }


    private static HashMap getEsimPackagePurchase(String esimProductId, String userId, Long orderId) {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE.getExplain(), orderId);

        String url = baseUrl + "/v2/orders";
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


        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("quantity",1);
        jsonObject.put("package_id",esimProductId);
        jsonObject.put("description",userId);
        jsonObject.put("brand_settings_name",null);


        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);
        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE_END.getExplain() + response.getBody(), orderId);


        return response.getBody();
    }


    public static HashMap getEsimPackages(){

        String url = baseUrl + "/v2/packages";
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


        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("include","topup");
        jsonObject.addProperty("limit",1000);
        jsonObject.addProperty("page",1);
        HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);

        ResponseEntity<HashMap> response = restTemplate.exchange(url+"?include=topup&limit=1000&page=1", HttpMethod.GET, entity, HashMap.class);


        return response.getBody();

    }

    public static String makeOauthAccessToken(Long orderId) throws NoSuchAlgorithmException {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.ACCESS_TOKEN.getExplain(), orderId);


        String airAloToken = orderMapper.selectOrderAiraloToken();
        Map<String,String> map = new HashMap<>();
        if(airAloToken==null){
            HashMap<String,Object> result = getAccessToken(orderId);
            Map<String,String> token = (Map<String, String>) result.get("data");
            airAloToken = token.get("token_type") + " " + token.get("access_token");
            map.put("token",airAloToken);
            orderMapper.inserttOrderAiraloToken(airAloToken);
        }

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.ACCESS_TOKEN_END.getExplain() + airAloToken, orderId);


        return airAloToken;

    }

    public static String EncodeBase64(String plainText)
    {
        String encodedString = Base64.getEncoder().encodeToString(plainText.getBytes());
        return encodedString;
    }


    public static HashMap getAccessToken(Long orderId) {

        String url = baseUrl + "/v2/token";
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(60000 * 1000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(60000); // 커넥션 최대 시간
        factory.setReadTimeout(60000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", "client_credentials");


        esimApiIngStepLogsService.insertRest(headers,map, orderId);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        ResponseEntity<HashMap> response =  restTemplate.postForEntity( url, request , HashMap.class );
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
