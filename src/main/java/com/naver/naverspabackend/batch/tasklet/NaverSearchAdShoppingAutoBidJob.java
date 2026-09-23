package com.naver.naverspabackend.batch.tasklet;

import com.naver.naverspabackend.config.NaverSearchAdProperties;
import com.naver.naverspabackend.dto.SearchAdShoppingDto;
import com.naver.naverspabackend.service.searchad.SearchAdShoppingService;
import com.naver.naverspabackend.util.NaverSearchAdClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 쇼핑검색 소재 자동입찰 (1시간 주기 권장).
 * NPLA 목표순위 추정입찰가 방향으로 ±bidStep 조정 (min/max·bidTolerance 가드).
 * avgRnk 는 참고용(today 통계)으로만 저장하고 입찰 판단에는 쓰지 않음.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverSearchAdShoppingAutoBidJob {

    private final NaverSearchAdProperties properties;
    private final NaverSearchAdClient client;
    private final SearchAdShoppingService searchAdShoppingService;

    public void run() {
        if (!properties.isEnabled()) {
            log.debug("naver-search-ad.enabled=false — skip shopping auto bid job");
            return;
        }
        if (!StringUtils.hasText(properties.getCustomerId())
            || !StringUtils.hasText(properties.getAccessLicense())
            || !StringUtils.hasText(properties.getSecretKey())) {
            log.error("naver-search-ad credentials incomplete — skip shopping job");
            return;
        }

        List<SearchAdShoppingDto> targets = searchAdShoppingService.findActiveList();
        if (targets == null || targets.isEmpty()) {
            log.warn("TB_SEARCH_AD_SHOPPING active list empty — nothing to adjust");
            return;
        }

        log.info("========== Shopping Ad AutoBid(NPLA) START targetPos={} bidStep={} count={} ==========",
            properties.getTargetPosition(), properties.getBidStep(), targets.size());

        for (SearchAdShoppingDto target : targets) {
            try {
                adjustOne(target);
            } catch (Exception e) {
                log.error("shopping auto bid failed nccAdId={} name={}",
                    target.getNccAdId(), target.getAdName(), e);
            }
        }

        log.info("========== Shopping Ad AutoBid(NPLA) END ==========");
    }

    private void adjustOne(SearchAdShoppingDto target) throws Exception {
        if (target == null || !StringUtils.hasText(target.getNccAdId())) {
            return;
        }

        int targetPos = target.getTargetPosition() != null
            ? target.getTargetPosition()
            : properties.getTargetPosition();
        int minBid = target.getMinBid() != null ? target.getMinBid() : properties.getMinBid();
        int maxBid = target.getMaxBid() != null ? target.getMaxBid() : properties.getMaxBid();

        Map<String, Object> ad = client.getAd(target.getNccAdId());
        int currentBid = NaverSearchAdClient.extractAdBidAmt(ad);

        Integer estimated = client.estimateNplaAveragePositionBid(
            target.getNccAdId(), targetPos, properties.getDevice());
        if (estimated == null || estimated <= 0) {
            log.warn("no NPLA estimate nccAdId={} pos={} — keep bid={}",
                target.getNccAdId(), targetPos, currentBid);
            searchAdShoppingService.updateBidSnapshot(
                buildSnapshot(target.getId(), currentBid, null, null, null, null, null, null));
            return;
        }

        int estimateClamped = clamp(estimated, minBid, maxBid);
        int step = Math.max(10, properties.getBidStep());
        int tolerance = Math.max(0, properties.getBidTolerance());
        int appliedBid = currentBid;

        // 참고용 today 통계 (입찰 판단 X)
        Double avgRnk = null;
        Integer lastImp = null;
        try {
            Map<String, Object> stats = client.getStatsByDatePreset(target.getNccAdId(), "today");
            avgRnk = toDouble(stats.get("avgRnk"));
            lastImp = toIntObj(stats.get("impCnt"));
        } catch (Exception e) {
            log.debug("optional today stats skip nccAdId={}", target.getNccAdId(), e);
        }

        int gap = estimateClamped - currentBid;
        if (Math.abs(gap) <= tolerance) {
            log.info("keep shopping bid nccAdId={} pos={} current={} estimate={} (within tolerance {})",
                target.getNccAdId(), targetPos, currentBid, estimateClamped, tolerance);
        } else if (gap > 0) {
            int desired = clamp(currentBid + step, minBid, Math.min(maxBid, estimateClamped));
            if (desired != currentBid) {
                client.updateAdBid(target.getNccAdId(), desired);
                appliedBid = desired;
                log.info("raise shopping bid(NPLA) nccAdId={} pos={} estimate={} {} -> {} (+{})",
                    target.getNccAdId(), targetPos, estimateClamped, currentBid, desired, step);
                searchAdShoppingService.insertBidHist(buildHist(
                    target, "RAISE", currentBid, desired, targetPos, avgRnk, null, lastImp, null));
            }
        } else {
            int desired = clamp(currentBid - step, Math.max(minBid, estimateClamped), maxBid);
            if (desired != currentBid) {
                client.updateAdBid(target.getNccAdId(), desired);
                appliedBid = desired;
                log.info("lower shopping bid(NPLA) nccAdId={} pos={} estimate={} {} -> {} (-{})",
                    target.getNccAdId(), targetPos, estimateClamped, currentBid, desired, step);
                searchAdShoppingService.insertBidHist(buildHist(
                    target, "LOWER", currentBid, desired, targetPos, avgRnk, null, lastImp, null));
            }
        }

        searchAdShoppingService.updateBidSnapshot(
            buildSnapshot(target.getId(), appliedBid, estimated, avgRnk, lastImp,
                target.getPrevImpCnt(), target.getPrevAvgRnk(), null));
    }

    private static Map<String, Object> buildHist(SearchAdShoppingDto target, String action,
        int beforeBid, int afterBid, int targetPos, Double avgRnk, Double deltaRnk,
        Integer lastImpCnt, Integer deltaImp) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("shoppingId", target.getId());
        param.put("nccAdId", target.getNccAdId());
        param.put("adName", target.getAdName());
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
}
