package com.naver.naverspabackend.batch.tasklet;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.batch.writer.NaverWritter;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.ProductMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.CommonUtil;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * 스마트 스토어 item select 상품 목록 조회
 *
 * @author gil
 */
@Slf4j
@Component
@StepScope
public class NaverOrderTasklet implements Tasklet {

/*    *//**
     * quartz context에서 전달한 값으로 StepScope에 의해 해당 reader가 초기화 될때 세팅된다.
     *//*
    @Value("#{jobParameters[jobTimeKey]}")
    private String jobTimeKey;*/

    @Autowired
    private NaverOrder naverOrder;


    @Autowired
    private CoupangOrder coupangOrder;

    @Autowired
    private StoreMapper storeMapper;


    @Value("${testmode}")
    private String testMode;
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        if(testMode.equals("true"))
        {
            return null;
        }
        log.error("*************************************************** 주문가져오기 2 ***************************************************");

        Map<String,Object> param = new HashMap<>();
        param.put("userAuthority","ROLE_ADMIN");
        List<StoreDto> storeDtos = storeMapper.selectStoreList(param);

        for (StoreDto storeDto : storeDtos) {

            if(storeDto.getPlatform()==null){
                log.warn("플랫폼이 설정되지 않은 스토어: Store ID: {}, Store Name: {}", storeDto.getId(), storeDto.getStoreName());
            }else if(storeDto.getPlatform().equals("naver")){
                naverOrder.processNaverStore(storeDto);
            }else if(storeDto.getPlatform().equals("coupang")){
                coupangOrder.processCoupangStore(storeDto);
            }else{
                log.warn("지원하지 않는 플랫폼: {} - Store ID: {}, Store Name: {}", storeDto.getPlatform(), storeDto.getId(), storeDto.getStoreName());
            }


        }
        return RepeatStatus.FINISHED;
    }


}