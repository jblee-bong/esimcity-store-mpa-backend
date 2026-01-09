package com.naver.naverspabackend.service.order;

import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.OrderRetransMailInfoDto;
import com.naver.naverspabackend.dto.OrderTugeEsimDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.util.PagingUtil;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OrderService {

    ApiResult<List<OrderDto>> fetchOrderList(Map<String, Object> map, PagingUtil pagingUtil);

    ApiResult<OrderDto> fetchOrder(Map<String, Object> map);

    OrderDto fetchOrderOnly(Map<String, Object> map);

    ApiResult<Void> updateOrderStatus(Map<String, Object> paramMap);

    ApiResult<?> fetchStatic(Map<String, Object> map);

    List<OrderDto> fetchOrderListForExcel(Map<String, Object> map);

    void updateWordMoveItem(Map<String, Object> item);

    ApiResult<Void> updateOrderMailReTrans(Map<String, Object> paramMap) throws Exception;

    ApiResult<Void> updateOrderKakaoReTrans(Map<String, Object> paramMap) throws Exception;

    ResponseEntity<Map<String, String>> updateTugeItem(Map<String, Object> item);

    OrderTugeEsimDto selecTugeItemWithIccid(OrderTugeEsimDto param);

    Map<String, String> insertReTransMailInfo(Map<String, Object> params);

    List<OrderRetransMailInfoDto> fetchRetransMailInfoDtoAll();

    void updateRetransMailInfoComfirm(OrderRetransMailInfoDto orderRetransMailInfoDto);
}
