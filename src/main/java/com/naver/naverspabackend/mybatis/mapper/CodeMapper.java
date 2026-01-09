package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.CodeDto;
import com.naver.naverspabackend.dto.ProductDto;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CodeMapper {

    @Select("select * from TB_CODE where del_yn = 'N' and code_cd != '00' order by code_cd, code_order")
    List<CodeDto> getCodeAll();

    int insertCode(Map<String, Object> paramMap);

    int updateCode(Map<String, Object> paramMap);

    int deleteCode(Map<String, Object> paramMap);

    List<CodeDto> fetchList(Map<String, Object> paramMap);

    CodeDto fetch(Map<String, Object> paramMap);

    List<ProductDto> getSellerCodeAll(Map<String, Object> paramMap);
}
