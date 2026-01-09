package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.MatchInfoDto;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.response.ApiResult;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MatchInfoMapper {

    MatchInfoDto selectMatchInfoByOrder(OrderDto orderDto);

    int inserMatchInfo(Map<String, Object> matchInfoMap);

    int adSelectMatchInfoListCnt(Map<String, Object> paramMap);

    List<MatchInfoDto> adSelectMatchInfoList(Map<String, Object> paramMap);

    MatchInfoDto fetchMatchInfo(Map<String, Object> paramMap);
    List<MatchInfoDto> fetchMatchInfoListMainId(Map<String, Object> paramMap);

    int updateMatchInfo(Map<String, Object> paramMap);

    int deleteMatchInfo(Map<String, Object> paramMap);

    List<Long> disableOptionList(Map<String, Object> paramMap);

    int deleteMatchInfoByMainId(Map<String, Object> paramMap);

    List<MatchInfoDto> adSelectMatchInfoListForExcel(Map<String, Object> map);

    void updateMatchInfoForUpload(MatchInfoDto matchInfoDto);

    List<MatchInfoDto> adSelectMatchInfoListWithMainIdForExcel(MatchInfoDto matchInfoDtoGroup);

    List<MatchInfoDto> selectMatchInfoListAll(MatchInfoDto matchInfoDto);
}
