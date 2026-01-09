package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.KakaoDto;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KakaoMsgMapper {

    void insertKakaoLog(Map<String, Object> map);

    int adSelectKakaoListCnt(Map<String, Object> paramMap);

    List<KakaoDto> adSelectKakaoList(Map<String, Object> paramMap);
}