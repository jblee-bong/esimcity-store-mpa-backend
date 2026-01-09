package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.ProductOptionDto;
import com.naver.naverspabackend.dto.StoreDto;
import java.util.List;
import java.util.Map;
import org.apache.catalina.Store;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductOptionMapper {

    List<ProductOptionDto> selectProductOptionList(StoreDto storeDto);

    int insertProductOption(ProductOptionDto productOptionDto);

    int updateProductOption(ProductOptionDto productOptionDto);

    int deleteProductOption(ProductOptionDto productOptionDto);

    int adSelectProductOptionListCnt(Map<String, Object> map);

    List<ProductOptionDto> adSelectProductOptionList(Map<String, Object> map);

    ProductOptionDto fetchProductOption(Map<String, Object> map);

    List<ProductOptionDto> adSelectProductOptionListForExcel(Map<String, Object> map);

    List<ProductDto> fetchProductListAll();
}
