package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.EsimPriceDto;
import com.naver.naverspabackend.dto.TopupOrderDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TopupOrderMapper {


    void insert(TopupOrderDto topupOrderDto);

    TopupOrderDto findByTokenId(TopupOrderDto param);

    void updateTopupStatus(TopupOrderDto topupOrderDto);

    void updatePaypalStatus(TopupOrderDto topupOrderDto);

    void updateTopupOrderNo(TopupOrderDto topupOrderDto);

    List<TopupOrderDto> findByEsimIccidSuccessCharge(TopupOrderDto topupOrderParam);
}
