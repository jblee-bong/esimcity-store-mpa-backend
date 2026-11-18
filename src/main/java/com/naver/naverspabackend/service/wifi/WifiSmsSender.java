package com.naver.naverspabackend.service.wifi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.dto.WifiDeviceDto;
import com.naver.naverspabackend.mybatis.mapper.WifiMapper;
import com.naver.naverspabackend.service.sms.SmsService;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.util.ApiUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 포켓와이파이 고객·관리자 알림. 카카오 알림톡(NHN, 승인 템플릿 WF_*) → 실패하면 같은 내용 SMS.
 * 발신 키·번호는 wifi.smsStoreId 스토어 설정(기존 eSIM 알림톡과 같은 NHN 계정).
 * 템플릿 원문: private-document2/이심시티/_docs/포켓와이파이/알림톡_템플릿.md (변수 이름을 바꾸면 안 됨)
 */
@Slf4j
@Component
public class WifiSmsSender {

    public static final String WF_OTP = "WF_OTP";
    public static final String WF_TOPUP_DONE = "WF_TOPUP_DONE";
    public static final String WF_TOPUP_FAIL = "WF_TOPUP_FAIL";
    public static final String WF_EXPIRE_SOON = "WF_EXPIRE_SOON";
    public static final String WF_REFUND_REQ = "WF_REFUND_REQ";
    public static final String WF_REFUND_DONE = "WF_REFUND_DONE";
    public static final String WF_REFUND_REJECT = "WF_REFUND_REJECT";
    public static final String WF_POINT_EXPIRE = "WF_POINT_EXPIRE";
    public static final String WF_ADM_TOPUP_FAIL = "WF_ADM_TOPUP_FAIL";
    public static final String WF_ADM_CANCEL_FAIL = "WF_ADM_CANCEL_FAIL";
    public static final String WF_ADM_AMOUNT = "WF_ADM_AMOUNT";
    public static final String WF_ADM_REFUND_REQ = "WF_ADM_REFUND_REQ";
    public static final String WF_ADM_SYNC_FAIL = "WF_ADM_SYNC_FAIL";
    public static final String WF_ADM_BALANCE = "WF_ADM_BALANCE";

    @Autowired
    private SmsService smsService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private WifiMapper wifiMapper;

    @Value("${wifi.smsStoreId:}")
    private String smsStoreId;
    @Value("${wifi.adminTel:}")
    private String adminTel;
    @Value("${wifi.smsEnabled:false}")
    private boolean smsEnabled;
    @Value("${wifi.otpSmsEnabled:false}")
    private boolean otpSmsEnabled;
    @Value("${wifi.deviceNames:}")   // 이름을 다르게 부르고 싶은 모델만 (예: T9=CityFi T9 Pro). 비우면 "CityFi " + 모델
    private String deviceNames;
    @Value("${kakao.api.domain:}")
    private String kakaoDomain;
    @Value("${kakao.api.messagesUrl:}")
    private String kakaoMessagesUrl;

    /** 고객 알림 (알림톡 → 실패 시 SMS). 실패해도 예외 없음. wifi.smsEnabled=false 면 로그만 */
    public boolean talk(String tel, String template, Map<String, String> params, String smsBody) {
        if (!smsEnabled) {
            log.info("[WIFI-TALK] 발송 꺼짐(wifi.smsEnabled=false) {} tel={} params={}", template, WifiAuthService.maskTel(tel), params);
            return true;
        }
        try {
            return deliver(tel, template, params, smsBody);
        } catch (Exception e) {
            log.error("[WIFI-TALK] {} 실패: {}", template, e.getMessage());
            return false;
        }
    }

    /** 로그인 인증번호. wifi.otpSmsEnabled=false 면 로그에만 남김 (shortcut: 테스트 기간용, 오픈 전 반드시 true) */
    public void sendOtp(String tel, String code) throws Exception {
        if (!otpSmsEnabled) {
            log.warn("[WIFI-SMS] 인증번호 문자 꺼짐(wifi.otpSmsEnabled=false) tel={} code={}", WifiAuthService.maskTel(tel), code);
            return;
        }
        if (!deliver(tel, WF_OTP, Collections.singletonMap("code", code), "[이심시티] 인증번호 [" + code + "]를 입력해주세요.")) {
            throw new IllegalStateException("인증번호 발송 실패");
        }
    }

    /** 관리자 알림 (wifi.adminTel 쉼표 구분). 로그는 항상 남김 */
    public void alertAdmin(String template, Map<String, String> params, String smsBody) {
        log.warn("[WIFI-ALERT] {} {}", template, smsBody);
        if (StringUtils.isBlank(adminTel)) return;
        for (String t : adminTel.split(",")) talk(t.trim(), template, params, "[와이파이] " + smsBody);
    }

    /** 템플릿 변수용 장비 이름: "CityFi T8(…0012)". 모델(T8)은 TUGE 장비 정보에서 자동, 예외만 wifi.deviceNames */
    public String deviceLabel(String sn) {
        if (sn == null) return "";
        WifiDeviceDto d = wifiMapper.findDevice(sn);
        String type = d == null ? null : d.getDeviceType();
        String model = null;
        for (String kv : deviceNames.split(",")) {
            String[] p = kv.split("=", 2);
            if (p.length == 2 && p[0].trim().equalsIgnoreCase(StringUtils.defaultString(type))) model = p[1].trim();
        }
        if (model == null) model = "CityFi" + (StringUtils.isBlank(type) ? "" : " " + type.trim());   // TUGE 모델(T8, T9…)을 그대로 붙임
        return model + "(…" + StringUtils.right(sn, 4) + ")";   // 고객 별명은 넣지 않음 (사용자 결정)
    }

    public static String won(int n) {
        return String.format("%,d", n);
    }

    public static Map<String, String> params(String... kv) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put(kv[i], kv[i + 1] == null ? "" : kv[i + 1]);
        return m;
    }

    /** 알림톡 1건. 실패(응답 오류·통신 오류)면 SMS 로 대신 보냄 */
    private boolean deliver(String tel, String template, Map<String, String> params, String smsBody) throws Exception {
        StoreDto store = store();
        try {
            Map<String, Object> recipient = new HashMap<>();
            recipient.put("recipientNo", tel);
            recipient.put("templateParameter", params);
            Map<String, Object> body = new HashMap<>();
            body.put("senderKey", store.getNhnKakaoSenderKey());
            body.put("templateCode", template);
            body.put("recipientList", Collections.singletonList(recipient));
            Map<String, String> header = Collections.singletonMap("X-Secret-Key", store.getNhnKakaoSecretKey());
            Map<String, Object> path = Collections.singletonMap("appkey", store.getNhnKakaoAppKey());
            String res = ApiUtil.post(kakaoDomain + kakaoMessagesUrl, header, body, path, okhttp3.MediaType.parse("application/json; charset=UTF-8"));
            if (ok(res)) {
                log.info("[WIFI-TALK] {} 발송 tel={}", template, WifiAuthService.maskTel(tel));
                return true;
            }
            log.warn("[WIFI-TALK] {} 실패 응답 → SMS 대체. res={}", template, StringUtils.abbreviate(res, 500));
        } catch (Exception e) {
            log.warn("[WIFI-TALK] {} 오류 → SMS 대체: {}", template, e.getMessage());
        }
        if (smsBody == null) return false;
        sendSms(store, tel, smsBody);
        return true;
    }

    /** NHN 응답: header.isSuccessful 이고 수신자 결과 코드 0 */
    static boolean ok(String res) throws Exception {
        Map<String, Object> r = new ObjectMapper().readValue(res, new TypeReference<Map<String, Object>>() { });
        Map<?, ?> h = (Map<?, ?>) r.get("header");
        if (h == null || !Boolean.TRUE.equals(h.get("isSuccessful"))) return false;
        Map<?, ?> m = (Map<?, ?>) r.get("message");
        List<?> results = m == null ? null : (List<?>) m.get("sendResults");
        if (results == null || results.isEmpty()) return false;
        Object code = ((Map<?, ?>) results.get(0)).get("resultCode");
        return code != null && "0".equals(String.valueOf(code));
    }

    private void sendSms(StoreDto store, String tel, String body) throws Exception {
        Map<String, Object> m = new HashMap<>();
        m.put("smsBody", body);
        m.put("shippingTel1", tel);
        m.put("storeId", store.getId());
        smsService.insertSms(m, store);
    }

    private StoreDto store() {
        if (StringUtils.isBlank(smsStoreId)) throw new IllegalStateException("wifi.smsStoreId 설정이 없습니다.");
        Map<String, Object> q = new HashMap<>();
        q.put("id", Long.valueOf(smsStoreId));
        StoreDto store = storeService.findById(q);
        if (store == null) throw new IllegalStateException("알림 발송 스토어를 찾을 수 없습니다.");
        return store;
    }
}
