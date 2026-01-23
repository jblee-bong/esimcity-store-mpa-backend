package com.naver.naverspabackend.service.payup.impl;

import com.google.gson.Gson;
import com.naver.naverspabackend.dto.*;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.service.apipurchaseitem.ApiPurchaseItemService;
import com.naver.naverspabackend.service.esimPrice.EsimPriceService;
import com.naver.naverspabackend.service.order.OrderService;
import com.naver.naverspabackend.service.payup.PayUpService;
import com.naver.naverspabackend.service.sms.KakaoService;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.service.topupOrder.TopupOrderService;
import com.naver.naverspabackend.util.EsimAccessUtil;
import com.naver.naverspabackend.util.EsimUtil;
import com.naver.naverspabackend.util.TsimUtil;
import com.naver.naverspabackend.util.TugeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PayUpServiceImpl implements PayUpService {


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


    @Value("${payup.merchantId}")
    private String merchantId;

    @Value("${payup.apiKey}")
    private String apiKey;

    @Value("${payup.redirectUrl}")
    private String redirectUrl;

    @Value("${payup.baseUrl}")
    private String baseUrl;

    @Value("${payup.authTokenURI}")
    private String authTokenURI;

    @Value("${payup.comfirmURI}")
    private String comfirmURI;

    @Value("${payup.cancelURI}")
    private String cancelURI;

    @Autowired
    private KakaoService kakaoService;


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
            topupOrderDto.setProductOption(apiPurchaseItemDto.getApiPurchaseItemDays() +"일 " + apiPurchaseItemDto.getApiPurchaseDataTotal() + " 저속 무제한");
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
                                            (apiPurchaseItemDto.isApiPurchaseItemIsDaily()?"매일 ":"총 ") + apiPurchaseItemDto.getApiPurchaseDataTotal()
                                    )
                    )
            );
            krwPrice = Double.valueOf(apiPurchaseItemDto.getApiPurchaseKrwPrice());
        }else
        if(type.equals(ApiType.ESIMACCESS.name())){
            //아래2개 티심용
            String[] apiPurchaseItemProcutIds = params.get("apiPurchaseItemProcutId").toString().split("\\|\\^\\|");
            String apiPurchaseItemProcutId = apiPurchaseItemProcutIds[0]; //충전상품ID
            String volumn = apiPurchaseItemProcutIds[1]; //사이즈
            String price = apiPurchaseItemProcutIds[2]; //가격(달러)
            String duration = apiPurchaseItemProcutIds[3]; //사용기간
            Map<String,String> topupPartamJson = new HashMap<>();
            topupPartamJson.put("apiPurchaseItemProcutId",apiPurchaseItemProcutId);
            topupOrderDto.setTopupParamJson(gson.toJson(topupPartamJson));


            long chargeBytes = Long.parseLong(volumn);
            String chargeString = "";
            long gbBoundary = 1024L * 1024 * 1024;
            if (chargeBytes >= gbBoundary) {
                // GB로 변환 (소수점 둘째자리까지)
                double gbValue = (double) chargeBytes / gbBoundary;
                chargeString = String.format("%.2f GB", gbValue);
            } else {
                // MB로 변환 (소수점 둘째자리까지)
                double mbValue = (double) chargeBytes / (1024 * 1024);
                chargeString = String.format("%.2f MB", mbValue);
            }
            topupOrderDto.setProductOption(duration +"일 " + "총 " + chargeString);

            EsimPriceDto esimPriceParam = new EsimPriceDto();
            esimPriceParam.setType(ApiType.ESIMACCESS.name());
            EsimPriceDto esimPriceDto = esimPriceService.findById(esimPriceParam);
            Double echangeRate = esimPriceDto.getExchangeRate() * esimPriceDto.getExchangeWeight();


            krwPrice = echangeRate *(Double.parseDouble(price)/10000); //만을나누는이유는 esimaccess는 달러에 10000 곱해서 줌
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
        topupOrderDto.setTopupKrwprice(( (int) Math.round(price) ) + "");
        Double usdAmount = (price / echangeRate); //5프로할인 (충전의경우)
        double amount = Math.round(usdAmount * 100.0) / 100.0;
        topupOrderDto.setTopupUsdprice(amount+"");

        // 1. Order ID 추출
        String token =orderDto.getOrderId()+ UUID.randomUUID().toString().substring(0,5) + new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date());
        topupOrderDto.setTokenId(token);
        //주문정보 ISERT
        topupOrderService.insert(topupOrderDto);

        result.put("result",true);
        result.put("token",token);
        result.put("orderName",topupOrderDto.getProductOption());
        result.put("totalAmount",Integer.parseInt(topupOrderDto.getTopupKrwprice()));
        result.put("merchantId",merchantId);
        result.put("redirectUrl",redirectUrl);
        result.put("userName",topupOrderDto.getShippingName());
        return result;
    }

    @Override
    public void captureOrder(Model model, String transactionId, String token, String amount) {
        try{
            String accessToken = getAccessToken();
            if(accessToken==null){
                throw new Exception();
            }
            TopupOrderDto param = new TopupOrderDto();
            System.out.println(transactionId);
            System.out.println(token);
            System.out.println(amount);
            param.setTokenId(token);
            TopupOrderDto topupOrderDto = topupOrderService.findByTokenId(param);
            if(topupOrderDto==null){
                throw new Exception();
            }
            topupOrderDto.setTopupTransactionId(transactionId);
            topupOrderService.updateTransactionId(topupOrderDto);

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

            String paymentResult = doPaymentCheck(transactionId,token,amount,accessToken);
            if(paymentResult==null || !paymentResult.equals("SUCCESS")) {//결제실패
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
                    topupOrderDto.setTopupStatus(3);
                    topupOrderService.updateTopupStatus(topupOrderDto);
                    model.addAttribute("success",true);

                    model.addAttribute("topupType",topupOrderDto.getEsimCorp());
                    return;
                }
            }
            else if(topupOrderDto.getEsimCorp().equals(ApiType.ESIMACCESS.name())){
                EsimAccessUtil esimAccessUtil = EsimUtil.getEsimAccess(storeDto);
                //티심 충전
                HashMap resultMap = esimAccessUtil.contextLoads5(topupOrderDto);

                if((Boolean) resultMap.get("success")){
                    try{
                        Map<String, Object> result = (Map<String, Object>) resultMap.get("obj");
                        topupOrderDto.setTopupOrderNo(result.get("transactionId").toString());
                    }catch (Exception e){
                        e.printStackTrace();
                        topupOrderDto.setTopupOrderNo(topupOrderDto.getTokenId());
                    }
                    topupOrderService.updateTopupOrderNo(topupOrderDto);
                    topupOrderDto.setTopupStatus(1);
                    topupOrderService.updateTopupStatus(topupOrderDto);
                    model.addAttribute("success",true);
                    return;
                }
            }

            topupOrderDto.setTopupStatus(2);
            topupOrderService.updateTopupStatus(topupOrderDto);
            String paymentRefundResult =  refundPayment(transactionId, accessToken );
            if(paymentRefundResult==null || !paymentRefundResult.equals("SUCCESS")) {//결제실패
                model.addAttribute("errorMessage","충전 요청 중 오류가 발생했습니다. 다시 시도해주세요.");
                topupOrderDto.setPaymentStatus(3);
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



    private String doPaymentCheck(String transactionId, String token, String amount, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            String url =  baseUrl  + comfirmURI;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.add("Authorization",accessToken);

            Map<String,Object> jsonObject = new HashMap<>();
            jsonObject.put("transactionId",transactionId);
            jsonObject.put("merchantId",merchantId);
            jsonObject.put("orderNumber",token);
            jsonObject.put("amount",amount);
            HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


            ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

            Map<String, Object> result = response.getBody();
            return  result.get("status").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public String refundPayment(String transactionId, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            String url =  baseUrl  + cancelURI;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.add("Authorization",accessToken);

            Map<String,Object> jsonObject = new HashMap<>();
            jsonObject.put("transactionId",transactionId);
            jsonObject.put("merchantId",merchantId);
            jsonObject.put("cancelReason","충전실패");
            HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


            ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

            Map<String, Object> result = response.getBody();
            return  result.get("status").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAccessToken(){
        RestTemplate restTemplate = new RestTemplate();
        try {
            String url =  baseUrl  + authTokenURI;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            Map<String,Object> jsonObject = new HashMap<>();
            jsonObject.put("merchantId",merchantId);
            jsonObject.put("apiKey",apiKey);
            HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(jsonObject), headers);


            ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, entity, HashMap.class);

            Map<String, Object> result = response.getBody();
            if(result.get("status").toString().equals("SUCCESS")){
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                return  data.get("accessToken").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
