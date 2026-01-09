package com.naver.naverspabackend.batch.tasklet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.batch.writer.CoupangWritter;
import com.naver.naverspabackend.batch.writer.NaverWritter;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.ProductOptionDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.ProductMapper;
import com.naver.naverspabackend.mybatis.mapper.ProductOptionMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.CoupangUtil;
import okhttp3.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootTest
class CoupangProductTaskletTest {

    @Value("${coupang-api.base}")
    private String baseUrl;


    @Value("${coupang-api.product-search}")
    private String productSearchUrl;

    @Value("${coupang-api.product-detail}")
    private String productDetailUrl;

    @Value("${coupang-api.category}")
    private String categoryUrl;


    @Autowired
    private CoupangWritter coupangWritter;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductOptionMapper productOptionMapper;

    @Test
    void execute() {

        Map<String,Object> param = new HashMap<>();
        param.put("userAuthority","ROLE_ADMIN");
        List<StoreDto> storeDtos = storeMapper.selectStoreList(param);
        for (StoreDto storeDto : storeDtos) {
            if(storeDto.getPlatform()==null){
            }else if(storeDto.getPlatform().equals("naver")){
            }else if(storeDto.getPlatform().equals("coupang")){
                processCoupangStore(storeDto);
            }else{
            }
        }
    }


    public void processCoupangStore(StoreDto storeDto) {
        try {
            List<Map<String, Object>> contentsList = new ArrayList<>();
            String nextToken = "1";
            while (nextToken!=null){
                Map<String, Object> resMap = requestProductList(storeDto,nextToken);
                contentsList.addAll((List<Map<String, Object>>)resMap.get("data"));
                if(resMap.get("nextToken")==null || resMap.get("nextToken").equals("")){
                    nextToken = null;
                }else{
                    nextToken = resMap.get("nextToken").toString();
                }
            }


            // 최종 입력 될 product 상품
            List<ProductDto> resultProductDtoList = new ArrayList<>();
            // 최종 입력 될 productOption 상품
            List<ProductOptionDto> resultProductOptionDtoList = new ArrayList<>();

            List<Long> originProductNoList = new ArrayList<>();
            List<String> originProductSellerManagementCodeList = new ArrayList<>();
            //TODO 가격을 어떻게 알수 있을까
            List<String> originProductDiscountedPriceList = new ArrayList<>();

            for(Map<String,Object> content : contentsList){
                originProductNoList.add(Long.parseLong(content.get("sellerProductId").toString()));
                String displayCategoryCode = content.get("displayCategoryCode").toString();


                Map<String, Object> resMap =    requestProductCategory(storeDto,displayCategoryCode);
                Map<String, Object> category = (Map<String, Object>) resMap.get("data");
                originProductSellerManagementCodeList.add(category.get("name").toString());

                originProductDiscountedPriceList.add("0");
                try{Thread.sleep(500);}catch (Exception e2){};
            }

            List<ProductDto> originProductList = new ArrayList<>();
            List<ProductOptionDto> originProductOptionList = new ArrayList<>();
            Map<Long, ProductDto> originProductListToMap = new HashMap<>();
            Map<Long, ProductOptionDto> originProductOptionListToMap = new HashMap<>();

            for (int i=0;i< originProductNoList.size();i++) {
                Map<String, Object> productInfo = requestProductInfo(storeDto,originProductNoList.get(i));
                ProductDto e = buildProductDto(productInfo, originProductNoList.get(i),originProductSellerManagementCodeList.get(i),"0", storeDto);
                originProductList.add(e);
                originProductOptionList.addAll(buildProductOption(productInfo, e, storeDto));

                try{Thread.sleep(500);}catch (Exception e2){};
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
                        localProduct.setOriginProductSellerManagementCode(originProduct.getOriginProductSellerManagementCode());
                        localProduct.setProductName(originProduct.getProductName());
                        localProduct.setStatusType(originProduct.getStatusType());
                        localProduct.setRepresentativeImageUrl(originProduct.getRepresentativeImageUrl());
                        localProduct.setOptionGroupName1(originProduct.getOptionGroupName1());
                        localProduct.setOptionGroupName2(originProduct.getOptionGroupName2());
                        localProduct.setOptionGroupName3(originProduct.getOptionGroupName3());
                        localProduct.setOptionGroupName4(originProduct.getOptionGroupName4());
                        localProduct.setSalePrice(originProduct.getSalePrice());
                        localProduct.setDiscountedPrice(originProduct.getDiscountedPrice());
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

            coupangWritter.productWrite(resultProductDtoList, resultProductOptionDtoList);




            // 최종

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public Map<String, Object> requestProductList(StoreDto storeDto, String nextToken) throws Exception {

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("vendorId", storeDto.getVendorId());
        paramMap.put("nextToken", nextToken);
        paramMap.put("maxPerPage", 100);

        String uri = ApiUtil.buildQueryParameter(productSearchUrl,paramMap,true);
        String authorization = CoupangUtil.getAuthorization("GET",uri,storeDto.getClientId(),storeDto.getClientSecret());
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", authorization);
        headerMap.put("content-type", "application/json");

        Map<String, Object> resMap = new HashMap<>();
        String res = ApiUtil.get(baseUrl + uri,headerMap);

        return objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> requestProductCategory(StoreDto storeDto, String displayCategoryCode) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(categoryUrl);
        UriComponents uriComponents = builder.buildAndExpand(displayCategoryCode + "");


        Map<String, Object> paramMap = new HashMap<>();
        String uri = ApiUtil.buildQueryParameter(uriComponents.toUriString(),paramMap,true);
        String authorization = CoupangUtil.getAuthorization("GET",uri,storeDto.getClientId(),storeDto.getClientSecret());
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", authorization);
        headerMap.put("content-type", "application/json");
        Map<String, Object> resMap = new HashMap<>();
        String res = ApiUtil.get(baseUrl + uri,headerMap);

        return objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});
    }


    public Map<String, Object> requestProductInfo(StoreDto storeDto, Long originProductNo) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(productDetailUrl);
        UriComponents uriComponents = builder.buildAndExpand(originProductNo + "");


        Map<String, Object> paramMap = new HashMap<>();
        String uri = ApiUtil.buildQueryParameter(uriComponents.toUriString(),paramMap,true);
        String authorization = CoupangUtil.getAuthorization("GET",uri,storeDto.getClientId(),storeDto.getClientSecret());
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", authorization);
        headerMap.put("content-type", "application/json");
        Map<String, Object> resMap = new HashMap<>();
        String res = ApiUtil.get(baseUrl + uri,headerMap);

        return objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});
    }


    public ProductDto buildProductDto(Map<String, Object> map, Long originProductNo, String originProductSellerManagementCode, String originProductDiscountedPrice, StoreDto storeDto){
        Map<String, Object> originProduct = (Map<String, Object>) map.get("data");

        String productName = Objects.toString(originProduct.get("sellerProductName"), "");
        String statusType = Objects.toString(originProduct.get("statusName"), "");
        Integer salePrice = 0;//TODO 판매가 어떻게?
        Integer discountedPrice = Integer.parseInt(originProductDiscountedPrice);



        ProductDto productDto = new ProductDto();
        productDto.setOriginProductNo(originProductNo);
        productDto.setOriginProductSellerManagementCode(originProductSellerManagementCode);
        productDto.setStoreId(storeDto.getId());
        productDto.setProductName(productName);
        productDto.setStatusType(statusType);
        productDto.setSalePrice(salePrice);
        productDto.setDiscountedPrice(discountedPrice);
        productDto.setRepresentativeImageUrl(null);

        try {
            //TODO옵션그룹이 없는것 같음 옵션 그룸 네임이 없음

            //productDto.setOptionGroupName1(Objects.toString(optionGroupNameMap.get("optionGroupName1"), ""));
            //productDto.setOptionGroupName2(Objects.toString(optionGroupNameMap.get("optionGroupName2"), ""));
            //productDto.setOptionGroupName3(Objects.toString(optionGroupNameMap.get("optionGroupName3"), ""));
            //productDto.setOptionGroupName4(Objects.toString(optionGroupNameMap.get("optionGroupName4"), ""));
        } catch (Exception e) {

        }

        return productDto;
    }

    public List<ProductOptionDto> buildProductOption(Map<String, Object> map, ProductDto productDto, StoreDto storeDto){
        Map<String, Object> originProduct = (Map<String, Object>) map.get("data");


        List<ProductOptionDto> productOptionDtoList = new ArrayList<>();


        List<Map<String, Object>> optionList = (List<Map<String, Object>>) originProduct.get("items");

        if(optionList != null && !optionList.isEmpty()){
            for (Map<String, Object> option : optionList) {
                ProductOptionDto productOptionDto = new ProductOptionDto();
                if(option.get("vendorItemId")==null)
                    continue;
                Long id = (Long) option.get("vendorItemId");
                String optionName1 = Objects.toString(option.get("itemName"), "");
                String optionName2 = null;
                String optionName3 = null;
                String optionName4 = null;
                Integer price = Integer.parseInt(option.get("salePrice").toString());
                Boolean usable = true;

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
    public void test(){
        System.out.println((new Date()).getTime());
    }
}