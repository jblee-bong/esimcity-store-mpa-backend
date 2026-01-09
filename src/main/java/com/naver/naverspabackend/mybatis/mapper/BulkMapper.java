package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.BulkDto;
import com.naver.naverspabackend.dto.OrderDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface BulkMapper {

    int selectBulkListCnt(Map<String, Object> map);

    List<BulkDto> selectBulkList(Map<String, Object> map);

    void insertBulk(BulkDto bulkDto);

    List<BulkDto> selectBulkListForExcel(Map<String, Object> map);

    List<Map<String,Object>> selectBulkListWithGroupByTitle();

    BulkDto selectNotUseBulkWithTitle(BulkDto bulkDto);

    void updateBulkOrder(BulkDto bulkDto);

    List<BulkDto> selectNotUseBulkWithOrderId(OrderDto orderDto);

    int updateCancel(Map<String, Object> paramMap);

    int updateComfirm(Map<String, Object> paramMap);

    Map<String,Object> adSelectCanUseStatic(Map<String, Object> map);

    List<Map<String, Object>> adSelectTitleGroupList(Map<String, Object> map);

    int bulkDelete(Map<String, Object> paramMap);
}
