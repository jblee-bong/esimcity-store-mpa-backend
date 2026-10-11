package com.naver.naverspabackend.batch.tasklet;

import com.naver.naverspabackend.config.NaverSearchAdProperties;
import com.naver.naverspabackend.dto.SearchAdKeywordDto;
import com.naver.naverspabackend.service.searchad.SearchAdKeywordService;
import com.naver.naverspabackend.util.NaverSearchAdClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 파워링크 키워드 자동입찰 (1시간 주기 권장).
 * 목표순위 추정입찰가(average-position-bid) 방향으로 ±bidStep 조정.
 * 추정 API 순위는 1~4만 지원. avgRnk 는 참고 저장만.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverSearchAdAutoBidJob {

    private final NaverSearchAdProperties properties;
    private final NaverSearchAdClient client;
    private final SearchAdKeywordService searchAdKeywordService;

    public void run() {
        if (!properties.isEnabled()) {
            log.debug("naver-search-ad.enabled=false — skip auto bid job");
            return;
        }
        if (!StringUtils.hasText(properties.getCustomerId())
            || !StringUtils.hasText(properties.getAccessLicense())
            || !StringUtils.hasText(properties.getSecretKey())) {
            log.error("naver-search-ad credentials incomplete (customerId / accessLicense / secretKey)");
            return;
        }

        List<SearchAdKeywordDto> targets = searchAdKeywordService.findActiveList();
        if (targets == null || targets.isEmpty()) {
            log.warn("TB_SEARCH_AD_KEYWORD active list empty — nothing to adjust");
            return;
        }

        log.info("========== PowerLink Keyword AutoBid(estimate) START targetPos={} bidStep={} count={} ==========",
            properties.getTargetPosition(), properties.getBidStep(), targets.size());

        for (SearchAdKeywordDto target : targets) {
            try {
                adjustOne(target);
            } catch (Exception e) {
                log.error("auto bid failed keyword={} nccKeywordId={}",
                    target.getKeyword(), target.getNccKeywordId(), e);
            }
        }

        log.info("========== PowerLink Keyword AutoBid(estimate) END ==========");
    }

    private void adjustOne(SearchAdKeywordDto target) throws Exception {
        if (target == null) {
            return;
        }
        if (!StringUtils.hasText(target.getKeyword()) || !StringUtils.hasText(target.getNccKeywordId())) {
            log.warn("skip invalid target (need keyword + nccKeywordId): id={}", target.getId());
            return;
        }

        int targetPos = target.getTargetPosition() != null
            ? target.getTargetPosition()
            : properties.getTargetPosition();
        if (targetPos < 1 || targetPos > 4) {
            log.warn("powerlink estimate position must be 1~4, got {} for keyword={} — clamp",
                targetPos, target.getKeyword());
            targetPos = Math.min(4, Math.max(1, targetPos));
        }
        int minBid = target.getMinBid() != null ? target.getMinBid() : properties.getMinBid();
        int maxBid = target.getMaxBid() != null ? target.getMaxBid() : properties.getMaxBid();

        Map<String, Object> keyword = client.getKeyword(target.getNccKeywordId());
        int currentBid = toInt(keyword.get("bidAmt"), 0);

        Integer estimated = client.estimateAveragePositionBid(
            target.getKeyword(), targetPos, properties.getDevice());
        if (estimated == null || estimated <= 0) {
            log.warn("no estimate keyword={} pos={} — keep bid={}",
                target.getKeyword(), targetPos, currentBid);
            searchAdKeywordService.updateBidSnapshot(
                buildSnapshot(target.getId(), currentBid, null, null, null, null, null, null));
            return;
        }

        int estimateClamped = clamp(estimated, minBid, maxBid);
        int step = Math.max(10, properties.getBidStep());
        int tolerance = Math.max(0, properties.getBidTolerance());
        int appliedBid = currentBid;

        Double avgRnk = null;
        Integer lastImp = null;
        try {
            Map<String, Object> stats = client.getStatsByDatePreset(target.getNccKeywordId(), "today");
            avgRnk = toDouble(stats.get("avgRnk"));
            lastImp = toIntObj(stats.get("impCnt"));
        } catch (Exception e) {
            log.debug("optional today stats skip nccKeywordId={}", target.getNccKeywordId(), e);
        }

        int gap = estimateClamped - currentBid;
        if (Math.abs(gap) <= tolerance) {
            log.info("keep keyword bid nccKeywordId={} keyword={} pos={} current={} estimate={} (tolerance {})",
                target.getNccKeywordId(), target.getKeyword(), targetPos, currentBid, estimateClamped, tolerance);
        } else if (gap > 0) {
            int desired = clamp(currentBid + step, minBid, Math.min(maxBid, estimateClamped));
            if (desired != currentBid) {
                keyword.put("bidAmt", desired);
                keyword.put("useGroupBidAmt", 0);
                client.updateKeywordBid(keyword);
                appliedBid = desired;
                log.info("raise keyword bid(estimate) nccKeywordId={} keyword={} pos={} estimate={} {} -> {} (+{})",
                    target.getNccKeywordId(), target.getKeyword(), targetPos, estimateClamped,
                    currentBid, desired, step);
                searchAdKeywordService.insertBidHist(buildHist(
                    target, "RAISE", currentBid, desired, targetPos, avgRnk, null, lastImp, null));
            }
        } else {
            int desired = clamp(currentBid - step, Math.max(minBid, estimateClamped), maxBid);
            if (desired != currentBid) {
                keyword.put("bidAmt", desired);
                keyword.put("useGroupBidAmt", 0);
                client.updateKeywordBid(keyword);
                appliedBid = desired;
                log.info("lower keyword bid(estimate) nccKeywordId={} keyword={} pos={} estimate={} {} -> {} (-{})",
                    target.getNccKeywordId(), target.getKeyword(), targetPos, estimateClamped,
                    currentBid, desired, step);
                searchAdKeywordService.insertBidHist(buildHist(
                    target, "LOWER", currentBid, desired, targetPos, avgRnk, null, lastImp, null));
            }
        }

        searchAdKeywordService.updateBidSnapshot(
            buildSnapshot(target.getId(), appliedBid, estimated, avgRnk, lastImp,
                target.getPrevImpCnt(), target.getPrevAvgRnk(), null));
    }

    private static Map<String, Object> buildHist(SearchAdKeywordDto target, String action,
        int beforeBid, int afterBid, int targetPos, Double avgRnk, Double deltaRnk,
        Integer lastImpCnt, Integer deltaImp) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("keywordId", target.getId());
        param.put("nccKeywordId", target.getNccKeywordId());
        param.put("keyword", target.getKeyword());
        param.put("action", action);
        param.put("beforeBid", beforeBid);
        param.put("afterBid", afterBid);
        param.put("targetPosition", targetPos);
        param.put("avgRnk", avgRnk);
        param.put("deltaRnk", deltaRnk);
        param.put("lastImpCnt", lastImpCnt);
        param.put("deltaImp", deltaImp);
        return param;
    }

    private static Map<String, Object> buildSnapshot(Long id, Integer currentBid, Integer estimateBid,
        Double avgRnk, Integer lastImpCnt, Integer prevImpCnt, Double prevAvgRnk, Double deltaRnk) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("id", id);
        param.put("currentBid", currentBid);
        param.put("estimateBid", estimateBid);
        param.put("avgRnk", avgRnk);
        param.put("lastImpCnt", lastImpCnt);
        param.put("prevImpCnt", prevImpCnt);
        param.put("prevAvgRnk", prevAvgRnk);
        param.put("deltaRnk", deltaRnk);
        return param;
    }

    private static int clamp(int value, int min, int max) {
        if (min > max) {
            int t = min;
            min = max;
            max = t;
        }
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private static Integer toIntObj(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int toInt(Object value, int defaultValue) {
        Integer parsed = toIntObj(value);
        return parsed != null ? parsed : defaultValue;
    }
}
