package com.naver.naverspabackend.batch.tasklet;

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
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 메인 DB에서 batch 관련 설정값을 불러와 ExcuteContext에 저장하는 Step
 *
 */

@Component
@Slf4j
public class NaverProduct {

    @Autowired
    private NaverRedisRepository naverRedisRepository;


    @Value("${naver-api.base}")
    private String baseUrl;

    @Value("${naver-api.product-search}")
    private String productSearchUrl;

    @Autowired
    private NaverWritter naverWritter;


    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductOptionMapper productOptionMapper;



    @Value("${naver-api.origin-product-info}")
    private String productInfoUrl;


    public void processNaverStore(StoreDto storeDto) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Optional<NaverRedisToken> byStoreId = naverRedisRepository.findById(storeDto.getId());
            if (!byStoreId.isPresent()){
                log.warn("네이버 토큰이 존재하지 않습니다. Store ID: {}", storeDto.getId());
                return;
            }

            NaverRedisToken naverRedisToken = byStoreId.get();
            Map<String, String> headerMap = naverRedisToken.returnHeaderMap();

            Map<String, Object> paramMap = new HashMap<>();
            //paramMap.put("productStatusTypes", "SALE");
            paramMap.put("orderType", "REG_DATE");
            paramMap.put("page", 1);
            paramMap.put("size", 500);

            Map<String, Object> resMap = new HashMap<>();
            String res = ApiUtil.post(baseUrl + productSearchUrl, headerMap, paramMap, null, MediaType.parse("application/json; charset=UTF-8"));

            resMap = objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});

            // 최종 입력 될 product 상품
            List<ProductDto> resultProductDtoList = new ArrayList<>();
            // 최종 입력 될 productOption 상품
            List<ProductOptionDto> resultProductOptionDtoList = new ArrayList<>();

            List<Map<String, Object>> contentsList = (List<Map<String, Object>>) resMap.get("contents");

            List<Long> originProductNoList = new ArrayList<>();
            List<String> originProductSellerManagementCodeList = new ArrayList<>();
            List<String> originProductDiscountedPriceList = new ArrayList<>();

            for(Map<String,Object> content : contentsList){
                ArrayList<Map<String,Object>> channelProducts = (ArrayList<Map<String, Object>>) content.get("channelProducts");
                originProductNoList.add(Long.valueOf(channelProducts.get(0).get("originProductNo").toString()));
                if(channelProducts.get(0).get("sellerManagementCode")!=null){
                    originProductSellerManagementCodeList.add(channelProducts.get(0).get("sellerManagementCode").toString());
                }else{
                    originProductSellerManagementCodeList.add("");
                }
                originProductDiscountedPriceList.add(channelProducts.get(0).get("discountedPrice").toString());
            }

            List<ProductDto> originProductList = new ArrayList<>();
            List<ProductOptionDto> originProductOptionList = new ArrayList<>();
            Map<Long, ProductDto> originProductListToMap = new HashMap<>();
            Map<Long, ProductOptionDto> originProductOptionListToMap = new HashMap<>();

            for (int i=0;i< originProductNoList.size();i++) {
                Map<String, Object> productInfo = requestProductInfo(originProductNoList.get(i), naverRedisToken);
                ProductDto e = buildProductDto(productInfo, originProductNoList.get(i),originProductSellerManagementCodeList.get(i),originProductDiscountedPriceList.get(i), storeDto);
                originProductList.add(e);
                originProductOptionList.addAll(buildProductOption(productInfo, e, storeDto));

                try{Thread.sleep(1000);}catch (Exception e2){};
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

            naverWritter.productWrite(resultProductDtoList, resultProductOptionDtoList);

        } catch (Exception e) {
            log.error("네이버 스토어 처리 중 오류 발생 - Store ID: {}, Store Name: {}, Error: {}", storeDto.getId(), storeDto.getStoreName(), e.getMessage());
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

    public ProductDto buildProductDto(Map<String, Object> map, Long originProductNo, String originProductSellerManagementCode, String originProductDiscountedPrice, StoreDto storeDto){
        Map<String, Object> originProduct = (Map<String, Object>) map.get("originProduct");
        String productName = Objects.toString(originProduct.get("name"), "");
        String statusType = Objects.toString(originProduct.get("statusType"), "");
        Integer salePrice = (Integer) originProduct.get("salePrice");
        Integer discountedPrice = Integer.parseInt(originProductDiscountedPrice);


        String representativeImageUrl = Objects.toString(( (Map) ((Map)originProduct.get("images")).get("representativeImage")).get("url"), "");

        ProductDto productDto = new ProductDto();
        productDto.setOriginProductNo(originProductNo);
        productDto.setOriginProductSellerManagementCode(originProductSellerManagementCode);
        productDto.setStoreId(storeDto.getId());
        productDto.setProductName(productName);
        productDto.setStatusType(statusType);
        productDto.setSalePrice(salePrice);
        productDto.setDiscountedPrice(discountedPrice);
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


        try{
            //추가상품 옵션으로 추가
            if(((Map) originProduct.get("detailAttribute")).get("supplementProductInfo")!=null){
                List<Map<String, Object>> supplementProductList = (List<Map<String, Object>>) ((Map) ((Map) originProduct.get("detailAttribute")).get("supplementProductInfo")).get("supplementProducts");

                for (Map<String, Object> supplementProduct : supplementProductList) {
                    ProductOptionDto productOptionDto = new ProductOptionDto();
                    Long id = Long.parseLong(supplementProduct.get("id").toString());
                    String optionName1 = Objects.toString("****추가상품**** " + supplementProduct.get("groupName"), "");
                    String optionName2 = Objects.toString(supplementProduct.get("name"), "");
                    String optionName3 = "";
                    String optionName4 = "";
                    Integer price = (Integer) supplementProduct.get("price");
                    Boolean usable = (Boolean) supplementProduct.get("usable");
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
        }catch (Exception e){
            e.printStackTrace();
        }

        //일반 옵션 추가
        List<Map<String, Object>> optionGroupList = (List<Map<String, Object>>) ((Map) ((Map) originProduct.get("detailAttribute")).get("optionInfo")).get("optionCombinations");

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
}
