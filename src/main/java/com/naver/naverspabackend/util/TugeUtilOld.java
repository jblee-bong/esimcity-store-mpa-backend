package com.naver.naverspabackend.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.naver.naverspabackend.dto.ApiPurchaseItemDto;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.service.esimapiingsteplogs.EsimApiIngStepLogsService;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

public class TugeUtilOld {

    public static String accountId;

    public static String signKey;

    public static String secretkey;

    public static String vector;

    public static String version;

    public static String baseUrl;

    public static String active;

    public static EsimApiIngStepLogsService esimApiIngStepLogsService;

    public TugeUtilOld(String accountId, String signKey, String secretkey, String vector, String version, String baseUrl, EsimApiIngStepLogsService esimApiIngStepLogsService, String active){
        this.accountId = accountId;
        this.signKey = signKey;
        this.secretkey = secretkey;
        this.vector = vector;
        this.version = version;
        this.baseUrl = baseUrl;
        this.esimApiIngStepLogsService = esimApiIngStepLogsService;
        this.active = active;

    }

    //MD5 암호화와 Hex(16진수) 인코딩
    public static String md5AndHex_apache(String plainText) throws Exception {

        String MD5 = "";
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(plainText.getBytes());
        byte byteData[] = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        MD5 = sb.toString();

        return MD5;
    }
    /**
     * Encryption method
     *
     * @param encData Data that needs to be encrypted
     * @param secretkey Key, 16 digits and letters
     * @param vector Initialization vector, 16-bit numbers and letters
     * @return
     * @throws Exception
     */
    public static String encrypt(String encData) {
        try {
            byte[] raw = secretkey.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "Algorithm/mode/complement method"
            IvParameterSpec iv = new IvParameterSpec(vector.getBytes("utf-8"));// Using CBCmode, a vector iv is needed, which can increase the strength of the encryption algorithm
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(encData.getBytes("utf-8"));
            return encodeBytes(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

     /* To hexadecimal
        *
         * @param bytes
        * @return
     */
    public static String encodeBytes(byte[]bytes){
        StringBuffer strBuf=new StringBuffer();
        for(int i=0;i<bytes.length;i++){
            strBuf.append((char)(((bytes[i]>>4)&0xF)+((int) 'a')));
            strBuf.append((char)(((bytes[i])&0xF)+((int)'a')));
        }
        return strBuf.toString();
    }
    /**
     * Decryption method
     *
     * @param decData Data to be decrypted
     * @param secretkey Key, 16 digits and letters
     * @param vector Initialization vector, 16-bit numbers and letters
     * @return
     * @throws Exception
     */
    public static String decrypt(String decData) {
        try {
            byte[] raw = secretkey.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(vector.getBytes("utf-8"));
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = decodeBytes(decData);
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *Bytearray
     *
     *@paramstr
     *@return
     */
    public static byte[]decodeBytes(String str){
        byte[]bytes=new byte[str.length()/2];
        for(int i=0;i<str.length();i+=2){
            char c=str.charAt(i);
            bytes[i/2]=(byte)((c-'a')<<4);
            c=str.charAt(i+1);
            bytes[i/2]+=(c-'a');
        }
        return bytes;
    }



    public static String contextLoads2(String userId, String esimProductId, String orderIdWithQuantityNumber, Long orderId) throws Exception {

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);

        System.setProperty("https.protocols","TLSv1.2");


        HashMap esimResult =  getEsimPackagePurchase(esimProductId,userId,orderIdWithQuantityNumber, orderId);

        Gson gson = new Gson();
        if(esimResult.get("code").toString().equals("0000")){
            String data = decrypt(esimResult.get("data").toString());
            List<HashMap<String,Object>> orderNo = gson.fromJson(data, new TypeToken<List<HashMap<String,Object>>>(){}.getType());


            esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE_END0.getExplain() + data, orderId);

            return    orderNo.get(0).get("orderNo").toString();
        }

        return null;
    }


    public static HashMap contextLoads3(String activationRequestId, Long orderId) throws Exception {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);

        System.setProperty("https.protocols","TLSv1.2");


        HashMap esimResult =  getEsimPurchaseStatus(activationRequestId,orderId);

        Gson gson = new Gson();
        if(esimResult.get("code").toString().equals("0000")){
            String data = decrypt(esimResult.get("data").toString());
            List<HashMap<String,Object>> orderNo = gson.fromJson(data, new TypeToken<List<HashMap<String,Object>>>(){}.getType());
            esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS_END.getExplain() + data, orderId);

            return    orderNo.get(0);
        }

        return null;
    }
    public static void contextLoads4(String orderId, String iccid, Model model, ApiPurchaseItemDto apiPurchaseItemDto) throws Exception {
        System.setProperty("https.protocols","TLSv1.2");

        HashMap esimResult =  getEsimStatus(orderId,iccid);
        HashMap esimResult2 =  getEsimStatus2(orderId);

        Gson gson = new Gson();
        if(esimResult.get("code").toString().equals("0000") && esimResult2.get("code").toString().equals("0000")){
            HashMap<String,Object> esimMap = gson.fromJson(decrypt(esimResult.get("data").toString()), HashMap.class);
            HashMap<String,Object> esimMap2 = gson.fromJson(decrypt(esimResult2.get("data").toString()), HashMap.class);

            String[] apiPurchaseItemProcutIds = apiPurchaseItemDto.getApiPurchaseItemProcutId().split("-");
            if(apiPurchaseItemDto.isApiPurchaseItemIsDaily()){
                String dailySize = apiPurchaseItemProcutIds[apiPurchaseItemProcutIds.length-1];
                if(dailySize.equals("U") || dailySize.equals("Unlimited") ){
                    model.addAttribute("totalUsage","unlimited");//현재 사이클 전체데이터
                }else{
                    if(dailySize.indexOf("MB")>-1){

                        model.addAttribute("totalUsage",dailySize.replace("MB",""));//현재 사이클 전체데이터
                    }
                    else if(dailySize.indexOf("GB")>-1){
                        model.addAttribute("totalUsage",Double.parseDouble(dailySize.replace("GB",""))*1024);//현재 사이클 전체데이터
                    }
                    else
                        model.addAttribute("totalUsage","unlimited");//현재 사이클 전체데이터
                }
            }



            if(esimMap.get("dataUsage")!=null && !esimMap.get("dataUsage").toString().equals(""))
                model.addAttribute("usage",esimMap.get("dataUsage") ); //현재 사이클 사용량
            else
                model.addAttribute("usage",0); //현재 사이클 사용량
            if(esimMap.get("dataTotal").equals("unlimited")) {
                model.addAttribute("remaining","unlimited");
            }
            else if(esimMap.get("dataUsage")!=null && !esimMap.get("dataUsage").toString().equals(""))
                model.addAttribute("remaining",(Float.parseFloat(esimMap.get("dataTotal").toString()) - Float.parseFloat(esimMap.get("dataUsage").toString()))); //현재 사이클  데이터 남은량
            else
                model.addAttribute("remaining",Float.parseFloat(esimMap.get("dataTotal").toString())); //현재 사이클  데이터 남은량


            if(esimMap2.get("installDevice")!=null)
                model.addAttribute("installDevice",esimMap2.get("installDevice").toString());
            SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // String 타입을 Date 타입으로 변환
            Calendar cal = Calendar.getInstance();
            Date updateDate = newDtFormat.parse(esimMap2.get("updateTime").toString()); //수정일인데 필요없을듯
            cal.setTime(updateDate);
            cal.add(Calendar.HOUR, 9);//GMT+0 시간이라 +9시간해줘야 한국시간
            String updateDateString = newDtFormat.format(cal.getTime());
            model.addAttribute("updateAt",updateDateString);

            if(esimMap2.get("installTime")!=null && !esimMap2.get("installTime").equals("")){
                Date installTime = newDtFormat.parse(esimMap2.get("installTime").toString()); //설치일
                cal.setTime(installTime);
                cal.add(Calendar.HOUR, 9);//GMT+0 시간이라 +9시간해줘야 한국시간
                String installTimeString = newDtFormat.format(cal.getTime());
                model.addAttribute("installAt",installTimeString);
            }else{
                model.addAttribute("installAt","");
            }

            if(esimMap2.get("renewExpirationTime")!=null && !esimMap2.get("renewExpirationTime").equals("")){
                Date renewExpirationTime = newDtFormat.parse(esimMap2.get("renewExpirationTime").toString());
                cal.setTime(renewExpirationTime);
                cal.add(Calendar.HOUR, 9);//GMT+0 시간이라 +9시간해줘야 한국시간
                String renewExpirationTimeString = newDtFormat.format(cal.getTime());
                model.addAttribute("expiredAt",renewExpirationTimeString); //연장 가능한 최종 시간


                Date expiredAt =  newDtFormat.parse(renewExpirationTimeString);
                Date now = new Date();
                model.addAttribute("end",now.after(expiredAt));
            }else{
                model.addAttribute("expiredAt","");
            }



            model.addAttribute("iccid",iccid);
        }

    }


    private static HashMap getEsimPurchaseStatus(String activationRequestId, Long orderId) throws Exception{
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS.getExplain(), orderId);

        String serviceName = "queryEsimOrderList";
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = formatter.format(now);

        String url = baseUrl +"saleOrderApi/"+ serviceName;

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Gson gson = new Gson();
        Map<String,Object> data = new HashMap<>();
        data.put("iccid","");
        data.put("otaOrderNo","");
        data.put("orderNo",activationRequestId);
        data.put("pageSize",10);
        data.put("orderStatus","");
        data.put("page",1);
        data.put("lang","en");

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("requestTime", timeStamp);
        jsonObject.put("accountId",accountId);
        jsonObject.put("data", encrypt(gson.toJson(data)));
        jsonObject.put("sign",sign(serviceName,timeStamp,encrypt(gson.toJson(data))));
        jsonObject.put("serviceName",serviceName);
        jsonObject.put("version",version);


        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);
        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS_END0.getExplain() + response.getBody(), orderId);

        return response.getBody();
    }

    private static HashMap getEsimPackagePurchase(String esimProductId, String userId, String orderIdWithQuantityNumber, Long orderId) throws Exception{
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE.getExplain(), orderId);


        String serviceName = "openCard";
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = formatter.format(now);

        String url = baseUrl +"saleOrderApi/"+ serviceName;

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Gson gson = new Gson();
        Map<String,Object> data = new HashMap<>();
        data.put("otaOrderNo",orderIdWithQuantityNumber);
        data.put("productCode",esimProductId);
        data.put("iccidAmount",1);
        data.put("notifyUrl","https://www.esimcity.com/alarm");
        data.put("currency","USD");
        data.put("lang","en");
        data.put("startDate","");
        data.put("email","");

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("requestTime", timeStamp);
        jsonObject.put("accountId",accountId);
        jsonObject.put("data", encrypt(gson.toJson(data)));
        jsonObject.put("sign",sign(serviceName,timeStamp,encrypt(gson.toJson(data))));
        jsonObject.put("serviceName",serviceName);
        jsonObject.put("version",version);


        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);

        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE_END0.getExplain() + response.getBody(), orderId);

        return response.getBody();
    }

    public static String sign(String serviceName, String requestTime, String body) throws Exception{
        return md5AndHex_apache(accountId+serviceName+requestTime+body+version+signKey);
    }





    private static HashMap getEsimStatus(String orderId, String iccid) throws Exception {

        String serviceName = "getEsimFlowByParams";
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = formatter.format(now);

        String url = baseUrl + "saleSimApi/" + serviceName;

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Gson gson = new Gson();
        Map<String,Object> data = new HashMap<>();
        data.put("iccid",iccid);
        data.put("orderNo",orderId);
        data.put("lang","en");

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("requestTime", timeStamp);
        jsonObject.put("accountId",accountId);
        jsonObject.put("data", encrypt(gson.toJson(data)));
        jsonObject.put("serviceName",serviceName);
        jsonObject.put("version",version);
        jsonObject.put("sign",sign(serviceName,timeStamp,encrypt(gson.toJson(data))));


        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        return response.getBody();

    }




    private static HashMap getEsimStatus2(String orderId) throws Exception {

        String serviceName = "getProfileInfo";
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = formatter.format(now);

        String url = baseUrl + "saleSimApi/" + serviceName;

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(5000); // 커넥션 최대 시간
        factory.setReadTimeout(45000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Gson gson = new Gson();
        Map<String,Object> data = new HashMap<>();
        data.put("orderNo",orderId);
        data.put("lang","en");

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("requestTime", timeStamp);
        jsonObject.put("accountId",accountId);
        jsonObject.put("data", encrypt(gson.toJson(data)));
        jsonObject.put("serviceName",serviceName);
        jsonObject.put("version",version);
        jsonObject.put("sign",sign(serviceName,timeStamp,encrypt(gson.toJson(data))));


        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        return response.getBody();

    }






}
