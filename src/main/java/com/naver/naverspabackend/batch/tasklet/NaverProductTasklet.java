package com.naver.naverspabackend.batch.tasklet;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.batch.writer.NaverWritter;
import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.ProductOptionDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.ProductMapper;
import com.naver.naverspabackend.mybatis.mapper.ProductOptionMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.security.BatchRedisRepository;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.SignatureGenerator;
import com.naver.naverspabackend.security.token.BatchRedisToken;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.util.ApiUtil;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;


/**
 * 스마트 스토어 item select 상품 목록 조회
 *
 * @author gil
 */
@Slf4j
@Component
@StepScope
public class NaverProductTasklet implements Tasklet {

    /**
     * quartz context에서 전달한 값으로 StepScope에 의해 해당 reader가 초기화 될때 세팅된다.
     */
    @Value("#{jobParameters[jobTimeKey]}")
    private String jobTimeKey;

    @Autowired
    private NaverSetting naverSetting;

    @Autowired
    private NaverProduct naverProduct;


    @Autowired
    private CoupangProduct coupangProduct;

    @Autowired
    private StoreMapper storeMapper;


    @Autowired
    private BatchRedisRepository batchRedisRepository;

    @Value("${spring.profiles.active}")
    private String active;

    @Value("${testmode}")
    private String testMode;
    final int AFTER_MINITURE = 60;//60분마다 실행
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.error("*************************************************** STEP START 0 ***************************************************");
        if(testMode.equals("true"))
        {
            return null;
        }



        // 인증정보 redis 저장
        naverSetting.setting(jobTimeKey);

        Date now = new Date();
        String batchId = active+1;
        try{

            Optional<BatchRedisToken> byBatchId = batchRedisRepository.findById(batchId);
            if(byBatchId.isPresent()){
                //그냥존재하면 무조건 안함. 존재안할시만하는걸로 수정//나중에 서버 좋아지면 해제
                if(true)
                    return RepeatStatus.FINISHED;

                // 1. 필요한 상수 정의: 1시간 = 3,600,000 밀리초
                final long ONE_HOUR_IN_MILLIS = AFTER_MINITURE * 60 * 1000L;
                // 2. 현재 시간 (밀리초 단위)을 얻습니다.
                long currentTimeMillis = now.getTime();
                BatchRedisToken batchRedisToken = byBatchId.orElse(null);
                Long timeStamp = batchRedisToken.getTimeStamp();
                // timeStamp가 null이 아닌지 확인
                if (timeStamp != null) {

                    // 3. 만료 기준 시점 계산: 현재 시각에서 1시간을 뺀 시간
                    long expiryThreshold = currentTimeMillis - ONE_HOUR_IN_MILLIS;
                    // 4. 비교: 저장된 timeStamp가 만료 기준 시점보다 작으면 (더 과거이면) 만료!
                    if (timeStamp < expiryThreshold) {
                        saveBatchInfo(batchId);
                    }else{
                        return RepeatStatus.FINISHED;
                    }
                }else{
                    saveBatchInfo(batchId);
                }
            }else{
                saveBatchInfo(batchId);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        log.error("*************************************************** 상품 동기화 1 ***************************************************");



        Map<String,Object> param = new HashMap<>();
        param.put("userAuthority","ROLE_ADMIN");
        List<StoreDto> storeDtos = storeMapper.selectStoreList(param);
        for (StoreDto storeDto : storeDtos) {
            if(storeDto.getPlatform()==null){
                log.warn("플랫폼이 설정되지 않은 스토어: Store ID: {}, Store Name: {}", storeDto.getId(), storeDto.getStoreName());
            }else if(storeDto.getPlatform().equals("naver")){
                naverProduct.processNaverStore(storeDto);
            }else if(storeDto.getPlatform().equals("coupang")){
                coupangProduct.processCoupangStore(storeDto);
            }else{
                log.warn("지원하지 않는 플랫폼: {} - Store ID: {}, Store Name: {}", storeDto.getPlatform(), storeDto.getId(), storeDto.getStoreName());
            }
        }
        return RepeatStatus.FINISHED;
    }

    public void saveBatchInfo(String batchId){
        Date now = new Date();
        Long timeStamp = now.getTime();
        BatchRedisToken batchRedisToken = new BatchRedisToken();
        batchRedisToken.setBatchId(batchId);
        batchRedisToken.setTimeStamp(timeStamp);
        batchRedisRepository.save(batchRedisToken);
    }


}