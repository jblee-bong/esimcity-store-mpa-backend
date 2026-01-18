package com.naver.naverspabackend.util;

import com.google.gson.Gson;
import com.naver.naverspabackend.dto.ApiPurchaseItemDto;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.service.esimapiingsteplogs.EsimApiIngStepLogsService;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

public class WorldMoveUtil {


    public static String merchantId;

    public static String deptId;

    public static String token;

    public static String baseUrl;


    public static EsimApiIngStepLogsService esimApiIngStepLogsService;

    public WorldMoveUtil(String merchantId, String deptId, String token, String baseUrl, EsimApiIngStepLogsService esimApiIngStepLogsService){
        this.merchantId = merchantId;
        this.deptId = deptId;
        this.token = token;
        this.baseUrl = baseUrl;
        this.esimApiIngStepLogsService = esimApiIngStepLogsService;

    }


    public static List<HashMap<String, Object>> contextLoads1() throws Exception {

        System.setProperty("https.protocols", "TLSv1.2");


        HashMap esimPackages = getEsimPackages();


        List<HashMap<String, Object>> esimPagekageList = (List<HashMap<String, Object>>) esimPackages.get("prodList");

        return esimPagekageList;
    }

    public static HashMap contextLoads2(String userId, String esimProductId, Integer retryCount, Long orderId) throws Exception {

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);

        System.setProperty("https.protocols","TLSv1.2");
        HashMap esimResult = null;
        try{
            esimResult =  getEsimPackagePurchase(esimProductId,userId, orderId);
        }catch (Exception e){
            e.printStackTrace();
            try{Thread.sleep(5000);}catch (Exception e2){
                esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.ERROR.getExplain() + "\n재시도 횟수 :" + retryCount + "\n"+ e.getMessage(), orderId);
            };
            if(retryCount<10)
                esimResult = contextLoads2(userId,esimProductId, retryCount+1, orderId);
            else
                throw new RuntimeException(e.getMessage());
        }


        return esimResult;
    }


    public static HashMap contextLoads3(String activationRequestId, Long orderId) throws Exception {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);

        System.setProperty("https.protocols","TLSv1.2");
        return getEsimPurchaseStatus(activationRequestId, orderId);
    }


    public static void  contextLoads4(String rcode, String iccid, String productId, Model model) throws Exception {
        System.setProperty("https.protocols","TLSv1.2");

        String dayCheckItem = productId.split("-")[productId.split("-").length-2];
        String esimType = "";
        double sizeMb = 0;
        if(dayCheckItem.equals("MAX") || dayCheckItem.equals("TI")){
            esimType = "unlimited";
        }
        else if(dayCheckItem.startsWith("T")){
            esimType = "total";
            sizeMb =  gbToMb(Double.parseDouble(dayCheckItem.replace("T","")));
        }
        else if(!dayCheckItem.startsWith("T")){
            esimType = "daily";
            if(dayCheckItem.indexOf("MB")>-1){
                sizeMb =  Double.parseDouble(dayCheckItem.replace("MB",""));
            }
            if(dayCheckItem.indexOf("GB")>-1){
                sizeMb =  gbToMb(Double.parseDouble(dayCheckItem.replace("GB","")));
            }
        }



        HashMap esimMap =  getEsimStatus(rcode);
        if(esimMap.get("code").toString().equals("0")){

            model.addAttribute("totalUsage",sizeMb);//전체일경우: 전체데이터, 매일일 경우: 매일 사이클 데이터
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);
            String useEndDate = sdf.format(new Date(Long.parseLong(esimMap.get("useEDate").toString())));
            String useStartDate = sdf.format(new Date(Long.parseLong(esimMap.get("useSDate").toString())));
            Date expiredAt =  sdf.parse(useEndDate);
            Date now = new Date();

            model.addAttribute("end",now.after(expiredAt));

            if(esimMap.get("esimStatus")!=null && Integer.parseInt(esimMap.get("esimStatus").toString()) != 0){

                model.addAttribute("useStartDate",useStartDate);//활성화시작일
                model.addAttribute("useEndDate",useEndDate);//사용종료일
            }
            if(esimType.equals("unlimited")){//무재한
                model.addAttribute("totalUsage","unlimited");
                String bytes = esimMap.get("totalUsage").toString();
                long size = Long.parseLong(bytes);
                Double retFormat = convertBytesToMB(size);
                model.addAttribute("usage",retFormat);
                model.addAttribute("remaining","unlimited"); //데이터 남은량 무재한

            }
            else if(esimType.equals("total")){
                String bytes = esimMap.get("totalUsage").toString();
                long size = Long.parseLong(bytes);
                Double retFormat = convertBytesToMB(size);
                model.addAttribute("usage",retFormat);
                model.addAttribute("remaining",sizeMb - retFormat); //데이터 남은량
            }
            else if(esimType.equals("daily")){
                long size = 0L;
                List<HashMap<String,Object>>  itemList = (List<HashMap<String, Object>>) esimMap.get("itemList");
                if(itemList.size()>0){
                    size = Long.parseLong(itemList.get(0).get("usage").toString());
                }
                Double retFormat = convertBytesToMB(size);
                model.addAttribute("usage",retFormat);
                model.addAttribute("remaining",sizeMb - retFormat); //매일 데이터 남은량
            }
        }
        model.addAttribute("esimType",esimType.equals("daily"));
        model.addAttribute("iccid",iccid);


        model.addAttribute("html","esimdata2");
    }

    private static HashMap getEsimStatus(String rcode) throws Exception{

        String url = baseUrl + "/Api/UseageDetail/queryUsage";
        Map<String,String> computedCommonParam = ComputeSHA1(merchantId + rcode +token);

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate = makeRestTemplate();

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("merchantId",merchantId);
        jsonObject.put("rcode",rcode);
        jsonObject.put("encStr",computedCommonParam.get("encStr"));



        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);


        return response.getBody();
    }

    private static HashMap<String,String> getEsimPurchaseStatus(String activationRequestId, Long orderId) throws Exception{
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS.getExplain(), orderId);

        String url = baseUrl + "/Api/SOrder/querybuyesim";
        Map<String,String> computedCommonParam = ComputeSHA1(merchantId + activationRequestId+token);

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate = makeRestTemplate();

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("merchantId",merchantId);
        jsonObject.put("orderId",activationRequestId);
        jsonObject.put("encStr",computedCommonParam.get("encStr"));


        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);
        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS_END.getExplain()+"1" + response.getBody(), orderId);

        HashMap<String,String> result = null;
        if(response.getBody().get("code").toString().equals("0")){
            result = getEsimPurchaseStatus2(response.getBody(), 1, orderId);
            try{Thread.sleep(1000);}catch (Exception e2){};

        }
        return result;
    }

    private static HashMap<String,String> getEsimPurchaseStatus2(HashMap item, int qrcodeType,Long orderId) throws Exception {
        String url = baseUrl + "/Api/OrderRedemption/redemption";

        List<Map<String,String>> itemList = (List<Map<String, String>>) item.get("itemList");
        String rcode = itemList.get(0).get("redemptionCode");
        String iccid = itemList.get(0).get("iccid");
        Map<String,String> computedCommonParam = ComputeSHA1(merchantId + rcode + qrcodeType + token);

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate = makeRestTemplate();

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("merchantId",merchantId);
        jsonObject.put("rcode",rcode);
        jsonObject.put("qrcodeType",qrcodeType);
        jsonObject.put("encStr",computedCommonParam.get("encStr"));


        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);
        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS_END.getExplain()+"2" + response.getBody(), orderId);


        if(response.getBody().get("code").toString().equals("0")){
            HashMap<String,String> result = new HashMap<>();
            result.put("rcode",rcode);
            result.put("iccid",iccid);
            return result;
        }
        return null;
    }

    private static HashMap getEsimPackagePurchase(String wmproductId, String userId, Long orderId) throws Exception{
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE.getExplain(), orderId);

        String url = baseUrl + "/Api/SOrder/mybuyesim";
        Map<String,String> computedCommonParam = ComputeSHA1(merchantId + deptId + userId +  wmproductId+1+token);

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate = makeRestTemplate();

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("merchantId",merchantId);
        jsonObject.put("deptId",deptId);
        jsonObject.put("email",userId);


        List<Map<String,Object>> prodList = new ArrayList<>();
        Map<String,Object> prod = new HashMap<>();
        prod.put("wmproductId",wmproductId);
        prod.put("qty",1);
        prodList.add(prod);

        jsonObject.put("prodList",prodList);
        jsonObject.put("encStr",computedCommonParam.get("encStr"));
        jsonObject.put("systemMail",false);



        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);
        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE_END.getExplain() + response.getBody(), orderId);


        return response.getBody();
    }


    public static HashMap getEsimPackages() throws Exception {
        String url = baseUrl + "/Api/QuoteMg/myQueryAll";
        Map<String,String> computedCommonParam = ComputeSHA1(merchantId + token);

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        // SSL 인증 무시
        RestTemplate restTemplate = new RestTemplate(factory);

        restTemplate = makeRestTemplate();


        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("merchantId",merchantId);
        jsonObject.put("encStr",computedCommonParam.get("encStr"));

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);


        return response.getBody();

    }


    public static void main(String[] args) throws Exception {

    }



    public static Map<String,String> ComputeSHA1(String encStrTxt) throws Exception{

        String encStr = shaAndHex(encStrTxt, "SHA1");
        Map<String,String> result = new HashMap<>();
        result.put("encStr",encStr);

        return result;
    }

    //MD5 암호화와 Hex(16진수) 인코딩
    public static String shaAndHex(String plainText, String Algorithms) throws Exception {

        //MessageDigest 인스턴스 생성(MD5)
        MessageDigest md = MessageDigest.getInstance(Algorithms);

        //해쉬값 업데이트
        md.update(plainText.getBytes("utf-8"));

        //Byte To Hex
        return Hex.encodeHexString(md.digest());
    }

    private static RestTemplate makeRestTemplate() throws Exception {

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        requestFactory.setConnectTimeout(5000); // 커넥션 최대 시간
        requestFactory.setReadTimeout(45000); // 읽기 최대 시간


        return new RestTemplate(requestFactory);
    }

    public static double gbToMb(double gb) {
        return Math.round(gb * 1024 * 100) / 100.0;
    }
    public static double convertBytesToMB(long bytes) {
        return Math.round((double) bytes / 1048576.0 * 100) / 100.0;
    }







}
