package com.naver.naverspabackend.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    List<Map<String, Object>> selectDashboardListWithStoreId(Map<String, Object> dashboard);
}
