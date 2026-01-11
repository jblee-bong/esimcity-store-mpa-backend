package com.naver.naverspabackend.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.naver.naverspabackend.dto.ApiPurchaseItemDto;
import com.naver.naverspabackend.dto.TopupOrderDto;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
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

public class EsimAccessUtil {


    public static String clientId;

    static String clientSecret;

    static String baseUrl;

    static String active;

    public static EsimApiIngStepLogsService esimApiIngStepLogsService;


    public static ApiPurchaseItemService apiPurchaseItemService;
    public static OrderMapper orderMapper;
    public EsimAccessUtil(String clientId, String clientSecret, String baseUrl, EsimApiIngStepLogsService esimApiIngStepLogsService,ApiPurchaseItemService apiPurchaseItemService, OrderMapper orderMapper){
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseUrl = baseUrl;
        this.esimApiIngStepLogsService = esimApiIngStepLogsService;
        this.apiPurchaseItemService = apiPurchaseItemService;
        this.orderMapper = orderMapper;
    }



    public static HashMap contextLoads2( String esimProductId,String esimProductDays, Long orderId) throws Exception {

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);
        System.setProperty("https.protocols","TLSv1.2");
        return  getEsimPackagePurchase(esimProductId, esimProductDays, orderId);


    }
    public static HashMap contextLoads3(String activationRequestId, Long orderId) throws Exception {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);
        System.setProperty("https.protocols","TLSv1.2");
        return getEsimPurchaseStatus(activationRequestId,orderId);
    }

    private static HashMap getEsimPurchaseStatus(String activationRequestId, Long orderId) {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS.getExplain(), orderId);

        String url = baseUrl + "/v1/open/esim/query";
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(60000 * 1000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(60000); // 커넥션 최대 시간
        factory.setReadTimeout(60000); // 읽기 최대 시간
        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("RT-AccessCode",clientId);
        Map<String,Object> pager = new HashMap<>();
        pager.put("pageSize",10);
        pager.put("pageNum",1);

        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("orderNo",activationRequestId);
        jsonObject.put("pager",pager);

        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.STATUS_END.getExplain() + response.getBody(), orderId);

        return response.getBody();
    }


    private static HashMap getEsimPackagePurchase(String esimProductId,  String esimProductDays, Long orderId) {

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE.getExplain(), orderId);
        String url = baseUrl + "/v1/open/esim/order";
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(60000 * 1000); // 커넥션풀에서 사용 가능한 연결을 가져오기 위해 대기하는 최대 시간
        factory.setConnectTimeout(60000); // 커넥션 최대 시간
        factory.setReadTimeout(60000); // 읽기 최대 시간

        RestTemplate restTemplate = new RestTemplate(factory);

        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("RT-AccessCode",clientId);

        List<Map<String,Object>> packageInfoList = new ArrayList<>();
        Map<String,Object> packageInfo = new HashMap<>();


        ApiPurchaseItemDto param = new ApiPurchaseItemDto();
        param.setApiPurchaseItemProcutId(esimProductId);
        param.setApiPurchaseItemType(ApiType.ESIMACCESS.name());
        ApiPurchaseItemDto apiPurchaseItemDto = apiPurchaseItemService.findById(param);

        packageInfo.put("packageCode",esimProductId);
        packageInfo.put("count",1);//몇개구매할껀지
        packageInfo.put("price",Double.parseDouble(apiPurchaseItemDto.getApiPurchasePrice()) * 10000); //구매가격을 넣어줌 가격이 틀리면 실패 (가격변동막아줌)
        if(esimProductDays!=null && !esimProductDays.trim().equals(""))
            packageInfo.put("periodNum",Integer.parseInt(esimProductDays));

        packageInfoList.add(packageInfo);


        Map<String,Object> jsonObject = new HashMap<>();
        jsonObject.put("transactionId",UUID.randomUUID().toString().substring(0,5) + new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date()));


        jsonObject.put("amount",getAmount(apiPurchaseItemDto.getApiPurchasePrice(),esimProductDays));
        jsonObject.put("packageInfoList",packageInfoList);

        esimApiIngStepLogsService.insertRest(headers,jsonObject, orderId);

        HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


        ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE_END.getExplain() + response.getBody(), orderId);


        return response.getBody();
    }
    private static Long getAmount(String amount, String esimProductDays){
        //double finalValue =  Double.parseDouble(amount) * 10000 * discountRatio(esimProductDays); 할인율 뺸 금액인가봄
        double finalValue =  Double.parseDouble(amount) * 10000;

        if(esimProductDays==null || esimProductDays.trim().equals("")){
            return  Math.round(finalValue);
        }
        return Math.round(finalValue) * Integer.parseInt(esimProductDays);
    }
    private static Double discountRatio(String daily){
        if(daily==null || daily.trim().equals("")){
            return  1.0;
        }
        Integer dailyCount = Integer.parseInt(daily);
        if(dailyCount>=1 && dailyCount<=4){
            return (Double) 0.96;
        }else if(dailyCount>=5 && dailyCount<=9){
            return (Double) 0.92;
        }else if(dailyCount>=10 && dailyCount<=19){
            return (Double) 0.89;
        }else if(dailyCount>=20 && dailyCount<=29){
            return (Double) 0.85;
        }else{
            return (Double) 0.82;
        }
    }




}
