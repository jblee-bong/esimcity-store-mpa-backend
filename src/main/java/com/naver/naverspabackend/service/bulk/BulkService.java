package com.naver.naverspabackend.service.bulk;

import com.naver.naverspabackend.dto.BulkDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.util.PagingUtil;

import java.util.List;
import java.util.Map;

public interface BulkService {
    ApiResult<?> fetchListExcelUpload(List<BulkDto> matchInfoDtoList);

    ApiResult<List<BulkDto>> fetchBulkList(Map<String, Object> map, PagingUtil pagingUtil);

    List<BulkDto> fetchBulkListForExcel(Map<String, Object> map);

    ApiResult<?> updateCancel(Map<String, Object> paramMap);

    ApiResult<?> updateComfirm(Map<String, Object> paramMap);

    ApiResult<?> fetchCanUseStatic(Map<String, Object> map);

    ApiResult<List<Map<String,Object>>> fetchTitleGroupList(Map<String, Object> map);

    ApiResult<?> bulkDelete(List<Map<String, Object>> paramMap);
}