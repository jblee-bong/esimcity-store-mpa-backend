package com.naver.naverspabackend.batch.tasklet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.batch.writer.NaverWritter;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.ProductMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.CommonUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import okhttp3.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@SpringBootTest
class NaverOrderTaskletTest {

    @Autowired
    private NaverSetting naverSetting;

    @Autowired
    private NaverWritter naverWritter;

    @Autowired
    private NaverRedisRepository naverRedisRepository;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private ProductMapper productMapper;

    @Value("${naver-api.base}")
    private String baseUrl;

    @Value("${naver-api.order-list-info}")
    private String orderListInfoUrl;

    @Value("${naver-api.order-detail-info}")
    private String orderDetailInfoUrl;

    private final int LIMIT_SIZE = 300;

    private final int DIFF_TIME = -1440;

    @Test
    void execute() throws JsonProcessingException, UnsupportedEncodingException {
        Map<String,Object> param = new HashMap<>();
        param.put("userAuthority","ROLE_ADMIN");
        List<StoreDto> storeDtos = storeMapper.selectStoreList(param);


        for (StoreDto storeDto : storeDtos) {
            Optional<NaverRedisToken> byStoreId = naverRedisRepository.findById(storeDto.getId());

            if (!byStoreId.isPresent()){
                return ;
            }

            NaverRedisToken naverRedisToken = byStoreId.get();

            Map<String, String> headerMap = naverRedisToken.returnHeaderMap();

            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("lastChangedFrom", URLEncoder.encode(CommonUtil.naverFormatDate(DIFF_TIME, ChronoUnit.MINUTES), StandardCharsets.UTF_8.name()));
            paramMap.put("lastChangedType", "PAYED");

            Map<String, Object> resMap = new HashMap<>();

            try {
                resMap = requestOrderNoList(headerMap, paramMap);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                continue;
            }

            // 최종 입력 될 product 상품
            List<OrderDto> orderDtoList = new ArrayList<>();

            if( resMap.get("data") != null) {

                Map<String, Object> moreMap = (Map<String, Object>) ((Map) resMap.get("data")).get("more");
                List<Map<String, Object>> orderListMap = (List<Map<String, Object>>) ((Map) resMap.get("data")).get("lastChangeStatuses");

                while (moreMap != null && moreMap.get("moreFrom") != null) {
                    paramMap.put("lastChangedFrom", URLEncoder.encode((String) moreMap.get("moreFrom"), StandardCharsets.UTF_8.name()));
                    paramMap.put("moreSequence", moreMap.get("moreSequence"));

                    moreMap = requestOrderNoList(headerMap, paramMap);

                    List<Map<String, Object>> moreOrderListMap = (List<Map<String, Object>>) ((Map) resMap.get("data")).get("lastChangeStatuses");

                    orderListMap.addAll(moreOrderListMap);
                }

                List<String> productOrderIds = new ArrayList<>();

                productOrderIds = orderListMap.stream().map(e -> e.get("productOrderId") + "").collect(Collectors.toList());

                List<Map<String, Object>> orderDetailList = requestOrderDetailList(productOrderIds, headerMap);

                orderDtoList = buildOrderDtoList(orderDetailList, storeDto);
            }

           // naverWritter.orderWrite(orderDtoList);

        }
    }

    public Map<String, Object> requestOrderNoList(Map<String, String> headerMap, Map<String, Object> paramMap) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String res = ApiUtil.get(baseUrl + orderListInfoUrl, paramMap, headerMap, true);
            return objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});
        }catch (Exception e){
            throw e;
        }
    }

    public List<Map<String, Object>> requestOrderDetailList(List<String> productOrderIds, Map<String, String> headerMap) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> paramMap = new HashMap<>();

        int repeat = productOrderIds.size() % LIMIT_SIZE != 0 ? productOrderIds.size()  / LIMIT_SIZE + 1 : productOrderIds.size()  / LIMIT_SIZE;

        List<List<String>> divicideList = new ArrayList<>();

        for(int i = 0; i < repeat; i++){

            // i 가 마지막이 아니라면
            if (i != (repeat - 1)) {
                divicideList.add(productOrderIds.subList(i * LIMIT_SIZE, (i + 1) * LIMIT_SIZE));

                // i 가 마지막 이라면
            } else {
                divicideList.add(productOrderIds.subList(i * LIMIT_SIZE, productOrderIds.size()));
            }
        }
        List resultList = new ArrayList();

        for (int i = 0; i < divicideList.size(); i++) {

            List<String> productOrderIds1 = divicideList.get(i);

            paramMap.put("productOrderIds", productOrderIds1);
            try {
                String res = ApiUtil.post(baseUrl + orderDetailInfoUrl, headerMap, paramMap, null, MediaType.parse("application/json; charset=UTF-8"));
                Map<String, Object> stringObjectMap = objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {
                });
                resultList.addAll((List) stringObjectMap.get("data"));
            }catch (Exception e){
                throw e;
            }
        }

        return resultList;
    }

    public List<OrderDto> buildOrderDtoList(List<Map<String, Object>> orderDetailList, StoreDto storeDto){
        List<OrderDto> orderDtoList = new ArrayList<>();

        for (Map<String, Object> objectMap : orderDetailList) {
            OrderDto orderDto = new OrderDto();

            Map<String, Object> productOrder = (Map<String, Object>) objectMap.get("productOrder");
            Map<String, Object> order = (Map<String, Object>) objectMap.get("order");

            String productName = Objects.toString(productOrder.get("productName"), "");
            String productOption = Objects.toString(productOrder.get("productOption"), "");
            String originalProductId = Objects.toString(productOrder.get("originalProductId"), "");
            String optionCode = Objects.toString(productOrder.get("optionCode"), "");
            String productOrderId = Objects.toString(productOrder.get("productOrderId"), "");

            orderDto.setProductName(productName);
            orderDto.setProductOption(productOption);
            orderDto.setOriginProductNo(Long.valueOf(originalProductId));
            orderDto.setProductOrderId(productOrderId);
            orderDto.setOptionId(Long.valueOf(optionCode));

            String ordererId = Objects.toString(order.get("ordererId"), "");
            String ordererName = Objects.toString(order.get("ordererName"), "");
            String orderId = Objects.toString(order.get("orderId"), "");
            String paymentDate = Objects.toString(order.get("paymentDate"), "");
            String ordererNo = Objects.toString(order.get("ordererNo"), "");
            String ordererTel = Objects.toString(order.get("ordererTel"), "");

            orderDto.setOrdererId(ordererId);
            orderDto.setOrdererName(ordererName);
            orderDto.setOrderId(orderId);

            if(!"".equals(paymentDate)){
                orderDto.setPaymentDate(CommonUtil.parseNaverFormatDate(paymentDate));
            }
            orderDto.setOrdererNo(ordererNo);
            orderDto.setOrdererTel(ordererTel);

            orderDto.setStoreId(storeDto.getId());

            orderDto.setInsertFlag(true);

            orderDtoList.add(orderDto);
        }

        return orderDtoList;
    }

    @Test
    public void test(){
        System.out.println((new Date()).getTime());
    }
}