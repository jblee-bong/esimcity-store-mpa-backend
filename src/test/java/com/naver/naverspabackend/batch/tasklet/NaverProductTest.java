package com.naver.naverspabackend.batch.tasklet;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.batch.writer.NaverWritter;
import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.ProductOptionDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.ProductMapper;
import com.naver.naverspabackend.mybatis.mapper.ProductOptionMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.SignatureGenerator;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.util.ApiUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest
class NaverProductTest {

    @Value("${naver-api.base}")
    private String baseUrl;

    @Value("${naver-api.product-search}")
    private String productSearchUrl;

    @Value("${naver-api.origin-product-info}")
    private String productInfoUrl;

    @Autowired
    private NaverRedisRepository naverRedisRepository;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductOptionMapper productOptionMapper;

    @Value("${naver-api.token}")
    private String tokenUrl;

    @Autowired
    private NaverWritter naverWritter;


    @Autowired
    private NaverSetting naverSetting;

    /**
     * 전자 서명 및 인증 토큰 발급 요청
     * @return
     * @throws Exception
     */
    @Test
    @Order(1)
    public void setting() throws Exception {
        Map<String , Object> returnMap = new HashMap<>();
        Map<String,Object> param = new HashMap<>();
        param.put("userAuthority","ROLE_ADMIN");
        List<StoreDto> storeDtoList = storeMapper.selectStoreList(param);

        for (StoreDto storeDto : storeDtoList) {
            try {
                Long storeId = storeDto.getId();

                Optional<NaverRedisToken> byStoreId = naverRedisRepository.findById(storeId);

                if(byStoreId.isPresent()){
                    NaverRedisToken naverRedisToken = byStoreId.orElse(null);

                    // mills
                    Long timeStamp = naverRedisToken.getTimeStamp();

                    Long expiresIn = naverRedisToken.getExpiresIn() * 1000;

                    Date now = new Date();

                    long time = now.getTime();

                    // 인증시간 만료
                    if((timeStamp + expiresIn) < time){
                        requestOauth2Token(storeDto, requestSecretSign(storeDto));
                    }
                }else{
                    requestOauth2Token(storeDto, requestSecretSign(storeDto));
                }
            } catch (Exception e) {
                e.printStackTrace();
                requestOauth2Token(storeDto, requestSecretSign(storeDto));
            }
        }

    }

    /**
     * 전자서명 키 요청
     * @return
     */
    public NaverRedisToken requestSecretSign(StoreDto storeDto){

        String secretSign = "";

        String clientId = storeDto.getClientId();
        String clientSecret = storeDto.getClientSecret();

        Date now = new Date();

        Long timeStamp = now.getTime();

        String signature = SignatureGenerator.generateSignature(clientId, clientSecret, timeStamp);

        NaverRedisToken naverRedisToken = new NaverRedisToken();

        naverRedisToken.setStoreId(storeDto.getId());
        naverRedisToken.setSecretSign(signature);
        naverRedisToken.setTimeStamp(timeStamp);

        NaverRedisToken save = naverRedisRepository.save(naverRedisToken);

        return save;
    }

    public NaverRedisToken requestOauth2Token(StoreDto storeDto, NaverRedisToken naverRedisToken) throws IOException {

        Map<String, Object> bodyMap = new HashMap<>();

        bodyMap.put("client_id", storeDto.getClientId());
        bodyMap.put("client_secret_sign", naverRedisToken.getSecretSign());
        bodyMap.put("timestamp", naverRedisToken.getTimeStamp());
        bodyMap.put("grant_type", "client_credentials");
        bodyMap.put("type", "SELF");

        String res = ApiUtil.postWithQueryParam(baseUrl + tokenUrl, bodyMap, null, false);

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> resMap = objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});

        String accessToken = Objects.toString(resMap.get("access_token"), "");
        String expiresIn = Objects.toString(resMap.get("expires_in"), "0");
        String tokenType = Objects.toString(resMap.get("token_type"), "");

        naverRedisToken.setOauthToken(accessToken);
        naverRedisToken.setExpiresIn(Long.parseLong(expiresIn));
        naverRedisToken.setTokenType(tokenType);

        NaverRedisToken save = naverRedisRepository.save(naverRedisToken);

        return save;
    }

    public StoreDto buildStoreDto(){
        StoreDto storeDto = new StoreDto();
        storeDto.setId(1L);
        storeDto.setClientId("test");
        storeDto.setClientSecret("test");
        return storeDto;
    }


    @Test
    @Transactional(rollbackFor = Exception.class)
    @Order(2)
    @Rollback(false)
    public void requestProduct() throws Exception {


        naverSetting.setting("1");
        Map<String,Object> param = new HashMap<>();
        param.put("userAuthority","ROLE_ADMIN");
        List<StoreDto> storeDtos = storeMapper.selectStoreList(param);
        ObjectMapper objectMapper = new ObjectMapper();

        for (StoreDto storeDto : storeDtos) {
            Optional<NaverRedisToken> byStoreId = naverRedisRepository.findById(storeDto.getId());

            if (!byStoreId.isPresent()){
                return;
            }
            if(storeDto.getId()!=6){
                continue;
            }

            NaverRedisToken naverRedisToken = byStoreId.get();

            Map<String, String> headerMap = naverRedisToken.returnHeaderMap();

            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("productStatusTypes", "SALE");
            paramMap.put("orderType", "REG_DATE");

            Map<String, Object> resMap = new HashMap<>();

            try {
                String res = ApiUtil.post(baseUrl + productSearchUrl, headerMap, paramMap, null, MediaType.parse("application/json; charset=UTF-8"));
                resMap = objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});
            }catch (Exception e){
                continue;
            }

            // 최종 입력 될 product 상품
            List<ProductDto> resultProductDtoList = new ArrayList<>();

            // 최종 입력 될 productOption 상품
            List<ProductOptionDto> resultProductOptionDtoList = new ArrayList<>();

            List<Map<String, Object>> contentsList = (List<Map<String, Object>>) resMap.get("contents");

            Set<Long> originProductNoList = new HashSet<>();

            try {
                originProductNoList = contentsList.stream().map(e -> (Long) e.get("originProductNo")).collect(Collectors.toSet());
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<ProductDto> originProductList = new ArrayList<>();
            List<ProductOptionDto> originProductOptionList = new ArrayList<>();
            Map<Long, ProductDto> originProductListToMap = new HashMap<>();
            Map<Long, ProductOptionDto> originProductOptionListToMap = new HashMap<>();

            for (Long i : originProductNoList) {
                Map<String, Object> productInfo = requestProductInfo(i, naverRedisToken);
                ProductDto e = buildProductDto(productInfo, i, storeDto);
                originProductList.add(e);
                originProductOptionList.addAll(buildProductOption(productInfo, e, storeDto));

                Thread.sleep(400);
            }

            if(!originProductList.isEmpty()){
                originProductListToMap = originProductList.stream().collect(Collectors.toMap(
                    (e -> e.getOriginProductNo()), Function.identity()
                ));
            }

            if(!originProductOptionList.isEmpty()){
                originProductOptionListToMap = originProductOptionList.stream().collect(Collectors.toMap(
                    (e -> e.getOptionId()), Function.identity()
                ));
            }

            List<ProductDto> localOriginProductList = productMapper.selectProductList(storeDto);

            Map<Long, ProductDto> localOriginProductListToMap = new HashMap<>();

            if(localOriginProductList != null && !localOriginProductList.isEmpty()){
                localOriginProductListToMap = localOriginProductList.stream().collect(Collectors.toMap(
                    (e -> e.getOriginProductNo()), Function.identity()
                ));
            }

            // 가져온 product 와 비교 local 과 비교하여 없다면 삭제 local key 가 product에 존재하지 않는다면 삭제, 존재하지만 데이터가 다르다면 삭제
            for (Long key : localOriginProductListToMap.keySet()) {
                ProductDto localProduct = localOriginProductListToMap.get(key);
                if(originProductListToMap.get(key) == null){
                    localProduct.setDeleteFlag(true);
                    resultProductDtoList.add(localProduct);
                }else if(originProductListToMap.get(key) != null){
                    ProductDto originProduct = originProductListToMap.get(key);

                    // 업데이트 할 사항이 있다면
                    if(!localProduct.compare(originProduct)){
                        localProduct.setProductName(originProduct.getProductName());
                        localProduct.setStatusType(originProduct.getStatusType());
                        localProduct.setRepresentativeImageUrl(originProduct.getRepresentativeImageUrl());
                        localProduct.setOptionGroupName1(originProduct.getOptionGroupName1());
                        localProduct.setOptionGroupName2(originProduct.getOptionGroupName2());
                        localProduct.setOptionGroupName3(originProduct.getOptionGroupName3());
                        localProduct.setOptionGroupName4(originProduct.getOptionGroupName4());
                        localProduct.setSalePrice(originProduct.getSalePrice());
                        localProduct.setUpdateFlag(true);
                        resultProductDtoList.add(localProduct);
                    }
                }
            }

            // 가져온 product 비교 local 과 비교하여 없다면 insert
            for (Long key : originProductListToMap.keySet()) {
                ProductDto originProduct = originProductListToMap.get(key);
                if(localOriginProductListToMap.get(key) == null){
                    originProduct.setInsertFlag(true);
                    resultProductDtoList.add(originProduct);
                }
            }

            List<ProductOptionDto> localOriginProductOptionList = productOptionMapper.selectProductOptionList(storeDto);

            Map<Long, ProductOptionDto> localOriginProductOptionListToMap = new HashMap<>();



            if(localOriginProductOptionList != null && !localOriginProductOptionList.isEmpty()){
                localOriginProductOptionListToMap = localOriginProductOptionList.stream().collect(Collectors.toMap(
                    (e -> e.getOptionId()), Function.identity()
                ));
            }

            // 가져온 productOption 와 비교 local 과 비교하여 없다면 삭제 local key 가 productOption에 존재하지 않는다면 삭제, 존재하지만 데이터가 다르다면 삭제
            for (Long key : localOriginProductOptionListToMap.keySet()) {
                ProductOptionDto localProductOption = localOriginProductOptionListToMap.get(key);
                if(originProductOptionListToMap.get(key) == null){
                    localProductOption.setDeleteFlag(true);
                    resultProductOptionDtoList.add(localProductOption);
                }else if(originProductOptionListToMap.get(key) != null){
                    ProductOptionDto originProductOption = originProductOptionListToMap.get(key);

                    // 업데이트 할 사항이 있다면
                    if(!localProductOption.compare(originProductOption)){
                        localProductOption.setOptionName1(originProductOption.getOptionName1());
                        localProductOption.setOptionName2(originProductOption.getOptionName2());
                        localProductOption.setOptionName3(originProductOption.getOptionName3());
                        localProductOption.setOptionName4(originProductOption.getOptionName4());
                        localProductOption.setPrice(originProductOption.getPrice());
                        localProductOption.setUsable(originProductOption.getUsable());
                        localProductOption.setUpdateFlag(true);
                        resultProductOptionDtoList.add(localProductOption);
                    }
                }
            }

            // 가져온 productOption 비교 local 과 비교하여 없다면 insert
            for (Long key : originProductOptionListToMap.keySet()) {
                ProductOptionDto originProductOption = originProductOptionListToMap.get(key);
                if(localOriginProductOptionListToMap.get(key) == null){
                    originProductOption.setInsertFlag(true);
                    resultProductOptionDtoList.add(originProductOption);
                }
            }

            naverWritter.productWrite(resultProductDtoList, resultProductOptionDtoList);

        }
    }

    public Map<String, Object> requestProductInfo(Long originProductNo, NaverRedisToken naverRedisToken) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + productInfoUrl);
        UriComponents uriComponents = builder.buildAndExpand(originProductNo + "");

        String s = ApiUtil.get(uriComponents.toUriString(), naverRedisToken.returnHeaderMap());

        result = objectMapper.readValue(s, new TypeReference<Map<String, Object>>() {});

        return result;
    }

    public ProductDto buildProductDto(Map<String, Object> map, Long originProductNo, StoreDto storeDto){
        Map<String, Object> originProduct = (Map<String, Object>) map.get("originProduct");
        String productName = Objects.toString(originProduct.get("name"), "");
        String statusType = Objects.toString(originProduct.get("statusType"), "");
        Integer salePrice = (Integer) originProduct.get("salePrice");
        String representativeImageUrl = Objects.toString(( (Map) ((Map)originProduct.get("images")).get("representativeImage")).get("url"), "");

        ProductDto productDto = new ProductDto();
        productDto.setOriginProductNo(originProductNo);
        productDto.setStoreId(storeDto.getId());
        productDto.setProductName(productName);
        productDto.setStatusType(statusType);
        productDto.setSalePrice(salePrice);
        productDto.setRepresentativeImageUrl(representativeImageUrl);

        try {
            Map optionGroupNameMap = (Map) ((Map) ((Map) originProduct.get("detailAttribute")).get("optionInfo")).get("optionCombinationGroupNames");

            productDto.setOptionGroupName1(Objects.toString(optionGroupNameMap.get("optionGroupName1"), ""));
            productDto.setOptionGroupName2(Objects.toString(optionGroupNameMap.get("optionGroupName2"), ""));
            productDto.setOptionGroupName3(Objects.toString(optionGroupNameMap.get("optionGroupName3"), ""));
            productDto.setOptionGroupName4(Objects.toString(optionGroupNameMap.get("optionGroupName4"), ""));

        } catch (Exception e) {

        }

        return productDto;
    }

    public List<ProductOptionDto> buildProductOption(Map<String, Object> resMap, ProductDto productDto, StoreDto storeDto){
        List<ProductOptionDto> productOptionDtoList = new ArrayList<>();

        Map<String, Object> originProduct = (Map<String, Object>) resMap.get("originProduct");

        List<Map<String, Object>> optionGroupList = (List<Map<String, Object>>) ((Map) ((Map) originProduct.get("detailAttribute")).get("optionInfo")).get("optionCombinations");

        if(storeDto.getId() == 6 && ((Map) originProduct.get("detailAttribute")).get("supplementProductInfo")!=null){
            System.out.println("test");

            List<Map<String, Object>> supplementProductList = (List<Map<String, Object>>) ((Map) ((Map) originProduct.get("detailAttribute")).get("supplementProductInfo")).get("supplementProducts");

            for (Map<String, Object> supplementProduct : supplementProductList) {
                if(Long.parseLong(supplementProduct.get("id").toString())==3417409907L){
                    System.out.println("test");
                }




            }
        }


        if(optionGroupList != null && !optionGroupList.isEmpty()){
            for (Map<String, Object> optionGroup : optionGroupList) {
                ProductOptionDto productOptionDto = new ProductOptionDto();
                Long id = (Long) optionGroup.get("id");
                String optionName1 = Objects.toString(optionGroup.get("optionName1"), "");
                String optionName2 = Objects.toString(optionGroup.get("optionName2"), "");
                String optionName3 = Objects.toString(optionGroup.get("optionName3"), "");
                String optionName4 = Objects.toString(optionGroup.get("optionName4"), "");
                Integer price = (Integer) optionGroup.get("price");
                Boolean usable = (Boolean) optionGroup.get("usable");

                productOptionDto.setOriginProductNo(productDto.getOriginProductNo());
                productOptionDto.setOptionId(id);
                productOptionDto.setStoreId(storeDto.getId());
                productOptionDto.setOptionName1(optionName1);
                productOptionDto.setOptionName2(optionName2);
                productOptionDto.setOptionName3(optionName3);
                productOptionDto.setOptionName4(optionName4);
                productOptionDto.setPrice(price);
                productOptionDto.setUsable(usable);

                productOptionDtoList.add(productOptionDto);
            }
        }
        return productOptionDtoList;
    }

    @Test
    @Disabled
    public void httpBuilderTest(){

        UriComponents uriComponents = UriComponentsBuilder.fromUriString(baseUrl + productInfoUrl)
            .buildAndExpand("test");

        System.out.println(uriComponents);

    }

}