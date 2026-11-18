package com.naver.naverspabackend.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * TUGE(TGT) Device Business API V1.23 클라이언트 (글로벌 포켓 WiFi T8).
 * - 요청 data 는 AES-128-CBC 후 'a'-offset hex 인코딩, sign = md5(accountId+serviceName+requestTime+body+version+signKey)
 * - 모든 시간은 GMT+0
 * - TPS 한도는 TUGE 전체 고객 공유 → API 별 최소 호출 간격으로 제어 (서버 1대 기준)
 */
@Slf4j
@Component
public class TugeDeviceUtil {

    public static final String UTC_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Value("${api.tugeDevice.accountId:}")
    private String accountId;
    @Value("${api.tugeDevice.signKey:}")
    private String signKey;
    @Value("${api.tugeDevice.secretKey:}")
    private String secretKey;
    @Value("${api.tugeDevice.vector:}")
    private String vector;
    @Value("${api.tugeDevice.version:1.0}")
    private String version;
    @Value("${api.tugeDevice.baseUrl:http://enterpriseapiuat.tugegroup.com:8060/}")
    private String baseUrl;

    private final Gson gson = new Gson();
    private final RestTemplate restTemplate = createRestTemplate();

    // shortcut: 서버 1대 기준 메모리 TPS 제어. 서버를 늘리면 Redis 기반으로 옮겨야 함
    private final Map<String, Object> locks = new ConcurrentHashMap<>();
    private final Map<String, Long> lastCall = new ConcurrentHashMap<>();

    /** 서버 시작 시 어떤 설정을 읽었는지 기록 (키 값 자체는 안 남김) */
    @javax.annotation.PostConstruct
    void logConfig() {
        log.info("[TUGE-DEVICE] 설정 configured={} accountId={} baseUrl={} 비어있음={}", isConfigured(), accountId, baseUrl, blankKeys());
    }

    private List<String> blankKeys() {
        List<String> b = new ArrayList<>();
        if (StringUtils.isBlank(accountId)) b.add("accountId");
        if (StringUtils.isBlank(signKey)) b.add("signKey");
        if (StringUtils.isBlank(secretKey)) b.add("secretKey");
        if (StringUtils.isBlank(vector)) b.add("vector");
        return b;
    }

    /** 로그용: 와이파이 비밀번호 가리기 */
    private String masked(Object o) {
        return gson.toJson(o).replaceAll("(\"(wifiPwd|currentPw)\":\")[^\"]*", "$1***");
    }

    public boolean isConfigured() {
        return StringUtils.isNoneBlank(accountId, signKey, secretKey, vector);
    }

    /* ===================== API ===================== */

    /** 3.1 상품 목록 (page 1부터, pageSize 최대 100) */
    public Map<String, Object> getProductList(int page, int pageSize) {
        Map<String, Object> d = new HashMap<>();
        d.put("page", page);
        d.put("pageSize", pageSize);
        d.put("status", "1");
        d.put("productType", "");
        d.put("lang", "en");
        return call("queryBatchChannelsProductListByParams", "api-publicappmodule/productApi/queryBatchChannelsProductListByParams", d, 1000);
    }

    /** 3.2 개통 여부. code 0002 = 미개통 */
    public Map<String, Object> checkDevice(String sn) {
        return call("checkDevice", "api-publicappmodule/saleDeviceApi/checkDevice", deviceNo(sn), 1000);
    }

    /** 3.3 개통 */
    public Map<String, Object> activeDevice(String sn) {
        return call("activeDevice", "api-publicappmodule/saleDeviceApi/activeDevice", deviceNo(sn), 1000);
    }

    /**
     * 3.4 요금제 주문. requestId 가 같으면 재시도로 처리됨(중복 주문 없음, TUGE 확인)
     * startDate: "yyyy-MM-dd HH:mm:ss"(GMT) 형식 필수. 날짜 지정 상품은 TUGE 가 이 시각을 한국 날짜로 바꿔 그날 00:00~23:59(KST)로 잡음
     * (2026-11-18 샌드박스: 11-26 00:00 → KST 11/26, 11-27 20:00 → KST 11/28). 그 외 상품은 실제 개통 시점부터라 값 의미 없음
     */
    public Map<String, Object> addOrder(String sn, String productCode, int orderPeriod, String startDate, String otaOrderNo) {
        Map<String, Object> d = new HashMap<>();
        d.put("productCode", productCode);
        d.put("currency", "USD");
        d.put("orderPeriod", orderPeriod);
        d.put("startDate", startDate);
        d.put("deviceNo", sn);
        d.put("lang", "en");
        d.put("otaOrderNo", otaOrderNo);
        d.put("requestId", otaOrderNo);
        return call("addOrder", "api-publicappmodule/saleOrderApi/addOrder", d, 340);
    }

    /** 3.5 요금제 주문 취소 (미개통 주문, 또는 개통된 주문 조기 종료). 장비로 발송된 주문이면 deviceNo 필수 */
    public Map<String, Object> cancelOrder(String orderNo, String sn) {
        Map<String, Object> d = new HashMap<>();
        d.put("orderNo", orderNo);
        if (sn != null) d.put("deviceNo", sn);
        d.put("lang", "en");
        return call("cancelOrder", "api-publicappmodule/saleOrderApi/cancelOrder", d, 1000);
    }

    /** 3.16 장비 개별 속성 조회 (와이파이 이름·비밀번호, 화면 언어, 데이터 절약 모드) */
    public Map<String, Object> getIndividualDeviceInfo(String sn) {
        Map<String, Object> d = new HashMap<>();
        d.put("page", 1);
        d.put("pageSize", 1);
        d.put("deviceNo", sn);
        d.put("lang", "en");
        return call("getIndividualDeviceInfo", "api-publicappmodule/saleDeviceApi/getIndividualDeviceInfo", d, 1000);
    }

    /** 3.15 장비 개별 속성 변경. 비밀번호·언어·절약모드 모두 필수라 현재 값에 바꿀 것만 덮어 보냄 */
    public Map<String, Object> updateIndividualDeviceInfo(String sn, String wifiPwd, String deviceLang, String cardLabel, String blackList) {
        Map<String, Object> d = new HashMap<>();
        d.put("deviceNo", sn);
        d.put("wifiPwd", wifiPwd);
        d.put("deviceLang", deviceLang);
        d.put("cardLabel", cardLabel == null ? "" : cardLabel);
        d.put("blackList", blackList);
        d.put("lang", "en");
        return call("updateIndividualDeviceInfo", "api-publicappmodule/saleDeviceApi/updateIndividualDeviceInfo", d, 1000);
    }

    /** 3.7 장비의 요금제 목록 (dataUsage 는 누적, 약 1시간 지연) */
    public Map<String, Object> queryOrderList(String sn) {
        Map<String, Object> d = new HashMap<>();
        d.put("page", 1);
        d.put("pageSize", 100);
        d.put("orderNo", "");
        d.put("deviceNo", sn);
        d.put("orderStatus", "");
        d.put("otaOrderNo", "");
        d.put("lang", "en");
        return call("queryOrderList", "api-publicappmodule/saleOrderApi/queryOrderList", d, 1000);
    }

    /** 3.8 채널 장비 목록 (필드명 deviceNO 대문자 O) */
    public Map<String, Object> getDeviceList(int page, int pageSize) {
        Map<String, Object> d = new HashMap<>();
        d.put("page", page);
        d.put("pageSize", pageSize);
        d.put("deviceNO", "");
        d.put("deviceStatus", "");
        d.put("lang", "en");
        return call("getDeviceList", "api-publicappmodule/saleDeviceApi/getDeviceList", d, 1000);
    }

    /** 3.9 장비 상태 변경 (SUSPEND/RESUME/STOP) */
    public Map<String, Object> updateDeviceStatus(List<String> sns, String operation) {
        Map<String, Object> d = new HashMap<>();
        d.put("deviceNoList", sns);
        d.put("operation", operation);
        d.put("lang", "en");
        return call("batchUpdateDeviceStatus", "api-publicappmodule/saleDeviceApi/batchUpdateDeviceStatus", d, 1000);
    }

    /** 3.10 실시간 상태(온라인, 배터리, 신호, 당일 사용량) - 필드명 deviceNO */
    public Map<String, Object> getConnectionStatus(String sn) {
        Map<String, Object> d = new HashMap<>();
        d.put("deviceNO", sn);
        d.put("lang", "en");
        return call("getDeviceConnectionStatus", "api-publicappmodule/saleDeviceApi/getDeviceConnectionStatus", d, 200);
    }

    /** 3.14 채널 계정 잔액 */
    public Map<String, Object> getBalance() {
        Map<String, Object> d = new HashMap<>();
        d.put("type", "");
        d.put("lang", "en");
        return call("getChannelAccountBalance", "api-publicappmodule/tgtapi/getChannelAccountBalance", d, 1000);
    }

    /** 3.23 장비 상세. WiFi 비밀번호가 포함되므로 절대 화면/로그로 내보내지 말 것 (소유 확인용으로만 사용) */
    public Map<String, Object> getDeviceInfo(String sn) {
        return call("deviceAggregationInfo", "api-publicappmodule/saleDeviceApi/deviceAggregationInfo", deviceNo(sn), 1000);
    }

    /** 3.24 데이터 충전팩 */
    public Map<String, Object> addPopupOrder(String baseOrderNo, String popupDataUsage, int count, String otaOrderNo) {
        Map<String, Object> d = new HashMap<>();
        d.put("orderNo", baseOrderNo);
        d.put("otaOrderNo", otaOrderNo);
        d.put("popupDataUsage", popupDataUsage);
        d.put("orderPeriod", count);
        d.put("lang", "en");
        d.put("requestId", otaOrderNo);
        return call("addPopupOrder", "api-publicappmodule/saleDeviceApi/addPopupOrder", d, 1000);
    }

    public static boolean isOk(Map<String, Object> res) {
        return res != null && "0000".equals(String.valueOf(res.get("code")));
    }

    public static String utcNow() {
        SimpleDateFormat f = new SimpleDateFormat(UTC_PATTERN);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(new Date());
    }

    /** 고객이 고른 한국 날짜(yyyy-MM-dd) → addOrder startDate. 그날 00:00 GMT = 한국 09:00 이라 TUGE 가 같은 한국 날짜로 잡음 */
    public static String startDateOf(String kstDate) {
        return kstDate + " 00:00:00";
    }

    /** TUGE UTC 문자열 → KST 문자열 (yyyy-MM-dd HH:mm). 파싱 실패 시 원본 */
    public static String utcToKst(Object utc) {
        if (utc == null || StringUtils.isBlank(utc.toString())) return null;
        try {
            SimpleDateFormat in = new SimpleDateFormat(UTC_PATTERN);
            in.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            out.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            return out.format(in.parse(utc.toString()));
        } catch (Exception e) {
            return utc.toString();
        }
    }

    /* ===================== 공통 ===================== */

    private Map<String, Object> deviceNo(String sn) {
        Map<String, Object> d = new HashMap<>();
        d.put("deviceNo", sn);
        d.put("lang", "en");
        return d;
    }

    /**
     * 공통 호출. 반환 Map: code, msg, data(복호화·파싱된 Object), total
     * 통신 오류도 예외 대신 code=NETWORK 로 반환 (호출부에서 재시도 판단)
     */
    private Map<String, Object> call(String serviceName, String path, Map<String, Object> data, long minIntervalMs) {
        Map<String, Object> result = new HashMap<>();
        if (!isConfigured()) {
            result.put("code", "NOT_CONFIGURED");
            result.put("msg", "api.tugeDevice 설정이 없습니다.");
            log.warn("[TUGE-DEVICE] {} 호출 안 함: 설정 없음 비어있음={} (서버가 새 yml 을 읽었는지 확인)", serviceName, blankKeys());
            return result;
        }
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                throttle(serviceName, minIntervalMs);
                String requestTime = utcNow();
                String body = encrypt(gson.toJson(data));
                log.info("[TUGE-DEVICE] {} 요청 url={} data={}", serviceName, baseUrl + path, masked(data));

                Map<String, Object> req = new HashMap<>();
                req.put("requestTime", requestTime);
                req.put("accountId", accountId);
                req.put("data", body);
                req.put("sign", md5(accountId + serviceName + requestTime + body + version + signKey));
                req.put("serviceName", serviceName);
                req.put("version", version);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                ResponseEntity<String> response = restTemplate.exchange(baseUrl + path, HttpMethod.POST,
                    new HttpEntity<>(gson.toJson(req), headers), String.class);

                Map<String, Object> raw = gson.fromJson(response.getBody(), Map.class);
                result.put("code", raw.get("code") == null ? null : String.valueOf(raw.get("code")));
                result.put("msg", raw.get("msg"));
                result.put("total", raw.get("total"));
                result.put("data", decodeData(raw.get("data")));
                log.info("[TUGE-DEVICE] {} 응답 http={} code={} msg={} data={}", serviceName, response.getStatusCodeValue(), result.get("code"), result.get("msg"),
                    StringUtils.abbreviate(masked(result.get("data")), 3000));

                // 1000 = 실패/호출 과다. 짧게 쉬고 1회 재시도 (addOrder 는 requestId 로 중복 방지됨)
                if ("1000".equals(result.get("code")) && attempt == 0) {
                    Thread.sleep(1200);
                    continue;
                }
                return result;
            } catch (Exception e) {
                log.error("[TUGE-DEVICE] {} error: {}", serviceName, e.getMessage());
                result.put("code", "NETWORK");
                result.put("msg", e.getMessage());
                if (attempt == 0) {
                    try { Thread.sleep(1200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        return result;
    }

    /** data 가 암호문 문자열이면 복호화, 이미 JSON 이면 그대로 (3.2 는 응답 data 비암호화) */
    private Object decodeData(Object data) {
        if (data == null) return null;
        if (!(data instanceof String)) return data;
        String s = (String) data;
        if (s.isEmpty()) return null;
        String plain = s.matches("[a-p]+") ? decrypt(s) : s;
        if (plain == null) return s;
        try {
            JsonElement el = JsonParser.parseString(plain);
            return gson.fromJson(el, Object.class);
        } catch (Exception e) {
            return plain;
        }
    }

    private void throttle(String serviceName, long minIntervalMs) throws InterruptedException {
        Object lock = locks.computeIfAbsent(serviceName, k -> new Object());
        synchronized (lock) {
            long wait = lastCall.getOrDefault(serviceName, 0L) + minIntervalMs - System.currentTimeMillis();
            if (wait > 0) Thread.sleep(wait);
            lastCall.put(serviceName, System.currentTimeMillis());
        }
    }

    private static RestTemplate createRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(5000);
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        return new RestTemplate(factory);
    }

    /* ===== 암호화 (TugeUtilOld 와 동일 규격, 문서 2.8) ===== */

    String encrypt(String plain) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES"),
            new IvParameterSpec(vector.getBytes(StandardCharsets.UTF_8)));
        return encodeBytes(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
    }

    String decrypt(String enc) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES"),
                new IvParameterSpec(vector.getBytes(StandardCharsets.UTF_8)));
            return new String(cipher.doFinal(decodeBytes(enc)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[TUGE-DEVICE] decrypt error");
            return null;
        }
    }

    static String encodeBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((char) (((b >> 4) & 0xF) + 'a'));
            sb.append((char) ((b & 0xF) + 'a'));
        }
        return sb.toString();
    }

    static byte[] decodeBytes(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length(); i += 2) {
            bytes[i / 2] = (byte) (((str.charAt(i) - 'a') << 4) + (str.charAt(i + 1) - 'a'));
        }
        return bytes;
    }

    static String md5(String s) throws Exception {
        byte[] d = MessageDigest.getInstance("MD5").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : d) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
