package com.naver.naverspabackend.service.product.impl;

import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.ProductOptionDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.ProductMapper;
import com.naver.naverspabackend.mybatis.mapper.ProductOptionMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.security.BatchRedisRepository;
import com.naver.naverspabackend.security.token.BatchRedisToken;
import com.naver.naverspabackend.service.product.ProductService;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.PagingUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductOptionMapper productOptionMapper;

    @Autowired
    private BatchRedisRepository batchRedisRepository;

    @Value("${spring.profiles.active}")
    private String active;
    @Override
    public ApiResult<List<ProductDto>> fetchProductList(Map<String, Object> map, PagingUtil pagingUtil) {
        CommonUtil.setPageIntoMap(map, pagingUtil, productMapper.adSelectProductListCnt(map));
        return ApiResult.succeed(productMapper.adSelectProductList(map), pagingUtil);
    }

    @Override
    public ApiResult<List<ProductOptionDto>> fetchProductOptionList(Map<String, Object> map, PagingUtil pagingUtil) {
        CommonUtil.setPageIntoMap(map, pagingUtil, productOptionMapper.adSelectProductOptionListCnt(map));
        return ApiResult.succeed(productOptionMapper.adSelectProductOptionList(map), pagingUtil);
    }

    @Override
    public List<ProductOptionDto> fetchProductOptionListForExcel(Map<String, Object> map) {
        return productOptionMapper.adSelectProductOptionListForExcel(map);
    }

    @Override
    public ApiResult<ProductDto> fetchProduct(Map<String, Object> map) {
        return ApiResult.succeed(productMapper.fetchProduct(map), null);
    }

    @Override
    public ApiResult<ProductOptionDto> fetchProductOption(Map<String, Object> map) {
        return ApiResult.succeed(productOptionMapper.fetchProductOption(map), null);
    }

    @Override
    public List<ProductDto> selectProductList(StoreDto storeDto) {
        return productMapper.selectProductList(storeDto);
    }

    @Override
    public ApiResult<Integer> fetchProductResetData() {

        String batchId = active+1;
        BatchRedisToken batchRedisToken = new BatchRedisToken();
        batchRedisToken.setBatchId(batchId);
        batchRedisRepository.delete(batchRedisToken);
        return ApiResult.succeed(1,null);
    }
}
