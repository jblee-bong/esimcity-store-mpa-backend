package com.naver.naverspabackend.service.code.impl;

import com.naver.naverspabackend.dto.CodeDto;
import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.mybatis.mapper.CodeMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.response.model.ResponseCode;
import com.naver.naverspabackend.service.code.CodeService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeServiceImpl implements CodeService {

    @Autowired
    private CodeMapper codeMapper;

    @Override
    public List<CodeDto> getCodeAll() {
        return codeMapper.getCodeAll();
    }

    @Override
    public ApiResult<?> createCode(Map<String, Object> paramMap){
        CodeDto fetch = codeMapper.fetch(paramMap);
        if(fetch != null){
            return ApiResult.failed("중복된 코드명이 존재합니다!", ResponseCode.SUCC);
        }
        return ApiResult.succeed(codeMapper.insertCode(paramMap), null);
    }

    @Override
    public ApiResult<?> updateCode(Map<String, Object> paramMap) {
        return ApiResult.succeed(codeMapper.updateCode(paramMap), null);
    }

    @Override
    public ApiResult<?> deleteCode(Map<String, Object> paramMap) {
        return ApiResult.succeed(codeMapper.deleteCode(paramMap), null);
    }

    @Override
    public ApiResult<List<CodeDto>> fetchList(Map<String, Object> paramMap) {
        return ApiResult.succeed(codeMapper.fetchList(paramMap), null);
    }

    @Override
    public ApiResult<CodeDto> fetch(Map<String, Object> paramMap) {
        return ApiResult.succeed(codeMapper.fetch(paramMap), null);
    }

    @Override
    public ApiResult<List<ProductDto>> getSellerCodeAll(Map<String, Object> paramMap) {
        return ApiResult.succeed(codeMapper.getSellerCodeAll(paramMap), null);
    }
}
