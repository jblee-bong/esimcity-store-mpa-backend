package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.*;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {


    int insertOrder(OrderDto orderDto);

    List<OrderDto> selectOrderForSms(StoreDto storeDto);

    void updateOrderSms(OrderDto orderDto);

    int adSelectOrderListCnt(Map<String, Object> map);

    List<OrderDto> adSelectOrderList(Map<String, Object> map);

    OrderDto fetchOrder(Map<String, Object> map);

    void updateOrderStatus(Map<String, Object> paramMap);

    Map<String,Object> adSelectOrderStatic(Map<String, Object> map);

    List<OrderDto> fetchOrderListForExcel(Map<String, Object> map);

    void updateOrderSmsForEsim(OrderDto orderDto);


    OrderDto adSelectOrder(Map<String, Object> paramMap);

    void updateOrderOnlyStatus(Map<String, Object> paramMap);


    OrderDto selectOrderWithEsimIccid(OrderDto orderDto);
    OrderDto selectOrderWithEsimApiRequestId(OrderDto orderDto);

    void insertOrderWorldmoveEsim(OrderWorldmoveEsimDto orderWorldmoveEsimDto);

    void insertOrderTugeEsim(OrderTugeEsimDto orderTugeEsimDto);

    void updateOrderSmsForEsimIccidWordMove(OrderDto orderDto);

    int selectCountOrderWorldMoveEsim(Map<String, Object> param);

    List<OrderTugeEsimDto> selectListOrderTugeEsim(Map<String, Object> param);

    OrderWorldmoveEsimDto selectOrderWorldMoveEsim(OrderWorldmoveEsimDto orderWroldMoveEsimParam);

    String selectOrderAiraloToken();

    void inserttOrderAiraloToken(String airAloToken);

    void updateOrderSmsForEsimIccidAll(OrderDto orderDto);

    void updateOrderSmsForEsimIccid(OrderDto orderDto);

    void updateOrderSmsForEsimActivationCode(OrderDto orderDto);

    void updateOrderSmsForEsimApiRequestId(OrderDto orderDto);


    void updateOrderMethod(OrderDto orderDto);

    OrderDto findById(Map<String, Object> paramMap);


    void updateOrderForTransSuccessYn(OrderDto orderDto);

    void updateBeforeStatus(OrderDto orderDto);

    OrderDto selectOrderWithProductOrderIdAndOrderId(OrderDto orderDto);

    List<OrderDto> selectOrderForTransStatusChange(StoreDto storeDto);

    List<OrderDto> selectOrderForOrderingStatusChange(StoreDto storeDto);

    void updateOrderForOrderingUseStatus(OrderDto orderDtoParam);

    List<OrderDto> selectOrderForCancelIccidUpdate(StoreDto storeDto);

    OrderDto fetchOrderOnly(Map<String, Object> map);

    void updateOrderAllPrice(OrderDto orderDto);

    void updateOrderSmsForEsimApn(OrderDto orderDto);

    OrderTugeEsimDto selecTugeItemWithIccid(OrderTugeEsimDto param);

    void updateOrderDecided(OrderDto orderDto);

    void updateOrderDecidedMethod(OrderDto orderDto);

    OrderWorldmoveEsimDto selectOrderWorldMoveEsimFirst(OrderWorldmoveEsimDto orderWroldMoveEsimParam);

    void insertReTransMailInfo(OrderRetransMailInfoDto orderRetransMailInfoDto);

    List<OrderRetransMailInfoDto> fetchRetransMailInfoDtoAll();

    void updateRetransMailInfoComfirm(OrderRetransMailInfoDto orderRetransMailInfoDto);
}
