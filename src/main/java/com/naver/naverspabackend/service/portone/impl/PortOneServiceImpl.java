package com.naver.naverspabackend.service.portone.impl;

import com.google.gson.Gson;
import com.naver.naverspabackend.dto.*;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.service.apipurchaseitem.ApiPurchaseItemService;
import com.naver.naverspabackend.service.esimPrice.EsimPriceService;
import com.naver.naverspabackend.service.order.OrderService;
import com.naver.naverspabackend.service.papal.PaypalService;
import com.naver.naverspabackend.service.portone.PortOneService;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.service.topupOrder.TopupOrderService;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.EsimUtil;
import com.naver.naverspabackend.util.TsimUtil;
import com.naver.naverspabackend.util.TugeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PortOneServiceImpl implements PortOneService {



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


    @Value("${portOne.storeId}")
    private String storeId;
    @Value("${portOne.channelKey}")
    private String channelKey;
    @Value("${portOne.secretKey}")
    private String secretKey;

    @Value("${portOne.checkUrl}")
    private String checkUrl;
    @Value("${portOne.redirectUrl}")
    private String redirectUrl;

    @Value("${portOne.cancelUrl}")
    private String cancelUrl;

    @Override
    public String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity( "/v1/oauth2/token", request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    @Override
    public Map<String, Object> createOrder(Map<String, Object> params) throws Exception {
        Map<String, Object> result = new HashMap<>();
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
        }else
        if(type.equals(ApiType.TUGE.name())){
            //아래2개 티심용
            String apiPurchaseItemProcutId = params.get("apiPurchaseItemProcutId").toString(); //충전상품ID
            Map<String,String> topupPartamJson = new HashMap<>();
            topupPartamJson.put("apiPurchaseItemProcutId",apiPurchaseItemProcutId);
            topupOrderDto.setTopupParamJson(gson.toJson(topupPartamJson));
            ApiPurchaseItemDto param = new ApiPurchaseItemDto();
            param.setApiPurchaseItemProcutId(apiPurchaseItemProcutId);
            param.setApiPurchaseItemType(type);
            ApiPurchaseItemDto apiPurchaseItemDto = apiPurchaseItemService.findById(param);
            if(apiPurchaseItemDto==null){
                throw new Exception();
            }
            topupOrderDto.setProductOption(apiPurchaseItemDto.getApiPurchaseItemDays() +"일 " +
                            (
                                    apiPurchaseItemDto.getApiPurchaseDataTotal().equals("Unlimited") ?"무제한":
                                            (
                                                    (apiPurchaseItemDto.isApiPurchaseItemIsDaily()?"매일 ":"총 ") + apiPurchaseItemDto.getApiPurchaseDataTotal() +
                                                            (apiPurchaseItemDto.getApiPurchaseSlowSpeed()!=null?" + 저속 무제한":"")
                                            )
                                    )
                    );
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
        topupOrderDto.setTopupKrwprice((price ) + "");
        Double usdAmount = (price / echangeRate); //5프로할인 (충전의경우)
        double amount = Math.round(usdAmount * 100.0) / 100.0;
        topupOrderDto.setTopupUsdprice(amount+"");



        // 1. 페이팔이 생성한 Order ID 추출
        String token =orderDto.getOrderId()+ UUID.randomUUID().toString().substring(0,5) + new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date());

        topupOrderDto.setTokenId(token);

        //주문정보 ISERT
        topupOrderService.insert(topupOrderDto);

        result.put("result",true);
        result.put("token",token);
        result.put("orderName",topupOrderDto.getProductOption());
        result.put("totalAmount",topupOrderDto.getTopupKrwprice());
        result.put("storeId",storeId);
        result.put("channelKey",channelKey);
        result.put("redirectUrl",redirectUrl);


        return result;
    }


    @Override
    public void captureOrder(Model model,String token) {
        try{
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


            if(topupOrderDto.getPaymentStatus()!=0 || topupOrderDto.getTopupStatus()!=0){

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
                return;
            }

            Map<String, Object> portOneResult = doPaymentCheck(token);
            if(portOneResult==null) {//결제실패
                model.addAttribute("errorMessage","충전 요청 중 오류가 발생했습니다. 다시 시도해주세요.");
                model.addAttribute("success",false);
                topupOrderDto.setPaymentStatus(2);
                topupOrderService.updatePaypalStatus(topupOrderDto);
                return;
            }else{
                topupOrderDto.setPaymentStatus(1);//결제성공
                topupOrderService.updatePaypalStatus(topupOrderDto);
            }

            if(topupOrderDto.getEsimCorp().equals(ApiType.TSIM.name())){
                TsimUtil tsimUtil = EsimUtil.getTsimUtil(apiPurchaseItemService,storeDto,active);
                //티심 충전
                HashMap resultMap = tsimUtil.contextLoads5(topupOrderDto);
                if(Integer.parseInt(resultMap.get("code").toString().toString()) == 1 && resultMap.get("msg").toString().equals("Success")){
                    topupOrderDto.setTopupStatus(1);
                    topupOrderService.updateTopupStatus(topupOrderDto);
                    model.addAttribute("success",true);
                    return;
                }else{
                }
            }
            else if(topupOrderDto.getEsimCorp().equals(ApiType.TUGE.name())){
                TugeUtil tugeUtil = EsimUtil.getTugeUtil(storeDto,active);
                //티심 충전
                HashMap resultMap = tugeUtil.contextLoads5(topupOrderDto);
                if(resultMap.get("code").toString().equals("0000")){
                    HashMap<String,Object> tugeData = (HashMap<String, Object>) resultMap.get("data");
                    topupOrderDto.setTopupOrderNo(tugeData.get("orderNo").toString());
                    topupOrderService.updateTopupOrderNo(topupOrderDto);
                    topupOrderDto.setTopupStatus(1);
                    topupOrderService.updateTopupStatus(topupOrderDto);
                    model.addAttribute("success",true);
                    return;
                }
            }

            topupOrderDto.setTopupStatus(2);
            topupOrderService.updateTopupStatus(topupOrderDto);
            Map<String, Object> portRefundResult =  refundPayment(token);
            if(portRefundResult!=null){
                Map<String, Object> cancellation = (Map<String, Object>) portRefundResult.get("cancellation");
                if(cancellation.get("status").toString().equals("SUCCEEDED")){
                    model.addAttribute("errorMessage","충전 요청 중 오류가 발생했습니다. 다시 시도해주세요.");
                    topupOrderDto.setPaymentStatus(3);
                }else{
                    model.addAttribute("errorMessage","충전 요청 중 오류가 발생하여, 결제 취소를 하였으나 결제 취소에 실패하였습니다. 고객센터로 문의주세요.");
                    topupOrderDto.setPaymentStatus(4);
                }
                topupOrderService.updatePaypalStatus(topupOrderDto);
            }else{
                model.addAttribute("errorMessage","충전 요청 중 오류가 발생하여, 결제 취소를 하였으나 결제 취소에 실패하였습니다. 고객센터로 문의주세요.");
                topupOrderDto.setPaymentStatus(4);
                topupOrderService.updatePaypalStatus(topupOrderDto);
            }
            model.addAttribute("success",false);
            return;
        }catch (Exception e){
            e.printStackTrace();
        }

        model.addAttribute("success",false);
        model.addAttribute("errorMessage","충전 요청 중 오류가 발생했습니다. 다시 시도해주세요.");



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

    public Map<String, Object> doPaymentCheck(String token){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "PortOne " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            // v2/checkout/orders/{order_id}/capture 호출
            String encodedPaymentId = URLEncoder.encode(token, "UTF-8");
            String url =  checkUrl + "/" + encodedPaymentId;

            ResponseEntity<Map> response = restTemplate.exchange(url,HttpMethod.GET,entity,Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                    return  body;
            }else{
                throw new Exception();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Object> refundPayment(String token) {
        try {
            Map<String, String> paramHeader = new HashMap<>();
            paramHeader.put("Authorization", "PortOne " + secretKey);

            Map<String, Object> pathParam = new HashMap<>();
            pathParam.put("token", URLEncoder.encode(token, "UTF-8"));

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reason","충전실패");
            return ApiUtil.postWithRestTemplate(cancelUrl, paramHeader,  paramMap, pathParam, okhttp3.MediaType.parse("application/json; charset=UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
