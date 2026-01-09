package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.KakaoContentsDto;
import com.naver.naverspabackend.dto.KakaoDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface KakaoContentsMapper {
    void insertKakaoContents(KakaoContentsDto kakaoContentsDto);

    List<KakaoContentsDto> adSelectKakaContentsList(Long id);

    KakaoContentsDto findById(Map<String, Object> paramMap);
}