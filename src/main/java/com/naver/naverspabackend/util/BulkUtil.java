package com.naver.naverspabackend.util;

import com.naver.naverspabackend.dto.BulkDto;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.enums.EsimApiIngSteLogsType;
import com.naver.naverspabackend.mybatis.mapper.BulkMapper;
import com.naver.naverspabackend.service.esimapiingsteplogs.EsimApiIngStepLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BulkUtil {

    public static BulkMapper bulkMapper;
    @Autowired
    public void BulkUtil(BulkMapper bulkMapper) {
        this.bulkMapper = bulkMapper;
    }


    public static EsimApiIngStepLogsService esimApiIngStepLogsService;
    @Autowired
    public void setEsimApiIngStepLogsService(EsimApiIngStepLogsService esimApiIngStepLogsService) {
        this.esimApiIngStepLogsService = esimApiIngStepLogsService;
    }

    public static List<Map<String, Object>> contextLoads1()  {
        return bulkMapper.selectBulkListWithGroupByTitle();
    }

    public static HashMap contextLoads2( String esimProductId, OrderDto orderDto, Long orderId) throws Exception {
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.START.getExplain(), orderId);

        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE.getExplain(), orderId);
        BulkDto searchBulkDto = new BulkDto();
        searchBulkDto.setBulkTitle(esimProductId);
        BulkDto bulkDto = bulkMapper.selectNotUseBulkWithTitle(searchBulkDto);
        if(bulkDto==null){
            throw new Exception(esimProductId + "벌크를 다사용함.");
        }
        bulkDto.setOrderId(orderDto.getId());

        bulkMapper.updateBulkOrder(bulkDto);
        HashMap hashMap = new HashMap();
        hashMap.put("bulkSmdp",bulkDto.getBulkSmdp());
        hashMap.put("bulkActiveCode",bulkDto.getBulkActiveCode());
        hashMap.put("bulkIccid",bulkDto.getBulkIccid());
        hashMap.put("bulkTitle",bulkDto.getBulkTitle());
        hashMap.put("orderId",bulkDto.getOrderId());
        hashMap.put("id",bulkDto.getId());
        if(bulkDto.getRentalNo()!=null)
            hashMap.put("rentalNo",bulkDto.getRentalNo());
        else
            hashMap.put("rentalNo","");
        esimApiIngStepLogsService.insert(EsimApiIngSteLogsType.PURCHASE_END.getExplain(), orderId);
        return hashMap;
    }


    public static HashMap contextLoads3( int qualityIndex, OrderDto orderDto) throws Exception {

        List<BulkDto> bulkDtoList = bulkMapper.selectNotUseBulkWithOrderId(orderDto);


        HashMap hashMap = new HashMap();
        hashMap.put("bulkSmdp",bulkDtoList.get(qualityIndex).getBulkSmdp());
        hashMap.put("bulkActiveCode",bulkDtoList.get(qualityIndex).getBulkActiveCode());
        hashMap.put("bulkIccid",bulkDtoList.get(qualityIndex).getBulkIccid());
        hashMap.put("bulkTitle",bulkDtoList.get(qualityIndex).getBulkTitle());
        hashMap.put("orderId",bulkDtoList.get(qualityIndex).getOrderId());
        hashMap.put("id",bulkDtoList.get(qualityIndex).getId());

        return hashMap;
    }


}
