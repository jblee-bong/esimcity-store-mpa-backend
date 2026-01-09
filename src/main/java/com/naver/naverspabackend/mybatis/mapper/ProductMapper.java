package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.StoreDto;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper {

    List<ProductDto> selectProductList(StoreDto storeDto);

    int insertProduct(ProductDto productDto);

    int updateProduct(ProductDto productDto);

    int deleteProduct(ProductDto productDto);

    int adSelectProductListCnt(Map<String, Object> map);

    List<ProductDto> adSelectProductList(Map<String, Object> map);

    ProductDto fetchProduct(Map<String, Object> map);
}
