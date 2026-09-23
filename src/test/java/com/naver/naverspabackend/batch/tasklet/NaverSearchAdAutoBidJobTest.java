package com.naver.naverspabackend.batch.tasklet;

import com.naver.naverspabackend.config.NaverSearchAdProperties;
import com.naver.naverspabackend.dto.SearchAdKeywordDto;
import com.naver.naverspabackend.dto.SearchAdShoppingDto;
import com.naver.naverspabackend.service.searchad.SearchAdKeywordService;
import com.naver.naverspabackend.service.searchad.SearchAdShoppingService;
import com.naver.naverspabackend.util.NaverSearchAdClient;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StringUtils;

/**
 * 검색광고 자동입찰 디버그용 (파워링크 + 쇼핑검색).
 *
 * 실행 전:
 * 1) application-cityProd.yml customer-id
 * 2) 파워링크: TB_SEARCH_AD_KEYWORD (nkw-...)  — delta
 * 3) 쇼핑검색: TB_SEARCH_AD_SHOPPING (nad-...) — delta
 *
 * 파워링크 추천 순서:
 * - printConfig
 * - printActiveKeywords
 * - debugFirstKeywordApi
 * - runAutoBidJob  (추정입찰 ±step)
 *
 * 쇼핑 추천 순서:
 * - printActiveShopping
 * - debugFirstShoppingApi
 * - debugStatReportShoppingKeyword
 * - runShoppingAutoBidJob
 */
@SpringBootTest
@ActiveProfiles("cityProd")
@TestPropertySource(properties = {
    "logging.config=classpath:logback-dev.xml"
})
public class NaverSearchAdAutoBidJobTest {

    private static final Logger log = LoggerFactory.getLogger(NaverSearchAdAutoBidJobTest.class);

    @Autowired
    private NaverSearchAdAutoBidJob autoBidJob;

    @Autowired
    private NaverSearchAdShoppingAutoBidJob shoppingAutoBidJob;

    @Autowired
    private NaverSearchAdProperties properties;

    @Autowired
    private SearchAdKeywordService searchAdKeywordService;

    @Autowired
    private SearchAdShoppingService searchAdShoppingService;

    @Autowired
    private NaverSearchAdClient client;

    @Test
    void printConfig() {
        log.info("==== naver-search-ad config ====");
        log.info("enabled={}", properties.isEnabled());
        log.info("baseUrl={}", properties.getBaseUrl());
        log.info("customerId={}", properties.getCustomerId());
        log.info("accessLicense set?={}", StringUtils.hasText(properties.getAccessLicense()));
        log.info("secretKey set?={}", StringUtils.hasText(properties.getSecretKey()));
        log.info("targetPosition={}", properties.getTargetPosition());
        log.info("device={}", properties.getDevice());
        log.info("minBid={}, maxBid={}, bidTolerance={}",
            properties.getMinBid(), properties.getMaxBid(), properties.getBidTolerance());
        log.info("rankTolerance={}, bidStep={}, minDeltaImp={}",
            properties.getRankTolerance(), properties.getBidStep(), properties.getMinDeltaImp());
    }

    @Test
    void printActiveKeywords() {
        List<SearchAdKeywordDto> list = searchAdKeywordService.findActiveList();
        log.info("==== active keywords count={} ====", list == null ? 0 : list.size());
        if (list == null) {
            return;
        }
        for (SearchAdKeywordDto row : list) {
            log.info("id={} keyword={} nccKeywordId={} pos={} currentBid={} estimateBid={} avgRnk={} lastImp={} lastChecked={}",
                row.getId(),
                row.getKeyword(),
                row.getNccKeywordId(),
                row.getTargetPosition(),
                row.getCurrentBid(),
                row.getEstimateBid(),
                row.getAvgRnk(),
                row.getLastImpCnt(),
                row.getLastCheckedDt());
        }
    }

    @Test
    void printActiveShopping() {
        List<SearchAdShoppingDto> list = searchAdShoppingService.findActiveList();
        log.info("==== active shopping ads count={} ====", list == null ? 0 : list.size());
        if (list == null) {
            return;
        }
        for (SearchAdShoppingDto row : list) {
            log.info("id={} adName={} nccAdId={} pos={} currentBid={} avgRnk={} deltaRnk={} lastImp={} prevImp={} lastChecked={}",
                row.getId(),
                row.getAdName(),
                row.getNccAdId(),
                row.getTargetPosition(),
                row.getCurrentBid(),
                row.getAvgRnk(),
                row.getDeltaRnk(),
                row.getLastImpCnt(),
                row.getPrevImpCnt(),
                row.getLastCheckedDt());
        }
    }

    @Test
    void debugFirstKeywordApi() throws Exception {
        assertCredentials();
        List<SearchAdKeywordDto> list = searchAdKeywordService.findActiveList();
        if (list == null || list.isEmpty()) {
            log.warn("활성 키워드 없음. TB_SEARCH_AD_KEYWORD 에 nkw-... 행을 넣으세요.");
            return;
        }
        SearchAdKeywordDto target = list.get(6);
        int position = target.getTargetPosition() != null
            ? target.getTargetPosition()
            : properties.getTargetPosition();
        if (position < 1 || position > 4) {
            position = Math.min(4, Math.max(1, position));
        }

        log.info(">>>> powerlink estimate keyword={} pos={} device={}",
            target.getKeyword(), position, properties.getDevice());
        Integer estimate = client.estimateAveragePositionBid(
            target.getKeyword(), position, properties.getDevice());
        log.info("<<<< estimateBid={}", estimate);

        log.info(">>>> getKeyword nccKeywordId={} keyword={} targetPos={}",
            target.getNccKeywordId(), target.getKeyword(), position);
        Map<String, Object> keyword = client.getKeyword(target.getNccKeywordId());
        log.info("<<<< keyword response={}", keyword);
        log.info("<<<< current bidAmt={}", keyword.get("bidAmt"));

        log.info(">>>> stats datePreset=today (참고용)");
        Map<String, Object> stats = client.getStatsByDatePreset(target.getNccKeywordId(), "today");
        log.info("<<<< stats={}", stats);
        log.info("<<<< avgRnk={} impCnt={}", stats.get("avgRnk"), stats.get("impCnt"));
    }

    /**
     * 쇼핑검색 소재 API만 조회 (입찰 변경 없음).
     */
    @Test
    void debugFirstShoppingApi() throws Exception {
        assertCredentials();

        List<SearchAdShoppingDto> list = searchAdShoppingService.findActiveList();
        if (list == null || list.isEmpty()) {
            log.warn("활성 쇼핑소재 없음. TB_SEARCH_AD_SHOPPING 에 nad-... 행을 넣으세요.");
            return;
        }

        SearchAdShoppingDto target = list.get(7);
        int position = target.getTargetPosition() != null
            ? target.getTargetPosition()
            : properties.getTargetPosition();

        log.info(">>>> npla estimate nccAdId={} position={} device={}",
            target.getNccAdId(), position, properties.getDevice());
        Integer estimate = client.estimateNplaAveragePositionBid(
            target.getNccAdId(), position, properties.getDevice());
        log.info("<<<< estimateBid={}", estimate);

        log.info(">>>> getAd nccAdId={}", target.getNccAdId());
        Map<String, Object> ad = client.getAd(target.getNccAdId());
        log.info("<<<< ad response={}", ad);
        log.info("<<<< current bidAmt={}", NaverSearchAdClient.extractAdBidAmt(ad));

        log.info(">>>> stats datePreset=today");
        Map<String, Object> stats = client.getStatsByDatePreset(target.getNccAdId(), "today");
        log.info("<<<< stats={}", stats);
        log.info("<<<< avgRnk={} impCnt={}", stats.get("avgRnk"), stats.get("impCnt"));

        Integer prevImp = target.getPrevImpCnt();
        Double prevAvg = target.getPrevAvgRnk();
        Integer impNow = stats.get("impCnt") instanceof Number
            ? ((Number) stats.get("impCnt")).intValue() : null;
        Double avgNow = stats.get("avgRnk") instanceof Number
            ? ((Number) stats.get("avgRnk")).doubleValue() : null;
        if (prevImp != null && prevAvg != null && impNow != null && avgNow != null
            && impNow > prevImp) {
            int deltaImp = impNow - prevImp;
            double deltaRnk = ((impNow * avgNow) - (prevImp * prevAvg)) / (double) deltaImp;
            log.info("<<<< sample delta Δimp={} deltaRnk={}", deltaImp, deltaRnk);
        } else {
            log.info("<<<< no delta yet (need prev snapshot from prior runShoppingAutoBidJob)");
        }
    }

    /**
     * POST /stat-reports reportTp=SHOPPINGKEYWORD_DETAIL (전일).
     * 계정 전체 쇼핑 검색어 성과 TSV — 생성에 수 분 걸릴 수 있음.
     */
    @Test
    @Timeout(300)
    void debugStatReportShoppingKeyword() throws Exception {
        assertCredentials();

        LocalDate statDt = LocalDate.now().minusDays(1);
        log.info(">>>> list existing /stat-reports");
        List<Map<String, Object>> existing = client.listStatReports();
        log.info("<<<< existing count={}", existing == null ? 0 : existing.size());
        if (existing != null) {
            int shown = 0;
            for (Map<String, Object> row : existing) {
                log.info("  job={}", row);
                if (++shown >= 5) {
                    break;
                }
            }
        }

        log.info(">>>> createStatReport SHOPPINGKEYWORD_DETAIL statDt={}", statDt);
        Map<String, Object> created = client.createStatReport("SHOPPINGKEYWORD_DETAIL", statDt.toString());
        log.info("<<<< created={}", created);

        Object jobIdObj = created.get("reportJobId");
        if (jobIdObj == null) {
            jobIdObj = created.get("id");
        }
        if (jobIdObj == null) {
            log.warn("reportJobId 없음 — 응답만 확인하세요: {}", created);
            return;
        }
        String jobId = String.valueOf(jobIdObj);
        log.info(">>>> waitAndReadStatReport jobId={}", jobId);
        String preview = client.waitAndReadStatReport(jobId, 240_000L);
        log.info("==== SHOPPINGKEYWORD_DETAIL preview (statDt={}) ====\n{}", statDt, preview);

        List<SearchAdShoppingDto> ads = searchAdShoppingService.findActiveList();
        if (ads != null) {
            for (SearchAdShoppingDto ad : ads) {
                if (!StringUtils.hasText(ad.getNccAdId())) {
                    continue;
                }
                long hits = preview.lines().filter(line -> line.contains(ad.getNccAdId())).count();
                log.info("<<<< filter nccAdId={} name={} matchingLines={}",
                    ad.getNccAdId(), ad.getAdName(), hits);
            }
        }
    }

    /**
     * GET /stats?id={nad}&statType=NPLA_SCH_KEYWORD — 소재별 유입 검색어 (전일, 즉시).
     */
    @Test
    void debugShoppingKeywordStats() throws Exception {
        assertCredentials();
        List<SearchAdShoppingDto> list = searchAdShoppingService.findActiveList();
        if (list == null || list.isEmpty()) {
            log.warn("활성 쇼핑소재 없음");
            return;
        }
        LocalDate until = LocalDate.now().minusDays(1);
        LocalDate since = until;
        for (SearchAdShoppingDto target : list) {
            log.info(">>>> NPLA_SCH_KEYWORD nccAdId={} name={} since={} until={}",
                target.getNccAdId(), target.getAdName(), since, until);
            List<Map<String, Object>> rows = client.getShoppingKeywordStats(
                target.getNccAdId(), since.toString(), until.toString());
            log.info("<<<< rows={}", rows == null ? 0 : rows.size());
            if (rows != null) {
                int n = 0;
                for (Map<String, Object> row : rows) {
                    log.info("  keywordStat={}", row);
                    if (++n >= 30) {
                        log.info("  ... truncated");
                        break;
                    }
                }
            }
        }
    }

    @Test
    void runAutoBidJob() {
        assertCredentials();
        boolean original = properties.isEnabled();
        properties.setEnabled(true);
        try {
            log.info(">>>> autoBidJob.run() start");
            autoBidJob.run();
            log.info("<<<< autoBidJob.run() end");
        } finally {
            properties.setEnabled(original);
        }
        printActiveKeywords();
    }

    @Test
    void runShoppingAutoBidJob() {
        assertCredentials();
        boolean original = properties.isEnabled();
        properties.setEnabled(true);
        try {
            log.info(">>>> shoppingAutoBidJob.run() start");
            shoppingAutoBidJob.run();
            log.info("<<<< shoppingAutoBidJob.run() end");
        } finally {
            properties.setEnabled(original);
        }
        printActiveShopping();
    }

    /** 스케줄러와 동일: 키워드 → 쇼핑 순 */
    @Test
    void runAllAutoBidJobs() {
        assertCredentials();
        boolean original = properties.isEnabled();
        properties.setEnabled(true);
        try {
            autoBidJob.run();
            shoppingAutoBidJob.run();
        } finally {
            properties.setEnabled(original);
        }
        printActiveKeywords();
        printActiveShopping();
    }

    private void assertCredentials() {
        if (!StringUtils.hasText(properties.getCustomerId())) {
            throw new IllegalStateException(
                "customer-id 가 비어 있습니다. application-cityProd.yml 의 naver-search-ad.customer-id 를 넣으세요.");
        }
        if (!StringUtils.hasText(properties.getAccessLicense())
            || !StringUtils.hasText(properties.getSecretKey())) {
            throw new IllegalStateException("access-license / secret-key 가 비어 있습니다.");
        }
    }
}
