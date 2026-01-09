package com.naver.naverspabackend.controller;

import com.naver.naverspabackend.annotation.PageResolver;
import com.naver.naverspabackend.annotation.TokenUser;
import com.naver.naverspabackend.common.ExcelFile;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.ProductOptionDto;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.order.OrderService;
import com.naver.naverspabackend.service.product.ProductService;
import com.naver.naverspabackend.util.PagingUtil;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @RequestMapping("/order/fetchList")
    public ApiResult<List<OrderDto>> fetchOrderList(@RequestBody Map<String, Object> map, @TokenUser UserDto userDto1, @PageResolver PagingUtil pagingUtil) {
        map.put("email",userDto1.getEmail());
        map.put("userAuthority",userDto1.getUserAuthority().name());

        return orderService.fetchOrderList(map, pagingUtil);
    }



    @PostMapping(value = "/order/fetchListExceldownload")
    public void fetchListExceldownload(HttpServletResponse response, @RequestBody Map<String, Object> map, @TokenUser UserDto userDto1) throws Exception {
        map.put("email",userDto1.getEmail());
        map.put("userAuthority",userDto1.getUserAuthority().name());
        List<OrderDto> orderDtoList = orderService.fetchOrderListForExcel(map);

        SXSSFWorkbook wb = null;
        OutputStream stream = null;

        try {
            ExcelFile<OrderDto> excelFile = new ExcelFile<>();
            wb = excelFile.renderExcel(orderDtoList, OrderDto.class, null);

            stream = response.getOutputStream();
            wb.write(stream);
            wb.dispose();
        } finally {
            wb.close();
            stream.close();
        }
    }



        @RequestMapping("/order/fetchStatic")
    public ApiResult<?> fetchStatic(@RequestBody Map<String, Object> map, @TokenUser UserDto userDto1) {
        map.put("email",userDto1.getEmail());
        map.put("userAuthority",userDto1.getUserAuthority().name());

        return orderService.fetchStatic(map);
    }


    @RequestMapping("/order/fetch")
    public ApiResult<OrderDto> fetchOrder(@RequestBody Map<String, Object> map) {
        return orderService.fetchOrder(map);
    }


    @RequestMapping("/order/updateStatus")
    public ApiResult<Void> updateOrderStatus(@RequestBody Map<String, Object> paramMap){
        return orderService.updateOrderStatus(paramMap);
    }


    @RequestMapping("/order/updateOrderMailReTrans")
    public ApiResult<Void> updateOrderMailReTrans(@RequestBody Map<String, Object> paramMap) throws Exception {
        return orderService.updateOrderMailReTrans(paramMap);
    }

    @RequestMapping("/order/updateOrderKakaoReTrans")
    public ApiResult<Void> updateOrderKakaoReTrans(@RequestBody Map<String, Object> paramMap) throws Exception {
        return orderService.updateOrderKakaoReTrans(paramMap);
    }
}
