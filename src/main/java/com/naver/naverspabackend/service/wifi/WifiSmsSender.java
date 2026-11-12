package com.naver.naverspabackend.service.wifi;

import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.service.sms.SmsService;
import com.naver.naverspabackend.service.store.StoreService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 기존 NHN SMS 발송(SmsService) 재사용. 발신번호/키는 wifi.smsStoreId 스토어 설정을 사용 */
@Slf4j
@Component
public class WifiSmsSender {

    @Autowired
    private SmsService smsService;
    @Autowired
    private StoreService storeService;

    @Value("${wifi.smsStoreId:}")
    private String smsStoreId;
    @Value("${wifi.adminTel:}")
    private String adminTel;

    /** 실패해도 예외를 던지지 않음 (알림용) */
    public boolean send(String tel, String body) {
        try {
            sendOrThrow(tel, body);
            return true;
        } catch (Exception e) {
            log.error("[WIFI-SMS] send fail: {}", e.getMessage());
            return false;
        }
    }

    /** SMS 90바이트(한글 45자) 이내로 보낼 것 */
    public void sendOrThrow(String tel, String body) throws Exception {
        if (StringUtils.isBlank(smsStoreId)) throw new IllegalStateException("wifi.smsStoreId 설정이 없습니다.");
        Map<String, Object> q = new HashMap<>();
        q.put("id", Long.valueOf(smsStoreId));
        StoreDto store = storeService.findById(q);
        if (store == null) throw new IllegalStateException("SMS 발송 스토어를 찾을 수 없습니다.");
        Map<String, Object> m = new HashMap<>();
        m.put("smsBody", body);
        m.put("shippingTel1", tel);
        m.put("storeId", store.getId());
        smsService.insertSms(m, store);
    }

    public void alertAdmin(String body) {
        log.warn("[WIFI-ALERT] {}", body);
        if (StringUtils.isBlank(adminTel)) return;
        for (String t : adminTel.split(",")) send(t.trim(), "[와이파이] " + body);
    }
}
