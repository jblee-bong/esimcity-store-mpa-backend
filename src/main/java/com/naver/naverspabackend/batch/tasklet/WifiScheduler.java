package com.naver.naverspabackend.batch.tasklet;

import com.naver.naverspabackend.dto.WifiOrderDto;
import com.naver.naverspabackend.mybatis.mapper.WifiMapper;
import com.naver.naverspabackend.service.wifi.WifiAdminService;
import com.naver.naverspabackend.service.wifi.WifiPlanChecker;
import com.naver.naverspabackend.service.wifi.WifiService;
import com.naver.naverspabackend.service.wifi.WifiSmsSender;
import com.naver.naverspabackend.util.TugeDeviceUtil;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 포켓와이파이 배치. api.tugeDevice 설정이 없으면 아무것도 하지 않음 */
@Slf4j
@Component
public class WifiScheduler {

    @Autowired
    private TugeDeviceUtil tuge;
    @Autowired
    private WifiAdminService adminService;
    @Autowired
    private WifiService wifiService;
    @Autowired
    private WifiMapper wifiMapper;
    @Autowired
    private WifiSmsSender smsSender;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private WifiPlanChecker planChecker;

    @Value("${wifi.balanceAlertUsd:100}")
    private double balanceAlertUsd;
    @Value("${wifi.balanceAccountType:BASIC}")
    private String balanceAccountType;

    /** 결제 완료 후 TUGE 주문이 일시 실패한 건 재시도 (같은 requestId → 중복 주문 없음) */
    @Scheduled(initialDelay = 60_000, fixedDelay = 60_000)
    public void retryOrders() {
        // 키 미설정이어도 실행: 대기 주문이 재시도 한도 후 실패·환불되도록 (멈춰 있지 않게)
        for (WifiOrderDto o : wifiMapper.retryTargetOrders()) {
            try {
                wifiService.fulfill(o);
            } catch (Exception e) {
                log.error("[WIFI] retry error {}", o.getOrderNo(), e);
            }
        }
    }

    @Scheduled(cron = "0 10 1 * * *")
    public void syncProducts() {
        if (!tuge.isConfigured()) return;
        try {
            adminService.syncProducts();
        } catch (Exception e) {
            log.error("[WIFI] product sync error", e);
            smsSender.alertAdmin(WifiSmsSender.WF_ADM_SYNC_FAIL, WifiSmsSender.params("message", String.valueOf(e.getMessage())), "상품 동기화 실패: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 30 1 * * *")
    public void syncDevices() {
        if (!tuge.isConfigured()) return;
        try {
            adminService.syncDevices();
        } catch (Exception e) {
            log.error("[WIFI] device sync error", e);
        }
    }

    /** 요금제 기간·상태 갱신(관리자 화면). 낮(10~19시)에는 내일 끝나는 요금제 알림도 (한 번만) */
    @Scheduled(cron = "0 20 * * * *", zone = "Asia/Seoul")
    public void checkPlans() {
        if (!tuge.isConfigured()) return;
        int hour = java.time.LocalTime.now(java.time.ZoneId.of("Asia/Seoul")).getHour();
        try {
            planChecker.run(hour >= 10 && hour < 20);
        } catch (Exception e) {
            log.error("[WIFI] plan check error", e);
        }
    }

    @Scheduled(cron = "0 30 0 * * *")
    public void expirePoints() {
        try {
            adminService.expirePoints();
        } catch (Exception e) {
            log.error("[WIFI] point expire error", e);
        }
    }

    /** 채널 잔액 부족하면 3.4 주문이 1014 로 실패 → 미리 알림 (6시간에 1번) */
    @Scheduled(cron = "0 5 * * * *")
    public void checkBalance() {
        if (!tuge.isConfigured()) return;
        Map<String, Object> res = tuge.getBalance();
        if (!TugeDeviceUtil.isOk(res) || !(res.get("data") instanceof Map)) return;
        Object list = ((Map<?, ?>) res.get("data")).get("accountList");
        if (!(list instanceof Collection)) return;
        for (Object o : (Collection<?>) list) {
            if (!(o instanceof Map)) continue;
            Map<?, ?> a = (Map<?, ?>) o;
            if (!balanceAccountType.equals(a.get("type")) || !(a.get("balance") instanceof Number)) continue;
            double balance = ((Number) a.get("balance")).doubleValue();
            if (balance < balanceAlertUsd) {
                Boolean first = redisTemplate.opsForValue().setIfAbsent("wifi:alert:lowbalance", "1", 6, TimeUnit.HOURS);
                if (Boolean.TRUE.equals(first)) smsSender.alertAdmin(WifiSmsSender.WF_ADM_BALANCE, WifiSmsSender.params("balance", String.valueOf(balance)), "TUGE 채널 잔액 " + balance + " USD. 충전 필요");
            }
        }
    }
}
