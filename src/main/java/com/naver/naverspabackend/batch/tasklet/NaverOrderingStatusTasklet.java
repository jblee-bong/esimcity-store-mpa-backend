package com.naver.naverspabackend.batch.tasklet;


import com.google.gson.Gson;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.CommonUtil;
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

import java.time.temporal.ChronoUnit;
import java.util.*;


/**
 * 스마트 스토어 발주처리 조회
 *
 * @author gil
 */
@Slf4j
@Component
@StepScope
public class NaverOrderingStatusTasklet implements Tasklet {


    @Autowired
    NaverOrderingStatus naverOrderingStaus;


    @Autowired
    CoupnagOrderingStatus coupangOrderingStaus;

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
        log.error("*************************************************** 발주처리 3 ***************************************************");

        Map<String,Object> param = new HashMap<>();
        param.put("userAuthority","ROLE_ADMIN");
        List<StoreDto> storeDtos = storeMapper.selectStoreList(param);


        for (StoreDto storeDto : storeDtos) {

            if(storeDto.getPlatform()==null){
                log.warn("플랫폼이 설정되지 않은 스토어: Store ID: {}, Store Name: {}", storeDto.getId(), storeDto.getStoreName());
            }else if(storeDto.getPlatform().equals("naver")){
                naverOrderingStaus.processNaverStore(storeDto);
            }else if(storeDto.getPlatform().equals("coupang")){
                coupangOrderingStaus.processCoupangStore(storeDto);
            }else{
                log.warn("지원하지 않는 플랫폼: {} - Store ID: {}, Store Name: {}", storeDto.getPlatform(), storeDto.getId(), storeDto.getStoreName());
            }


        }

        return RepeatStatus.FINISHED;
    }

}