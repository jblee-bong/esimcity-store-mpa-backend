package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.EsimApiIngStepLogsDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EsimApiIngStepLogsMapper {

    void insert(EsimApiIngStepLogsDto esimApiIngStepLogsDto);

    List<EsimApiIngStepLogsDto> adSelectEsimApiIngStepLogsList(Long id);
}
