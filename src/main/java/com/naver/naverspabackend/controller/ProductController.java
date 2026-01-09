package com.naver.naverspabackend.controller;

import com.naver.naverspabackend.annotation.PageResolver;
import com.naver.naverspabackend.annotation.TokenUser;
import com.naver.naverspabackend.common.ExcelFile;
import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.ProductOptionDto;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.product.ProductService;
import com.naver.naverspabackend.util.PagingUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @RequestMapping("/product/fetchList")
    public ApiResult<List<ProductDto>> fetchProductList(@RequestBody Map<String, Object> map, @TokenUser UserDto userDto1, @PageResolver PagingUtil pagingUtil){
        map.put("email",userDto1.getEmail());
        map.put("userAuthority",userDto1.getUserAuthority().name());

        return productService.fetchProductList(map, pagingUtil);
    }

    @RequestMapping("/product/option/fetchList")
    public ApiResult<List<ProductOptionDto>> fetchProductOptionList(@RequestBody Map<String, Object> map, @PageResolver PagingUtil pagingUtil){
        return productService.fetchProductOptionList(map, pagingUtil);
    }
    @RequestMapping("/product/option/fetchListExceldownload")
    public void fetchProductOptionListExcelDownload(HttpServletResponse response, @RequestBody Map<String, Object> map, @TokenUser UserDto userDto1) throws IOException {
        map.put("email",userDto1.getEmail());
        map.put("userAuthority",userDto1.getUserAuthority().name());
        List<ProductOptionDto> ProductOptionDtoList = productService.fetchProductOptionListForExcel(map);

        SXSSFWorkbook wb = null;
        OutputStream stream = null;

        try {
            ExcelFile<ProductOptionDto> excelFile = new ExcelFile<>();
            wb = excelFile.renderExcel(ProductOptionDtoList, ProductOptionDto.class, null);

            stream = response.getOutputStream();
            wb.write(stream);
            wb.dispose();
        } finally {
            wb.close();
            stream.close();
        }
    }

    @RequestMapping("/product/fetch")
    public ApiResult<ProductDto> fetchProduct(@RequestBody Map<String, Object> map){
        return productService.fetchProduct(map);
    }

    @RequestMapping("/product/option/fetch")
    public ApiResult<ProductOptionDto> fetchProductOption(@RequestBody Map<String, Object> map){
        return productService.fetchProductOption(map);
    }


    @RequestMapping("/product/resetData")
    public ApiResult<Integer> fetchProductResetData(@TokenUser UserDto userDto){

        return productService.fetchProductResetData();
    }

}
