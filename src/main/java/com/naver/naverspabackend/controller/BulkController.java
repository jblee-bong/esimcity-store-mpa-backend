package com.naver.naverspabackend.controller;

import com.google.gson.reflect.TypeToken;
import com.naver.naverspabackend.annotation.PageResolver;
import com.naver.naverspabackend.annotation.TokenUser;
import com.naver.naverspabackend.common.ExcelFile;
import com.naver.naverspabackend.dto.BulkDto;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.bulk.BulkService;
import com.naver.naverspabackend.util.JsonUtil;
import com.naver.naverspabackend.util.PagingUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class BulkController {

    @Autowired
    private BulkService bulkService;

    @RequestMapping("/bulk/fetchList")
    public ApiResult<List<BulkDto>> fetchBulkList(@RequestBody Map<String, Object> map, @TokenUser UserDto userDto1, @PageResolver PagingUtil pagingUtil) {
        map.put("email",userDto1.getEmail());
        map.put("userAuthority",userDto1.getUserAuthority().name());

        return bulkService.fetchBulkList(map, pagingUtil);
    }

    @RequestMapping("/bulk/fetchCanUseStatic")
    public ApiResult<?> fetchCanUseStatic(@RequestBody Map<String, Object> map, @TokenUser UserDto userDto1) {
        map.put("email",userDto1.getEmail());
        map.put("userAuthority",userDto1.getUserAuthority().name());

        return bulkService.fetchCanUseStatic(map);
    }

    @RequestMapping("/bulk/fetchTitleGroupList")
    public ApiResult<?> fetchTitleGroupList(@TokenUser UserDto userDto1) {
        Map<String, Object> map = new HashMap<>();
        map.put("email",userDto1.getEmail());
        map.put("userAuthority",userDto1.getUserAuthority().name());

        return bulkService.fetchTitleGroupList(map);
    }


    @PostMapping(value = "/bulk/fetchListExcelUpload")
    public ApiResult<?> fetchListExcelUpload(@RequestParam(name="multipartFile") MultipartFile file, @TokenUser UserDto userDto1) throws Exception {

        ExcelFile<BulkDto> excelFile = new ExcelFile<>();
        List<Map<String,Object>> items = excelFile.uploadExcel(file.getInputStream(), BulkDto.class);


        TypeToken<ArrayList<BulkDto>> token = new TypeToken<ArrayList<BulkDto>>() {};
        List<BulkDto> matchInfoDtoList = JsonUtil.fromJson(JsonUtil.toJson(items), token.getType());

        return bulkService.fetchListExcelUpload(matchInfoDtoList);
    }

    @PostMapping(value = "/bulk/cancelUpdate")
    public ApiResult<?> cancelUpdate(@RequestBody Map<String, Object> paramMap){
        return bulkService.updateCancel(paramMap);
    }
    @PostMapping(value = "/bulk/bulkDelete")
    public ApiResult<?> cancelUpdate(@RequestBody List<Map<String, Object>> paramMap){
        return bulkService.bulkDelete(paramMap);
    }


    @PostMapping(value = "/bulk/comfirmUpdate")
    public ApiResult<?> comfirmUpdate(@RequestBody Map<String, Object> paramMap){
        return bulkService.updateComfirm(paramMap);
    }

    @PostMapping(value = "/bulk/fetchListExceldownload")
    public void fetchListExceldownload(HttpServletResponse response, @RequestBody Map<String, Object> map, @TokenUser UserDto userDto1) throws Exception {
        map.put("email",userDto1.getEmail());
        map.put("userAuthority",userDto1.getUserAuthority().name());
        List<BulkDto> bulkDtoList = bulkService.fetchBulkListForExcel(map);

        SXSSFWorkbook wb = null;
        OutputStream stream = null;

        try {
            ExcelFile<BulkDto> excelFile = new ExcelFile<>();
            wb = excelFile.renderExcel(bulkDtoList, BulkDto.class, null);

            stream = response.getOutputStream();
            wb.write(stream);
            wb.dispose();
        } finally {
            wb.close();
            stream.close();
        }
    }



}
