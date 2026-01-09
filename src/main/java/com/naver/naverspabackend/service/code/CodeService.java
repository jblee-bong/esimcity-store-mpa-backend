package com.naver.naverspabackend.service.code;

import com.naver.naverspabackend.dto.CodeDto;
import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.response.ApiResult;
import java.util.List;
import java.util.Map;

public interface CodeService {

    List<CodeDto> getCodeAll();

    ApiResult<?> createCode(Map<String, Object> paramMap);

    ApiResult<?> updateCode(Map<String, Object> paramMap);

    ApiResult<?> deleteCode(Map<String, Object> paramMap);

    ApiResult<List<CodeDto>> fetchList(Map<String, Object> paramMap);

    ApiResult<CodeDto> fetch(Map<String, Object> paramMap);

    ApiResult<List<ProductDto>> getSellerCodeAll(Map<String, Object> paramMap);
}
