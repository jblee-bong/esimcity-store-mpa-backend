package com.naver.naverspabackend.service.bulk.impl;

import com.naver.naverspabackend.dto.BulkDto;
import com.naver.naverspabackend.dto.MatchInfoDto;
import com.naver.naverspabackend.mybatis.mapper.BulkMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.bulk.BulkService;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.PagingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BulkServiceImpl implements BulkService {

    @Autowired
    private BulkMapper bulkMapper;


    @Override
    public ApiResult<List<BulkDto>> fetchBulkList(Map<String, Object> map, PagingUtil pagingUtil) {
        CommonUtil.setPageIntoMap(map, pagingUtil, bulkMapper.selectBulkListCnt(map));
        return ApiResult.succeed(bulkMapper.selectBulkList(map), pagingUtil);
    }

    @Override
    public List<BulkDto> fetchBulkListForExcel(Map<String, Object> map) {
        return bulkMapper.selectBulkListForExcel(map);
    }

    @Override
    public ApiResult<?> updateCancel(Map<String, Object> paramMap) {
        return ApiResult.succeed(bulkMapper.updateCancel(paramMap), null);
    }

    @Override
    public ApiResult<?> updateComfirm(Map<String, Object> paramMap) {
        return ApiResult.succeed(bulkMapper.updateComfirm(paramMap), null);
    }

    @Override
    public ApiResult<?> fetchCanUseStatic(Map<String, Object> map) {
        return ApiResult.succeed(bulkMapper.adSelectCanUseStatic(map),null);
    }

    @Override
    public ApiResult<List<Map<String, Object>>> fetchTitleGroupList(Map<String, Object> map) {

        return ApiResult.succeed(bulkMapper.adSelectTitleGroupList(map),null);
    }

    @Override
    public ApiResult<?> bulkDelete(List<Map<String, Object>> paramMap) {
        for(Map<String,Object> map :paramMap){
            bulkMapper.bulkDelete(map);
        }
        return ApiResult.succeed(1,null);
    }


    @Override
    public ApiResult<?> fetchListExcelUpload(List<BulkDto> bulkDtoList) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

        for(BulkDto bulkDto:bulkDtoList){
            try{
                Date date = formatter.parse(bulkDto.getBulkOpenDt());
                Calendar dateCalendar = Calendar.getInstance();
                dateCalendar.setTime(date);
                dateCalendar.add(Calendar.DAY_OF_MONTH,Integer.parseInt(bulkDto.getBulkExpiredDay()));
                bulkDto.setBulkExpiredDt(formatter.format(dateCalendar.getTime()));
            }catch (Exception e){
                e.printStackTrace();
            }
            bulkMapper.insertBulk(bulkDto);
        }
        return ApiResult.succeed(null, null);
    }
}
