package com.naver.naverspabackend.batch.tasklet;


import com.naver.naverspabackend.dto.OrderRetransMailInfoDto;
import com.naver.naverspabackend.service.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * esim api의 상품 목록 조회화여 db 저장
 *
 * @author jblee
 */



@SpringBootTest
public class ApiRetransMailSchedulerTest {



    @Autowired
    private OrderService orderService;



    @Test
    public void ApiRetransMail () {

        List<OrderRetransMailInfoDto> orderRetransMailInfoDtoList = orderService.fetchRetransMailInfoDtoAll();

        for(OrderRetransMailInfoDto orderRetransMailInfoDto : orderRetransMailInfoDtoList) {
            try{
                Map<String, Object> paramMapMap =new HashMap<>();
                paramMapMap.put("id", orderRetransMailInfoDto.getOrderId());
                paramMapMap.put("reEmail", orderRetransMailInfoDto.getMail());
                orderService.updateOrderMailReTrans(paramMapMap);
                orderService.updateRetransMailInfoComfirm(orderRetransMailInfoDto);
            }catch (Exception e) {

            }
        }
    }
}