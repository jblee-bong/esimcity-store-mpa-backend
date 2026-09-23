package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.SearchAdShoppingDto;
import com.naver.naverspabackend.dto.SearchAdShoppingHistDto;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SearchAdShoppingMapper {

    int insert(Map<String, Object> paramMap);

    int update(Map<String, Object> paramMap);

    int delete(Map<String, Object> paramMap);

    List<SearchAdShoppingDto> selectList(Map<String, Object> paramMap);

    int selectListCnt(Map<String, Object> paramMap);

    SearchAdShoppingDto selectDetail(Map<String, Object> paramMap);

    List<SearchAdShoppingDto> selectActiveList();

    int updateBidStatus(Map<String, Object> paramMap);

    int insertHist(Map<String, Object> paramMap);

    List<SearchAdShoppingHistDto> selectHistList(Map<String, Object> paramMap);
}
