package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.SearchAdKeywordDto;
import com.naver.naverspabackend.dto.SearchAdKeywordHistDto;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SearchAdKeywordMapper {

    int insert(Map<String, Object> paramMap);

    int update(Map<String, Object> paramMap);

    int delete(Map<String, Object> paramMap);

    List<SearchAdKeywordDto> selectList(Map<String, Object> paramMap);

    int selectListCnt(Map<String, Object> paramMap);

    SearchAdKeywordDto selectDetail(Map<String, Object> paramMap);

    List<SearchAdKeywordDto> selectActiveList();

    int updateBidStatus(Map<String, Object> paramMap);

    int insertHist(Map<String, Object> paramMap);

    List<SearchAdKeywordHistDto> selectHistList(Map<String, Object> paramMap);
}
