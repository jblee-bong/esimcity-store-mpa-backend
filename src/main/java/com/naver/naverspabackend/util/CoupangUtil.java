package com.naver.naverspabackend.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
/**
 * 쿠팡 API 연동을 위한 authorization 생성 클래스
 */
@Slf4j
public class CoupangUtil {



    /**
     * 쿠팡 Authorization 생성
     * @param method HTTP 메서드 (GET, POST, PUT, DELETE)
     * @param uri API 경로
     * @param accessKey 쿠팡 Access Key
     * @param secretKey 쿠팡 Secret Key
     * @return 쿠팡 authorization 헤더
     */
    public static String getAuthorization(String method, String uri, String accessKey, String secretKey) throws Exception {
        String[] parts = uri.split("\\?");
        if (parts.length > 2) {
            throw new RuntimeException("incorrect uri format");
        } else {
            String path = parts[0];
            String query = "";
            if (parts.length == 2) {
                query = parts[1];
            }

            SimpleDateFormat dateFormatUtc = new SimpleDateFormat("yyMMdd'T'HHmmss'Z'");
            dateFormatUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
            String datetime = dateFormatUtc.format(new Date());
            String message = datetime + method + path + query;

            String signature;
            try {
                SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(signingKey);
                byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
                signature = Hex.encodeHexString(rawHmac);
            } catch (GeneralSecurityException var14) {
                throw new IllegalArgumentException("Unexpected error while creating hash: " + var14.getMessage(), var14);
            }

            return String.format("CEA algorithm=%s,access-key=%s,signed-date=%s,signature=%s", "HmacSHA256", accessKey, datetime, signature);

        }
    }

}
