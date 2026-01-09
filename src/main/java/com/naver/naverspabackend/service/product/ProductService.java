package com.naver.naverspabackend.service.product;

import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.ProductOptionDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.List;
import java.util.Map;

public interface ProductService {

    ApiResult<List<ProductDto>> fetchProductList(Map<String, Object> map, PagingUtil pagingUtil);

    ApiResult<List<ProductOptionDto>> fetchProductOptionList(Map<String, Object> map, PagingUtil pagingUtil);

    List<ProductOptionDto> fetchProductOptionListForExcel(Map<String, Object> map);

    ApiResult<ProductDto> fetchProduct(Map<String, Object> map);

    ApiResult<ProductOptionDto> fetchProductOption(Map<String, Object> map);


    List<ProductDto> selectProductList(StoreDto storeDto);

    ApiResult<Integer> fetchProductResetData();

}
