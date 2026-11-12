package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.EsimInstallTokenDto;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EsimInstallTokenMapper {

    int insert(Map<String, Object> paramMap);

    EsimInstallTokenDto selectValidByToken(Map<String, Object> paramMap);
}
