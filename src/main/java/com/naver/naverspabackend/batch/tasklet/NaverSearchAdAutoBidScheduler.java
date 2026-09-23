package com.naver.naverspabackend.batch.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 네이버 검색광고 자동입찰 스케줄 (추정입찰 ±step, 10분).
 * - 파워링크 키워드: average-position-bid
 * - 쇼핑검색 소재: NPLA average-position-bid
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverSearchAdAutoBidScheduler {

    private final NaverSearchAdAutoBidJob autoBidJob;
    private final NaverSearchAdShoppingAutoBidJob shoppingAutoBidJob;

    /** 파워링크 키워드 — 매 10분 (0,10,20,30,40,50) */
    @Scheduled(cron = "0 0/10 * * * *")
    public void scheduleKeyword() {
        try {
            autoBidJob.run();
        } catch (Exception e) {
            log.error("NaverSearchAdAutoBidScheduler keyword job failed", e);
        }
    }

    /** 쇼핑검색 소재 — 매 10분 (5,15,25,35,45,55) API 겹침 완화 */
    @Scheduled(cron = "0 5/10 * * * *")
    public void scheduleShopping() {
        try {
            shoppingAutoBidJob.run();
        } catch (Exception e) {
            log.error("NaverSearchAdAutoBidScheduler shopping job failed", e);
        }
    }
}
