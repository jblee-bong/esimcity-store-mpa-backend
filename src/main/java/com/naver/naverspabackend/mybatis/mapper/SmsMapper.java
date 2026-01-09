package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.SmsDto;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SmsMapper {

    int insertSmsResponse(Map<String, Object> map);

    int adSelectSmsListCnt(Map<String, Object> paramMap);

    List<SmsDto> adSelectSmsList(Map<String, Object> paramMap);
}
