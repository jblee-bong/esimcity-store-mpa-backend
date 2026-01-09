package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.EsimPriceDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EsimPriceMapper {


    EsimPriceDto findById(EsimPriceDto esimPriceDto);
}
