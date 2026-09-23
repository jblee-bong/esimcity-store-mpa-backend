package com.naver.naverspabackend.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class NaverSearchAdSignatureHelper {

    private NaverSearchAdSignatureHelper() {
    }

    /**
     * 네이버 검색광고 API 서명: Base64(HMAC-SHA256("{timestamp}.{method}.{uri}"))
     */
    public static String generate(String timestamp, String method, String uri, String secretKey) {
        try {
            String message = timestamp + "." + method + "." + uri;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate Naver Search Ad signature", e);
        }
    }
}
