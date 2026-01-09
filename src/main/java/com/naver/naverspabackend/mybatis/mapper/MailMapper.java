package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.MailContentsDto;
import com.naver.naverspabackend.dto.MailDto;
import com.naver.naverspabackend.dto.SmsDto;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MailMapper {

    int insertMailResponse(MailDto mailDto);

    int adSelectMailListCnt(Map<String, Object> paramMap);

    List<MailDto> adSelectMailList(Map<String, Object> paramMap);

    void insertMailContents(MailContentsDto mailContentsDto);

    List<MailContentsDto> selectMailContentsList(MailContentsDto mailContentsDto);
}
