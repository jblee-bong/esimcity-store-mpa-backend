package com.naver.naverspabackend.batch.tasklet;


import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.security.TugeRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * esim api의 상품 목록 조회화여 db 저장
 *
 * @author jblee
 */



@Slf4j
@Component
public class NaverDeceidedScheduler {


    @Autowired
    NaverDeceided naverDeceided;



    @Autowired
    private StoreMapper storeMapper;


    @Value("${testmode}")
    private String testMode;

    @Autowired
    private TugeRedisRepository tugeRedisRepository;
    @Scheduled(cron = "0 30 11,18 * * *")
    public void NaverDeceidedScheduler () {
        if(testMode.equals("true"))
        {
            return ;
        }
        log.error("*************************************************** 확정문자 전송 ***************************************************");
        Map<String,Object> param = new HashMap<>();
        param.put("userAuthority","ROLE_ADMIN");
        List<StoreDto> storeDtos = storeMapper.selectStoreList(param);
        for (StoreDto storeDto : storeDtos) {
            if(storeDto.getPlatform()==null){
                log.warn("플랫폼이 설정되지 않은 스토어: Store ID: {}, Store Name: {}", storeDto.getId(), storeDto.getStoreName());
            }else if(storeDto.getPlatform().equals("naver")){
                naverDeceided.processNaverStore(storeDto);
            }else if(storeDto.getPlatform().equals("coupang")){
                //coupnagChangeStatus.processCoupangStore(storeDto);
            }else{
                log.warn("지원하지 않는 플랫폼: {} - Store ID: {}, Store Name: {}", storeDto.getPlatform(), storeDto.getId(), storeDto.getStoreName());
            }
        }
    }


}