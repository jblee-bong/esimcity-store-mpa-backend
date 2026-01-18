package com.naver.naverspabackend.batch.tasklet;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.naver.naverspabackend.dto.*;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.TugeRedisRepository;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.service.apipurchaseitem.ApiPurchaseItemService;
import com.naver.naverspabackend.service.esimPrice.EsimPriceService;
import com.naver.naverspabackend.service.product.ProductService;
import com.naver.naverspabackend.service.sms.MatchInfoService;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.util.*;
import okhttp3.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * esim api의 상품 목록 조회화여 db 저장
 *
 * @author jblee
 */



@SpringBootTest
public class ApiPurchaseListSchedulerTest {

    @Autowired
    private NaverRedisRepository naverRedisRepository;


    @Autowired
    private TugeRedisRepository tugeRedisRepository;

    @Autowired
    private ApiPurchaseItemService apiPurchaseItemService;

    @Value("${exchange.key}")
    private String exchangeApiKey;

    @Value("${exchange.url}")
    private String exchangeApiUrl;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductService productService;


    @Autowired
    private MatchInfoService matchInfoService;

    @Autowired
    private EsimPriceService esimPriceService;



    @Value("${naver-api.base}")
    private String baseUrl;

    @Value("${naver-api.product-option-info-change}")
    private String productOtionInfoChangeUrl;

    @Autowired
    private NaverSetting naverSetting;
    @Test
    void ApiPurchaseList () throws Exception {

        Map<String,Double> exchangeRate = getExchangeRate();

        // 흠위에꺼를 쓰고싶은데 현재 환율데로 하면, 충전금액에 대해 비용의 차이가 발생하네. 충전한 달러로 차감하니까 티심, 투지는 그래서 티심 투지의 같은경우 지정해야할듯
            try{
                List<String> cardTypeList = new ArrayList<>();
                List<Boolean> cardRenewList = new ArrayList<>();

                EsimPriceDto param = new EsimPriceDto();
                param.setType(ApiType.TUGE.name());
                EsimPriceDto esimPriceDto = esimPriceService.findById(param);
                Double echangeRate = esimPriceDto.getExchangeRate() * esimPriceDto.getExchangeWeight();
                ApiPurchaseItemDto apiPurchaseItemDto = new ApiPurchaseItemDto();
                apiPurchaseItemDto.setApiPurchaseItemType(ApiType.TUGE.name());
                List<HashMap<String, Object>> itemList = OriginTugeUtil.contextLoads1();
                apiPurchaseItemService.deleteWithApiPurchaseItemType(apiPurchaseItemDto);
                apiPurchaseItemService.deleteWithApiCardType();
                for(int j=0;j<itemList.size();j++){
                    apiPurchaseItemDto.setApiPurchaseItemProcutId(null);
                    apiPurchaseItemDto.setApiPurchaseItemDescription(null);
                    apiPurchaseItemDto.setApiPurchaseItemSelectType(null);
                    apiPurchaseItemDto.setApiPurchaseItemDays(null);
                    if(cardTypeList.indexOf(itemList.get(j).get("cardType").toString())==-1){
                        HashMap<String, Object> cartType = OriginTugeUtil.contextLoads6(itemList.get(j).get("cardType").toString());
                        if(cartType!=null){
                            cardTypeList.add(cartType.get("cardType").toString());
                            if(cartType.get("renewFlag")!=null)
                                cardRenewList.add((Boolean) cartType.get("renewFlag"));
                            else
                                cardRenewList.add(false);


                            ApiCardTypeDto apiCardTypeDto = new ApiCardTypeDto();
                            apiCardTypeDto.setCardType(cartType.get("cardType").toString());
                            if(cartType.get("timeZone")!=null)apiCardTypeDto.setTimeZone(cartType.get("timeZone").toString());
                            if(cartType.get("renewFlag")!=null)apiCardTypeDto.setRenewYn((Boolean) cartType.get("renewFlag"));
                            if(cartType.get("supportGetUsage")!=null)apiCardTypeDto.setSupportGetUsageYn((Boolean) cartType.get("supportGetUsage"));
                            if(cartType.get("renewCount")!=null)apiCardTypeDto.setRenewCount((Integer) cartType.get("renewCount"));
                            apiPurchaseItemService.insertCardType(apiCardTypeDto);
                        }else{
                            cardTypeList.add(itemList.get(j).get("cardType").toString());
                            cardRenewList.add(false);
                        }

                    }



                    if(itemList.get(j).get("countryCodeList")!=null){
                        List<String> contryCodeList = (List<String>) itemList.get(j).get("countryCodeList");
                        String contryCode = "";
                        for(String contry : contryCodeList){
                            if(contryCode.equals("")){
                                contryCode = contry;
                            }else{
                                contryCode += ","+contry;
                            }
                        }
                        apiPurchaseItemDto.setApiPurchaseCoverDomainCode(contryCode);
                    }
                    if(itemList.get(j).get("cardType")!=null)apiPurchaseItemDto.setApiPurchaseItemCardType(itemList.get(j).get("cardType").toString());
                    apiPurchaseItemDto.setApiPurchaseItemPeriodType((Integer) itemList.get(j).get("periodType"));
                    apiPurchaseItemDto.setApiPurchaseItemDays(itemList.get(j).get("usagePeriod").toString());

                    if(itemList.get(j).get("productCode")!=null && !itemList.get(j).get("productCode").equals("")){
                        apiPurchaseItemDto.setApiPurchaseItemProcutId(itemList.get(j).get("productCode").toString());
                        if(itemList.get(j).get("productName")!=null)apiPurchaseItemDto.setApiPurchaseItemDescription(itemList.get(j).get("productName").toString());
                        if(itemList.get(j).get("productSelectType")!=null)apiPurchaseItemDto.setApiPurchaseItemSelectType(itemList.get(j).get("productSelectType").toString());
                        if(itemList.get(j).get("day")!=null)apiPurchaseItemDto.setApiPurchaseItemDays(itemList.get(j).get("day").toString());

                        if(itemList.get(j).get("netPrice")!=null){
                            apiPurchaseItemDto.setApiPurchasePrice(itemList.get(j).get("netPrice").toString());
                            if(echangeRate!=null){
                                double krwPrice = echangeRate *Double.parseDouble(itemList.get(j).get("netPrice").toString());
                                apiPurchaseItemDto.setApiPurchaseKrwPrice(krwPrice+"");
                            }
                        }

                        if(itemList.get(j).get("activeType")!=null){
                            // ACTIVEDBYDEVICE, ACTIVEDBYORDER 이둘중하나인데 어떤걸 써야하는지 확인필요. 뭐냐면 사고 사용자가 활성화하면 개통되는건지 아닌지
                            System.out.println(itemList.get(j).get("activeType").toString());
                        }


                        apiPurchaseItemDto.setApiPurchaseCurrency("$"); //TUGE 단위 존재안함. 무조건 $임



                        if(itemList.get(j).get("productType")!=null){
                            apiPurchaseItemDto.setApiPurchaseProductType(itemList.get(j).get("productType").toString());
                            apiPurchaseItemDto.setApiPurchaseItemIsDaily(itemList.get(j).get("productType").toString().equals("DAILY_PACK"));
                        }
                        // 이건안보임 if(itemList.get(j).get("cover_domain_code")!=null)apiPurchaseItemDto.setApiPurchaseCoverDomainCode(itemList.get(j).get("cover_domain_code").toString());
                        if(itemList.get(j).get("apnDesc")!=null)apiPurchaseItemDto.setApiPurchaseApn(itemList.get(j).get("apnDesc").toString());

                        if(!itemList.get(j).get("productType").toString().equals("DAILY_PACK")){
                            if(itemList.get(j).get("dataTotal")!=null && itemList.get(j).get("dataUnit")!=null){
                                apiPurchaseItemDto.setApiPurchaseDataTotal(itemList.get(j).get("dataTotal").toString()+itemList.get(j).get("dataUnit").toString());
                            }
                        }else{
                            if(itemList.get(j).get("highSpeed")!=null){
                                apiPurchaseItemDto.setApiPurchaseDataTotal(itemList.get(j).get("highSpeed").toString());
                            }
                        }

                        if(itemList.get(j).get("limitSpeed")!=null)apiPurchaseItemDto.setApiPurchaseSlowSpeed(itemList.get(j).get("limitSpeed").toString());

                        if(itemList.get(j).get("cardType")!=null && cardTypeList.indexOf(itemList.get(j).get("cardType").toString()) >-1 && cardRenewList.get(cardTypeList.indexOf(itemList.get(j).get("cardType").toString()))){
                            apiPurchaseItemDto.setApiPurchaseIsCharge(true);
                        }else{
                            apiPurchaseItemDto.setApiPurchaseIsCharge(false);

                        }

                        //TODO 충전기능 오픈전까지 충전불가 아래삭제
                        apiPurchaseItemDto.setApiPurchaseIsCharge(false);




                    }
                    apiPurchaseItemService.insert(apiPurchaseItemDto);
                }

            }catch (Exception e){
                e.printStackTrace();
            }

                try{
                    EsimPriceDto param = new EsimPriceDto();
                    param.setType(ApiType.WORLDMOVE.name());
                    EsimPriceDto esimPriceDto = esimPriceService.findById(param);
                    Double echangeRate = esimPriceDto.getExchangeRate() * esimPriceDto.getExchangeWeight();
                    ApiPurchaseItemDto apiPurchaseItemDto = new ApiPurchaseItemDto();
                    apiPurchaseItemDto.setApiPurchaseItemType(ApiType.WORLDMOVE.name());
                    List<HashMap<String, Object>> itemList = OriginWorldMoveUtil.contextLoads1();
                    apiPurchaseItemService.deleteWithApiPurchaseItemType(apiPurchaseItemDto);
                    for(int j=0;j<itemList.size();j++){
                        apiPurchaseItemDto.setApiPurchaseItemProcutId(null);
                        apiPurchaseItemDto.setApiPurchaseItemDescription(null);
                        apiPurchaseItemDto.setApiPurchaseItemSelectType(null);
                        apiPurchaseItemDto.setApiPurchaseItemDays(null);
                        if(itemList.get(j).get("wmproductId")!=null && !itemList.get(j).get("wmproductId").equals("")){
                            String wmproductId = itemList.get(j).get("wmproductId").toString();
                            String wmproductIds[] = wmproductId.split("-");
                            apiPurchaseItemDto.setApiPurchaseItemProcutId(wmproductId);
                            if(itemList.get(j).get("productName")!=null)apiPurchaseItemDto.setApiPurchaseItemDescription(itemList.get(j).get("productName").toString());
                            if(itemList.get(j).get("productSelectType")!=null)apiPurchaseItemDto.setApiPurchaseItemSelectType(itemList.get(j).get("productSelectType").toString());
                            if(itemList.get(j).get("day")!=null)apiPurchaseItemDto.setApiPurchaseItemDays(itemList.get(j).get("day").toString());

                            //월드무브 충전 불가
                            apiPurchaseItemDto.setApiPurchaseIsCharge(false);




                            try{
                                if(wmproductIds.length>4){
                                    String totalString = wmproductIds[wmproductIds.length-2];
                                    boolean dailFlag = totalString.indexOf("T")==-1;
                                    apiPurchaseItemDto.setApiPurchaseItemIsDaily(dailFlag);
                                }else{
                                    apiPurchaseItemDto.setApiPurchaseItemIsDaily(false);
                                }
                            }catch (Exception e){

                            }


                            apiPurchaseItemDto.setApiPurchaseCurrency("NT"); //WORLDMOVE 단위 존재안함. 무조건 $임


                            if(itemList.get(j).get("productPrice")!=null){
                                apiPurchaseItemDto.setApiPurchasePrice(itemList.get(j).get("productPrice").toString());
                                if(echangeRate!=null){
                                    double krwPrice = echangeRate *Double.parseDouble(itemList.get(j).get("productPrice").toString());
                                    apiPurchaseItemDto.setApiPurchaseKrwPrice(krwPrice+"");
                                }
                            }


                            if(itemList.get(j).get("productRegion")!=null)apiPurchaseItemDto.setApiPurchaseCoverDomainCode(itemList.get(j).get("productRegion").toString());

                        }
                        try{
                            apiPurchaseItemService.insert(apiPurchaseItemDto);

                        } catch (Exception e) {
                            System.out.println("중복아이템" + apiPurchaseItemDto.getApiPurchaseItemProcutId());
                        }
                    }
                }catch (Exception e){
                }

                try{
                    EsimPriceDto param = new EsimPriceDto();
                    param.setType(ApiType.TSIM.name());
                    EsimPriceDto esimPriceDto = esimPriceService.findById(param);
                    Double echangeRate = esimPriceDto.getExchangeRate() * esimPriceDto.getExchangeWeight();
                    ApiPurchaseItemDto apiPurchaseItemDto = new ApiPurchaseItemDto();
                    apiPurchaseItemDto.setApiPurchaseItemType(ApiType.TSIM.name());
                    List<HashMap<String, Object>> itemList = OriginTsimUtil.contextLoads1();
                    apiPurchaseItemService.deleteWithApiPurchaseItemType(apiPurchaseItemDto);
                    for(int j=0;j<itemList.size();j++){
                        apiPurchaseItemDto.setApiPurchaseItemProcutId(null);
                        apiPurchaseItemDto.setApiPurchaseItemDescription(null);
                        apiPurchaseItemDto.setApiPurchaseItemSelectType(null);
                        apiPurchaseItemDto.setApiPurchaseItemDays(null);
                        if(itemList.get(j).get("channel_dataplan_id")!=null && !itemList.get(j).get("channel_dataplan_id").equals("")){
                            apiPurchaseItemDto.setApiPurchaseItemProcutId(itemList.get(j).get("channel_dataplan_id").toString());
                            if(itemList.get(j).get("channel_dataplan_name")!=null)apiPurchaseItemDto.setApiPurchaseItemDescription(itemList.get(j).get("channel_dataplan_name").toString());
                            if(itemList.get(j).get("productSelectType")!=null)apiPurchaseItemDto.setApiPurchaseItemSelectType(itemList.get(j).get("productSelectType").toString());
                            if(itemList.get(j).get("day")!=null)apiPurchaseItemDto.setApiPurchaseItemDays(itemList.get(j).get("day").toString());

                            if(itemList.get(j).get("price")!=null){
                                apiPurchaseItemDto.setApiPurchasePrice(itemList.get(j).get("price").toString());
                                if(echangeRate!=null){
                                    double krwPrice = echangeRate *Double.parseDouble(itemList.get(j).get("price").toString());
                                    apiPurchaseItemDto.setApiPurchaseKrwPrice(krwPrice+"");
                                }
                            }
                            if(itemList.get(j).get("currency")!=null)apiPurchaseItemDto.setApiPurchaseCurrency(itemList.get(j).get("currency").toString());
                            if(itemList.get(j).get("cover_domain_code")!=null)apiPurchaseItemDto.setApiPurchaseCoverDomainCode(itemList.get(j).get("cover_domain_code").toString());
                            if(itemList.get(j).get("is_daily")!=null)apiPurchaseItemDto.setApiPurchaseItemIsDaily((Boolean) itemList.get(j).get("is_daily"));
                            if(itemList.get(j).get("apn")!=null){
                                String apn = itemList.get(j).get("apn").toString();
                                apiPurchaseItemDto.setApiPurchaseApn(apn);
                                Integer topupSupport = 0;
                                if(itemList.get(j).get("topup_support")!=null){
                                    topupSupport = Integer.parseInt(itemList.get(j).get("topup_support").toString());
                                }

                                if(topupSupport==1 && !apiPurchaseItemDto.isApiPurchaseItemIsDaily() && (apn.toLowerCase().equals("e-ideas") || apn.toLowerCase().equals("plus") || apn.toLowerCase().equals("ctm-mobile"))){
                                    apiPurchaseItemDto.setApiPurchaseIsCharge(true);
                                }else {
                                    apiPurchaseItemDto.setApiPurchaseIsCharge(false);
                                }
                            }else{
                                apiPurchaseItemDto.setApiPurchaseIsCharge(false);
                            }


                            //TODO 충전기능 오픈전까지 충전불가 아래삭제
                            apiPurchaseItemDto.setApiPurchaseIsCharge(false);

                            String formattedAllowance = ""; // 최종 출력될 문자열
                            if(apiPurchaseItemDto.isApiPurchaseItemIsDaily()){
                                if(itemList.get(j).get("day_data_allowance") != null){
                                    int mbValue = Integer.parseInt(itemList.get(j).get("day_data_allowance").toString());
                                    formattedAllowance = formatDataSize(mbValue);
                                }
                            } else {
                                if(itemList.get(j).get("data_allowance") != null){
                                    int mbValue = Integer.parseInt(itemList.get(j).get("data_allowance").toString());
                                    formattedAllowance = formatDataSize(mbValue);
                                }
                            }
                            apiPurchaseItemDto.setApiPurchaseDataTotal(formattedAllowance);


                        }
                        apiPurchaseItemService.insert(apiPurchaseItemDto);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                try{
                    ApiPurchaseItemDto apiPurchaseItemDto = new ApiPurchaseItemDto();
                    apiPurchaseItemDto.setApiPurchaseItemType(ApiType.ESIMACCESS.name());
                    List<Map<String,Object>> itemList  = OriginEsimAccessUtil.contextLoads1();

                    EsimPriceDto param = new EsimPriceDto();
                    param.setType(ApiType.ESIMACCESS.name());
                    EsimPriceDto esimPriceDto = esimPriceService.findById(param);
                    Double echangeRate = esimPriceDto.getExchangeRate() * esimPriceDto.getExchangeWeight();

                    apiPurchaseItemService.deleteWithApiPurchaseItemType(apiPurchaseItemDto);

                    for(int j=0;j<itemList.size();j++){
                        Map<String,Object> item = itemList.get(j);

                        int activeType  = (int) item.get("activeType");//언제부터 요금제 카운트 시작 1. 휴대폰설치시점 2. 최초네트워크접속시점
                        if(activeType!=2){ // 최초네트워크접속시점만 판매함
                            continue;
                        }
                        apiPurchaseItemDto.setApiPurchaseExportDomainCode(item.get("ipExport").toString());
                        apiPurchaseItemDto.setApiPurchaseItemProcutId(item.get("packageCode").toString());
                        apiPurchaseItemDto.setApiPurchaseItemDescription(item.get("name").toString());
                        double price = (Double.parseDouble(item.get("price").toString())/10000);
                        apiPurchaseItemDto.setApiPurchasePrice(price+"");//가격
                        if(echangeRate!=null){
                            double krwPrice = echangeRate *price;
                            apiPurchaseItemDto.setApiPurchaseKrwPrice(krwPrice+"");
                        }
                        apiPurchaseItemDto.setApiPurchaseCurrency(item.get("currencyCode").toString());//화폐단위
                        apiPurchaseItemDto.setApiPurchaseDataTotal(item.get("volume").toString());//용량
                        apiPurchaseItemDto.setApiPurchaseDataUnit("bytes"); //용량단위

                        int dataType  = (int) item.get("dataType");
                        apiPurchaseItemDto.setApiPurchaseItemSelectType(dataType+"");//1:총량제, 2:일일제한(속도제어), 3:일일제한(차단) 4:일일무제한
                        apiPurchaseItemDto.setApiPurchaseItemIsDaily(dataType==2 || dataType == 3  || dataType == 4);

                        int unusedValidTime  = (int) item.get("unusedValidTime");
                        apiPurchaseItemDto.setApiPurchaseUnusedValidTime(unusedValidTime+"");

                        apiPurchaseItemDto.setApiPurchaseItemDays(((Integer) item.get("duration")) +"");//활성화후 유효기간 (일)

                        apiPurchaseItemDto.setApiPurchaseCoverDomainCode(item.get("location").toString());

                        apiPurchaseItemDto.setApiPurchaseNormalSpeed(item.get("speed").toString());

                        apiPurchaseItemDto.setApiPurchaseSlowSpeed(item.get("fupPolicy").toString());
                        if((Integer) item.get("supportTopUpType")==2){
                            apiPurchaseItemDto.setApiPurchaseIsCharge(true);
                        }else{
                            apiPurchaseItemDto.setApiPurchaseIsCharge(false);

                        }


                        apiPurchaseItemService.insert(apiPurchaseItemDto);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }




    }
    // --- 변환 메서드 (클래스 내부에 추가) ---
    public String formatDataSize(int mb) {
        if (mb >= 1024) {
            int gb = mb / 1024; // 소수점 없이 깔끔하게 나누기 위해 int 사용
            return gb + "GB";
        } else {
            return mb + "MB";
        }
    }

    @Test
    void doPriceChange () {
        Map<String,Object> param = new HashMap<>();
        param.put("userAuthority","ROLE_ADMIN");
        List<StoreDto> storeDtos = storeService.selectStoreList(param);
        //모든옵션
        for(StoreDto storeDto:storeDtos){
            List<ProductDto> productDtoList = productService.selectProductList(storeDto);

            for(ProductDto productDto:productDtoList){
                Map<String, Object> bodyMap = new HashMap<>();
                Map<String, Object> salePrice = new HashMap<>();
                salePrice.put("salePrice",productDto.getSalePrice());
                bodyMap.put("productSalePrice",salePrice);
                MatchInfoDto matchInfoParam = new MatchInfoDto();
                matchInfoParam.setOriginProductNo(productDto.getOriginProductNo());
                List<MatchInfoDto> matchInfoDtoList = matchInfoService.selectMatchInfoListAll(matchInfoParam);
                List<Map<String,Object>> optionCombinations = new ArrayList<>();
                for(MatchInfoDto matchInfoDto:matchInfoDtoList){
                    Map<String,Object> optionBodyMap = new HashMap<>();

                    ApiPurchaseItemDto apiPurchaseItemParam = new ApiPurchaseItemDto();
                    apiPurchaseItemParam.setApiPurchaseItemProcutId(matchInfoDto.getEsimProductId());
                    if(matchInfoDto.getProductDto()!=null && matchInfoDto.getProductOptionDto()!=null){
                        double currentOptionPrice = Double.parseDouble(matchInfoDto.getProductOptionDto().getPrice().toString());
                        if(currentOptionPrice==0){ //옵션가가 0원이면 최초등록이라 관리자사이트에서 직접수정 0원이면 프로덕트가격임
                            continue;
                        }

                        if(matchInfoDto.getEsimType().equals("06")){
                            apiPurchaseItemParam.setApiPurchaseItemType(ApiType.WORLDMOVE.name());
                        }else if(matchInfoDto.getEsimType().equals("03")){
                            apiPurchaseItemParam.setApiPurchaseItemType(ApiType.TSIM.name());
                        }else if(matchInfoDto.getEsimType().equals("05")){
                            apiPurchaseItemParam.setApiPurchaseItemType(ApiType.TUGE.name());
                        }else if(matchInfoDto.getEsimType().equals("07")){
                            apiPurchaseItemParam.setApiPurchaseItemType(ApiType.ESIMACCESS.name());
                        }


                        if (apiPurchaseItemParam.getApiPurchaseItemType() != null) {
                            ApiPurchaseItemDto apiPurchaseItemDto = apiPurchaseItemService.selectApiPurchaseItemWithApiPurchaseItemTypeAndApiPurchaseItemProcutId(apiPurchaseItemParam);

                            if(apiPurchaseItemDto.getApiPurchaseKrwPrice()!=null){
                                double apiPrice = getRealSamePrice(apiPurchaseItemDto);

                                if(matchInfoDto.getEsimType().equals("07")){
                                    apiPrice = getEsimAccessRealPrice(apiPurchaseItemDto,matchInfoDto.getEsimProductDays());
                                }


                                //한국가격에서 세일가격을 뺴야 옵션가랑 비교가능
                                apiPrice = apiPrice-matchInfoDto.getProductDto().getSalePrice();

                                if (Math.abs(apiPrice - currentOptionPrice) > 10) {
                                    // 두 가격의 차이(양수)가 100원보다 크면 실행 바꿔야함. 가격을 api 가격으로
                                    optionBodyMap.put("id",matchInfoDto.getProductOptionDto().getOptionId());
                                    optionBodyMap.put("stockQuantity",10000);
                                    optionBodyMap.put("price",apiPrice);
                                    optionCombinations.add(optionBodyMap);

                                }

                            }else{
                                //없는경우 품절시킬까? 서버장에로 데이터 못받아온 경우도 있자나..그냥두자
                            }
                        }
                    }



                }
                if(optionCombinations.size()>0){
                    Map<String, Object> optionInfo = new HashMap<>();
                    optionInfo.put("optionCombinations",optionCombinations);
                    bodyMap.put("optionInfo",optionInfo);
                    processNaverStore(productDto.getOriginProductNo(),bodyMap,storeDto);
                }
            }
        }
    }


    @Test
    void esimAccessTest () throws Exception {
        ApiPurchaseItemDto apiPurchaseItemDto = new ApiPurchaseItemDto();
        apiPurchaseItemDto.setApiPurchaseItemType(ApiType.ESIMACCESS.name());
        List<Map<String,Object>> itemList  = OriginEsimAccessUtil.contextLoads1();

        EsimPriceDto param = new EsimPriceDto();
        param.setType(ApiType.ESIMACCESS.name());
        EsimPriceDto esimPriceDto = esimPriceService.findById(param);
        Double echangeRate = esimPriceDto.getExchangeRate() * esimPriceDto.getExchangeWeight();

        apiPurchaseItemService.deleteWithApiPurchaseItemType(apiPurchaseItemDto);

        for(int j=0;j<itemList.size();j++){
            Map<String,Object> item = itemList.get(j);

            int activeType  = (int) item.get("activeType");//언제부터 요금제 카운트 시작 1. 휴대폰설치시점 2. 최초네트워크접속시점
            if(activeType!=2){ // 최초네트워크접속시점만 판매함
                continue;
            }
            apiPurchaseItemDto.setApiPurchaseExportDomainCode(item.get("ipExport").toString());
            apiPurchaseItemDto.setApiPurchaseItemProcutId(item.get("packageCode").toString());
            apiPurchaseItemDto.setApiPurchaseItemDescription(item.get("name").toString());
            double price = (Double.parseDouble(item.get("price").toString())/10000);
            apiPurchaseItemDto.setApiPurchasePrice(price+"");//가격
            if(echangeRate!=null){
                double krwPrice = echangeRate *price;
                apiPurchaseItemDto.setApiPurchaseKrwPrice(krwPrice+"");
            }
            apiPurchaseItemDto.setApiPurchaseCurrency(item.get("currencyCode").toString());//화폐단위
            apiPurchaseItemDto.setApiPurchaseDataTotal(item.get("volume").toString());//용량
            apiPurchaseItemDto.setApiPurchaseDataUnit("bytes"); //용량단위

            int dataType  = (int) item.get("dataType");
            apiPurchaseItemDto.setApiPurchaseItemSelectType(dataType+"");//1:총량제, 2:일일제한(속도제어), 3:일일제한(차단) 4:일일무제한
            apiPurchaseItemDto.setApiPurchaseItemIsDaily(dataType==2 || dataType == 3  || dataType == 4);

            int unusedValidTime  = (int) item.get("unusedValidTime");
            apiPurchaseItemDto.setApiPurchaseUnusedValidTime(unusedValidTime+"");

            apiPurchaseItemDto.setApiPurchaseItemDays(((Integer) item.get("duration")) +"");//활성화후 유효기간 (일)

            apiPurchaseItemDto.setApiPurchaseCoverDomainCode(item.get("location").toString());

            apiPurchaseItemDto.setApiPurchaseNormalSpeed(item.get("speed").toString());

            apiPurchaseItemDto.setApiPurchaseSlowSpeed(item.get("fupPolicy").toString());
            if((Integer) item.get("supportTopUpType")==2){
                apiPurchaseItemDto.setApiPurchaseIsCharge(true);
            }else{
                apiPurchaseItemDto.setApiPurchaseIsCharge(false);

            }


            apiPurchaseItemService.insert(apiPurchaseItemDto);
        }


    }

    @Test
    void esimAccessPurchase () throws Exception {
        StoreDto storeDto = new StoreDto();
        EsimAccessUtil esimAccess  =  EsimUtil.getEsimAccess(storeDto);
        //Brazil 500MB/Day
        HashMap esimMap = esimAccess.contextLoads2( "P74QTP3AR", "365",12312L); //이심 요청
        System.out.println(esimMap);


    }
    @Test
    void esimAccessPurchaseStatus () throws Exception {
        StoreDto storeDto = new StoreDto();
        EsimAccessUtil esimAccess  =  EsimUtil.getEsimAccess(storeDto);
        //Brazil 500MB/Day
        Map<String,Object> result =  esimAccess.contextLoads3("B23051616050537", 12312L);
        System.out.println(result);


    }

    public double getRealSamePrice(ApiPurchaseItemDto apiPurchaseItemDto){

        EsimPriceDto param = new EsimPriceDto();
        param.setType(apiPurchaseItemDto.getApiPurchaseItemType());
        EsimPriceDto esimPriceDto = esimPriceService.findById(param);
        Double weight1 = esimPriceDto.getWeight1();
        Double weight2 = esimPriceDto.getWeight2();
        Double weight3 = esimPriceDto.getWeight3();
        Double weight4 = esimPriceDto.getWeight4();
        Double weight5 = esimPriceDto.getWeight5();

        double apiPrice =  Double.parseDouble(apiPurchaseItemDto.getApiPurchaseKrwPrice());
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
        double daysWeight = 0;
        //여기서 알고리즘 실행

        boolean dailyFlag = apiPurchaseItemDto.isApiPurchaseItemIsDaily();

        if(!dailyFlag){
            //총량일경우 가격 가중치로
            daysWeight = priceWeight;
        }else{
            //데일리가없을경우 그냥 가격가중치로
            try{
                int daily = Integer.parseInt(apiPurchaseItemDto.getApiPurchaseItemDays().toString());
                if(daily<=5)
                    daysWeight = weight1;
                else if(daily<=10)
                    daysWeight = weight2;
                else if(daily<=15)
                    daysWeight = weight3;
                else if(daily<=20)
                    daysWeight = weight4;
                else
                    daysWeight = weight5;
            }catch (Exception e){
                daysWeight = priceWeight;

            }
        }
        if(priceWeight<daysWeight){
            return Math.round(apiPrice * priceWeight / 100.0) * 100;
        }else{
            return Math.round(apiPrice * daysWeight / 100.0) * 100;
        }
    }


    private double getEsimAccessRealPrice(ApiPurchaseItemDto apiPurchaseItemDto, String esimProductDays) {

        EsimPriceDto param = new EsimPriceDto();
        param.setType(apiPurchaseItemDto.getApiPurchaseItemType());
        EsimPriceDto esimPriceDto = esimPriceService.findById(param);
        Double weight1 = esimPriceDto.getWeight1();
        Double weight2 = esimPriceDto.getWeight2();
        Double weight3 = esimPriceDto.getWeight3();
        Double weight4 = esimPriceDto.getWeight4();
        Double weight5 = esimPriceDto.getWeight5();

        double apiPrice =  Double.parseDouble(apiPurchaseItemDto.getApiPurchaseKrwPrice());


        //esimaccess에서 제공하는 할인율에 따른 가격 계산
        //=IFS(C47<= 1, 0.96,  C47<= 2, 0.95,  C47<= 3, 0.94,  C47<= 4, 0.93,
        // C47 <= 5,0.92,C47 <= 6,0.91,
        // C47 <= 7,0.9,C47 <= 8,0.89,
        // C47 <= 9,0.89,
        // C47<=11,0.89,C47<=13,0.88
        // ,C47<=15,0.87
        // ,C47<=17,0.86,C47<=19,0.85,
        // C47<=21,0.85,
        // C47<=23,0.84
        // ,C47<=25,0.83,
        // C47<=27,0.82,C47<=29,0.82, TRUE, 0.82)
        try{
            int daily = Integer.parseInt(esimProductDays);
            if(daily<=1)
                apiPrice = apiPrice * 0.96;
            else if(daily<=2)
                apiPrice = apiPrice * 0.95;
            else if(daily<=3)
                apiPrice = apiPrice * 0.94;
            else if(daily<=4)
                apiPrice = apiPrice * 0.93;
            else if(daily<=5)
                apiPrice = apiPrice * 0.92;
            else if(daily<=6)
                apiPrice = apiPrice * 0.91;
            else if(daily<=7)
                apiPrice = apiPrice * 0.9;
            else if(daily<=11)
                apiPrice = apiPrice * 0.89;
            else if(daily<=13)
                apiPrice = apiPrice * 0.88;
            else if(daily<=15)
                apiPrice = apiPrice * 0.87;
            else if(daily<=17)
                apiPrice = apiPrice * 0.86;
            else if(daily<=21)
                apiPrice = apiPrice * 0.85;
            else if(daily<=23)
                apiPrice = apiPrice * 0.84;
            else if(daily<=25)
                apiPrice = apiPrice * 0.83;
            else
                apiPrice = apiPrice * 0.82;

            apiPrice = apiPrice * daily;

        }catch (Exception e){

        }

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
        double daysWeight = 0;
        //여기서 알고리즘 실행

        boolean dailyFlag = apiPurchaseItemDto.isApiPurchaseItemIsDaily();

        if(!dailyFlag){
            //총량일경우 가격 가중치로
            daysWeight = priceWeight;
        }else{
            //데일리가없을경우 그냥 가격가중치로
            try{
                int daily = Integer.parseInt(esimProductDays);
                if(daily<=9)
                    daysWeight = weight1;
                else if(daily<=19)
                    daysWeight = weight2;
                else if(daily<=25)
                    daysWeight = weight3;
                else if(daily<=25)
                    daysWeight = weight4;
                else
                    daysWeight = weight5;

            }catch (Exception e){
                daysWeight = priceWeight;

            }
        }
        if(priceWeight<daysWeight){
            return Math.round(apiPrice * priceWeight / 100.0) * 100;
        }else{
            return Math.round(apiPrice * daysWeight / 100.0) * 100;
        }

    }


    public void processNaverStore(Long originProductNo, Map<String,Object> bodyMap,StoreDto storeDto) {
        try {
            // 인증정보 redis 저장
            naverSetting.setting(null);
            ObjectMapper objectMapper = new ObjectMapper();
            Optional<NaverRedisToken> byStoreId = naverRedisRepository.findById(storeDto.getId());
            if (!byStoreId.isPresent()){
                return;
            }

            requestOptionInfoChange(originProductNo,bodyMap,byStoreId.get());



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Map<String, Object> requestOptionInfoChange(Long originProductNo,Map<String, Object> bodyMap, NaverRedisToken naverRedisToken) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();


        Map<String, String> headerMap = naverRedisToken.returnHeaderMap();
        Map<String, Object> pathParameter = new HashMap<>();
        pathParameter.put("originProductNo",originProductNo);



        String res = ApiUtil.put(baseUrl+productOtionInfoChangeUrl, headerMap, bodyMap,pathParameter,MediaType.parse("application/json; charset=UTF-8"));

        return result;
    }

    public Map<String,Double> getExchangeRate() {
        Map<String,Double> result = new HashMap<>();
        // ExchangeRate-API 사용 예시 (무료 키 발급 필요)
        String url = exchangeApiUrl + exchangeApiKey + "/latest/KRW";

        RestTemplate restTemplate = new RestTemplate();

        try {
            // API 호출 및 응답을 Map으로 받기
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("conversion_rates")) {
                Map<String, Double> rates = (Map<String, Double>) body.get("conversion_rates");

                double usdRate = rates.get("USD");
                double twdRate = rates.get("TWD");

                System.out.println("====== 현재 환율 (1원 기준) ======");
                System.out.println("USD: " + usdRate);
                System.out.println("TWD: " + twdRate);

                System.out.println("====== 원화 환산 (1단위 기준) ======");
                System.out.println("1달러당: " + ((1 / usdRate)* 1.01) + "원");
                System.out.println("1대만달러당: " + ((1 / twdRate)* 1.01) + "원");
                //1.01 더하는 이유는 환율방어 갑자기올라도 방어됨. 1%

                result.put("TWD", ((1 / twdRate)* 1.01));
                result.put("USD", ((1 / usdRate)* 1.01));
            }
        } catch (Exception e) {
            System.out.println("환율 정보를 가져오는 중 오류 발생: " + e.getMessage());
        }

        return  result;
    }
}