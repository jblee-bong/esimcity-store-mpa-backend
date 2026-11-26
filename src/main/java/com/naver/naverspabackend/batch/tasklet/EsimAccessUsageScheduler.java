package com.naver.naverspabackend.batch.tasklet;

import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.util.EsimAccessUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * esimaccess 데일리 상품 "오늘 사용량" 용 누적 사용량 이력.
 * API 는 누적값만 주고(lastUpdateTime 1시간 단위), 리셋은 activateTime + 24시간 × N 이라
 * 매시간 누적값을 쌓아두고 사용량 페이지에서 "현재 누적 - 리셋 직전 누적" 으로 계산.
 */
@Slf4j
@Component
public class EsimAccessUsageScheduler {

    private static final DateTimeFormatter API_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StoreService storeService;

    @Value("${testmode}")
    private String testMode;

    @Scheduled(cron = "0 15 * * * *")
    public void collect() {
        if ("true".equals(testMode)) return;

        // 스토어별 esimTranNo → orderId (한 주문에 여러 개면 "a,b"). 발송 180일 이내, 끝난 eSIM 제외
        Set<String> done = new HashSet<>(orderMapper.selectEsimAccessUsageDoneTranNos());
        Map<Long, Map<String, Long>> byStore = new HashMap<>();
        for (OrderDto o : orderMapper.selectEsimAccessDailyUsageTargets()) {
            for (String t : o.getEsimTranNo().split(",")) {
                if (!t.trim().isEmpty() && !done.contains(t.trim()))
                    byStore.computeIfAbsent(o.getStoreId(), k -> new LinkedHashMap<>()).put(t.trim(), o.getId());
            }
        }
        boolean checkStatus = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).getHour() == 4; // 하루 한 번 상태 확인

        int saved = 0;
        for (Map.Entry<Long, Map<String, Long>> e : byStore.entrySet()) {
            Map<String, Object> p = new HashMap<>();
            p.put("id", e.getKey());
            StoreDto store = storeService.findById(p);
            if (store == null || store.getEsimApiEsimaccessClientId() == null) continue;

            List<String> tranNos = new ArrayList<>(e.getValue().keySet());
            if (checkStatus) tranNos.removeIf(t -> markIfEnded(store.getEsimApiEsimaccessClientId(), t));
            for (int i = 0; i < tranNos.size(); i += 10) {
                try {
                    for (Map<String, Object> u : EsimAccessUtil.queryUsage(store.getEsimApiEsimaccessClientId(), tranNos.subList(i, Math.min(i + 10, tranNos.size())))) {
                        LocalDateTime updated = parseUtc(u.get("lastUpdateTime"));
                        if (updated == null || u.get("dataUsage") == null) continue; // 아직 설치/사용 전
                        String tranNo = String.valueOf(u.get("esimTranNo"));
                        Map<String, Object> row = new HashMap<>();
                        row.put("esimTranNo", tranNo);
                        row.put("orderId", e.getValue().get(tranNo));
                        row.put("dataUsage", ((Number) u.get("dataUsage")).longValue());
                        row.put("totalData", u.get("totalData") == null ? null : ((Number) u.get("totalData")).longValue());
                        row.put("lastUpdateTime", updated);
                        orderMapper.insertEsimAccessUsageHist(row); // 같은 시각이면 insert ignore
                        saved++;
                    }
                    Thread.sleep(500); // 초당 8회 제한: 배치는 초당 2회 이하로, 나머지는 고객 페이지·주문 발송 몫
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception ex) {
                    log.error("[ESIMACCESS] usage query error store={}", e.getKey(), ex);
                }
            }
        }
        orderMapper.deleteOldEsimAccessUsageHist();
        log.info("[ESIMACCESS] usage hist collected {}", saved);
    }

    // USED_UP 은 데일리에선 "오늘 용량 소진"일 수 있어서 끝난 걸로 안 봄
    private static final Set<String> ENDED = new HashSet<>(Arrays.asList("USED_EXPIRED", "UNUSED_EXPIRED", "CANCEL", "REVOKED"));

    /** esimaccess 상태가 끝났으면 DONE 에 넣고 true. 조회 실패면 false (다음 날 다시 확인) */
    private boolean markIfEnded(String accessCode, String tranNo) {
        try {
            String status = EsimAccessUtil.queryStatus(accessCode, tranNo);
            Thread.sleep(500); // 초당 8회 제한
            if (status == null || !ENDED.contains(status)) return false;
            Map<String, Object> row = new HashMap<>();
            row.put("esimTranNo", tranNo);
            row.put("esimStatus", status);
            orderMapper.insertEsimAccessUsageDone(row);
            return true;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception ex) {
            log.error("[ESIMACCESS] status query error {}", tranNo, ex);
            return false;
        }
    }

    /** "2025-03-19T18:00:00+0000" → UTC LocalDateTime (DB 에 UTC 그대로 저장) */
    public static LocalDateTime parseUtc(Object v) {
        if (v == null || v.toString().isEmpty()) return null;
        try {
            return ZonedDateTime.parse(v.toString(), API_TIME).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }
}
