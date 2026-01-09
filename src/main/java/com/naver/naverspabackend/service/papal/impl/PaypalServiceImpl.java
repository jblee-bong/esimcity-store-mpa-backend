package com.naver.naverspabackend.service.papal.impl;

import com.google.gson.Gson;
import com.naver.naverspabackend.dto.*;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.service.apipurchaseitem.ApiPurchaseItemService;
import com.naver.naverspabackend.service.esimPrice.EsimPriceService;
import com.naver.naverspabackend.service.order.OrderService;
import com.naver.naverspabackend.service.papal.PaypalService;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.service.topupOrder.TopupOrderService;
import com.naver.naverspabackend.util.EsimUtil;
import com.naver.naverspabackend.util.TsimUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaypalServiceImpl implements PaypalService {



    @Value("${paypal.clientId}")
    private String clientId;

    @Value("${paypal.clientSecret}")
    private String clientSecret;


    @Value("${paypal.successCallback}")
    private String successCallback;
    @Value("${paypal.cancelCallback}")
    private String cancelCallback;


    @Autowired
    private EsimPriceService esimPriceService;

    @Autowired
    private TopupOrderService topupOrderService;

    @Autowired
    private StoreService storeService;


    @Value("${spring.profiles.active}")
    private String active;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ApiPurchaseItemService apiPurchaseItemService;


    @Value("${paypal.apiUrl}")
    private String paypalApiUrl; // 실결제 시 api-m.paypal.com

    @Override
    public String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(paypalApiUrl + "/v1/oauth2/token", request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    @Override
    public String createOrder(Map<String, Object> params) throws Exception {
        String id = params.get("realOrderId").toString();
        String type = params.get("type").toString();
        String iccid = params.get("iccid").toString(); //결제비용
        Map<String, Object> map = new HashMap<>();
        map.put("id",id);
        OrderDto orderDto = orderService.fetchOrderOnly(map);
        if(orderDto==null){
            throw new Exception();
        }
        TopupOrderDto topupOrderDto = new TopupOrderDto();
        topupOrderDto.setStoreId(orderDto.getStoreId());
        topupOrderDto.setOrderId(orderDto.getId());
        topupOrderDto.setPaymentStatus(0);
        topupOrderDto.setTopupStatus(0);
        topupOrderDto.setEsimCorp(type);
        topupOrderDto.setEsimIccid(iccid);
        topupOrderDto.setShippingTel(orderDto.getShippingTel1());
        topupOrderDto.setShippingName(orderDto.getShippingName());
        topupOrderDto.setShippingMail(orderDto.getTransMail());
        //위에3개공통

        Gson gson = new Gson();
        Double krwPrice = null;
        if(type.equals(ApiType.TSIM.name())){
            //아래2개 티심용
            String apiPurchaseItemProcutId = params.get("apiPurchaseItemProcutId").toString(); //충전상품ID
            String deviceId = params.get("deviceId").toString(); //결제비용
            Map<String,String> topupPartamJson = new HashMap<>();
            topupPartamJson.put("apiPurchaseItemProcutId",apiPurchaseItemProcutId);
            topupPartamJson.put("deviceId",deviceId);
            topupOrderDto.setTopupParamJson(gson.toJson(topupPartamJson));
            ApiPurchaseItemDto param = new ApiPurchaseItemDto();
            param.setApiPurchaseItemProcutId(apiPurchaseItemProcutId);
            param.setApiPurchaseItemType(type);
            ApiPurchaseItemDto apiPurchaseItemDto = apiPurchaseItemService.findById(param);
            if(apiPurchaseItemDto==null){
                throw new Exception();
            }
            topupOrderDto.setProductOption(apiPurchaseItemDto.getApiPurchaseItemDays() +"일 " + apiPurchaseItemDto.getApiPurchaseDataTotal());
            krwPrice = Double.valueOf(apiPurchaseItemDto.getApiPurchaseKrwPrice());
        }

        EsimPriceDto esimPriceParam = new EsimPriceDto();
        esimPriceParam.setType(type);
        EsimPriceDto esimPriceDto = esimPriceService.findById(esimPriceParam);
        Double weight1 = esimPriceDto.getWeight1();
        Double weight2 = esimPriceDto.getWeight2();
        Double weight3 = esimPriceDto.getWeight3();
        Double weight4 = esimPriceDto.getWeight4();
        Double weight5 = esimPriceDto.getWeight5();
        Double echangeRate = esimPriceDto.getExchangeRate() ;//KRW->USD로하는거기때문에 환율가중치는 추가안함. * 면가격이 더낮아짐



        double apiPrice =  krwPrice;
        double priceWeight = 0;
        //금액으로인한 가중치
        if(apiPrice<= 5000){
            priceWeight = weight1;
        }else if(apiPrice<= 10000){
            priceWeight = weight2;
        }else if(apiPrice<= 15000){
            priceWeight = weight3;
        }else if(apiPrice<= 20000){
            priceWeight = weight4;
        }else{
            priceWeight = weight5;
        }
        double price = Math.round(apiPrice * priceWeight / 100.0) * 100;
        topupOrderDto.setTopupKrwprice((price * 0.95) + "");
        Double usdAmount = (price / echangeRate) * 0.95; //5프로할인 (충전의경우)
        double amount = Math.round(usdAmount * 100.0) / 100.0;
        topupOrderDto.setTopupUsdprice(amount+"");

        String currency = "USD";
        String accessToken = getAccessToken();
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 페이팔 요청 데이터 구성
        String jsonBody = "{" +
                "\"intent\": \"CAPTURE\"," +
                "\"purchase_units\": [{\"amount\": {\"currency_code\": \""+currency+"\", \"value\": \"" + amount + "\"}}]," +
                "\"application_context\": {" +
                "\"return_url\": \""+successCallback+"\"," +
                "\"cancel_url\": \""+cancelCallback+"\"" +
                "}" +
                "}";

        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(paypalApiUrl + "/v2/checkout/orders", request, Map.class);

        Map<String, Object> responseBody = response.getBody();

        // 1. 페이팔이 생성한 Order ID 추출
        String token = (String) responseBody.get("id");
        topupOrderDto.setTokenId(token);

        //주문정보 ISERT
        topupOrderService.insert(topupOrderDto);

        // 결과에서 approval_url (사용자가 접속할 페이지) 추출
        List<Map<String, String>> links = (List<Map<String, String>>) response.getBody().get("links");
        return links.stream()
                .filter(link -> "approve".equals(link.get("rel")))
                .findFirst()
                .get()
                .get("href");
    }


    @Override
    public String captureOrder(String token) {
        try{
            TopupOrderDto param = new TopupOrderDto();
            param.setTokenId(token);
            TopupOrderDto topupOrderDto = topupOrderService.findByTokenId(param);
            if(topupOrderDto==null){
                throw new Exception();
            }
            if(topupOrderDto.getPaymentStatus()!=0 || topupOrderDto.getTopupStatus()!=0){
                return "already";
            }
            Map<String, Object> data = new HashMap<>();
            data.put("id",topupOrderDto.getStoreId());
            StoreDto storeDto = storeService.findById(data);


            if(topupOrderDto.getEsimCorp().equals(ApiType.TSIM.name())){
                TsimUtil tsimUtil = EsimUtil.getTsimUtil(apiPurchaseItemService,storeDto,active);

                HashMap resultMap = tsimUtil.contextLoads5(topupOrderDto);

                if(Integer.parseInt(resultMap.get("code").toString().toString()) == 1 && resultMap.get("msg").toString().equals("Success")){
                    topupOrderDto.setTopupStatus(1);
                    topupOrderService.updateTopupStatus(topupOrderDto);
                    boolean paypalResult = doPaymentComfirm(token);
                    if(paypalResult){
                        topupOrderDto.setPaymentStatus(1);
                    }else{
                        topupOrderDto.setPaymentStatus(2);
                    }
                    topupOrderService.updatePaypalStatus(topupOrderDto);
                    if(topupOrderDto.getPaymentStatus()==1){
                        return "success";
                    }else{

                        return "topupSuccessBotPaymentFail";
                    }
                }else{
                    topupOrderDto.setTopupStatus(2);
                    topupOrderService.updateTopupStatus(topupOrderDto);


                    return "fail";
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "error";


    }

    @Override
    public void makeModel(String token, Model model) throws Exception {
        TopupOrderDto param = new TopupOrderDto();
        param.setTokenId(token);
        TopupOrderDto topupOrderDto = topupOrderService.findByTokenId(param);
        if(topupOrderDto==null){
            throw new Exception();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id",topupOrderDto.getStoreId());
        StoreDto storeDto = storeService.findById(data);
        model.addAttribute("productName",topupOrderDto.getProductOption());
        model.addAttribute("iccid",topupOrderDto.getEsimIccid());
        model.addAttribute("chargeDate",new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        model.addAttribute("esimCopyWrite",storeDto.getEsimCopyWrite());
        model.addAttribute("esimQuestLink",storeDto.getEsimQuestLink());
        model.addAttribute("esimLogoLink",storeDto.getEsimLogoLink());

        if(topupOrderDto.getPaymentStatus()==1 && topupOrderDto.getTopupStatus()==1){
            model.addAttribute("success",true);
        }
        else if(topupOrderDto.getPaymentStatus()==2 && topupOrderDto.getTopupStatus()==1){
            model.addAttribute("success",false);
            model.addAttribute("errorMessage","충전은 완료하였으나, 결제에 실패하였습니다. 추후 결제 요청 드리겠습니다. 즐거운 여행되세요.");
        }else{
            model.addAttribute("success",false);
            model.addAttribute("errorMessage","충전 요청 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }

    public boolean doPaymentComfirm(String token){
        String accessToken = getAccessToken(); // 이전에 만든 토큰 발급 메소드 사용
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Capture API는 빈 본문({})을 요구합니다.
        HttpEntity<String> request = new HttpEntity<>("{}", headers);

        try {
            // v2/checkout/orders/{order_id}/capture 호출
            String url = paypalApiUrl + "/v2/checkout/orders/" + token + "/capture";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                // 상태가 COMPLETED라면 실제 돈이 빠져나간 것입니다.
                if("COMPLETED".equals(body.get("status"))){
                    return  true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean refundPayment(String captureId) {
        String accessToken = getAccessToken();
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 환불 요청 (빈 객체를 보내면 전체 환불)
        HttpEntity<String> request = new HttpEntity<>("{}", headers);

        try {
            // POST /v2/payments/captures/{capture_id}/refund
            String url = paypalApiUrl + "/v2/payments/captures/" + captureId + "/refund";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
