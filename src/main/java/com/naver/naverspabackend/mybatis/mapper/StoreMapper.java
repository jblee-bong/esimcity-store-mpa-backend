package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.StoreDto;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoreMapper {

    int insertStore(Map<String, Object> map);

    List<StoreDto> selectStoreList(Map<String, Object> paramMap);

    int selectStoreListCnt(Map<String, Object> paramMap);


    StoreDto selectStoreDetail(Map<String, Object> paramMap);

    void updateStore(Map<String, Object> paramMap);

    void deleteStore(Map<String, Object> paramMap);

    List<StoreDto> selectStoreListAll(Map<String, Object> paramMap);
}
