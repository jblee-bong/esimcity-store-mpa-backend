package com.naver.naverspabackend.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

public class SignatureGenerator {

    public static String generateSignature(String clientId, String clientSecret, Long timestamp) {
        // 밑줄로 연결하여 password 생성
        String password = StringUtils.joinWith("_", clientId, timestamp);
        // bcrypt 해싱
        String hashedPw = BCrypt.hashpw(password, clientSecret);
        // base64 인코딩
        return Base64.getUrlEncoder().encodeToString(hashedPw.getBytes(StandardCharsets.UTF_8));
    }

    public static void main(String args[]) {
        String clientId = "aaaabbbbcccc";
        String clientSecret = "$2a$10$abcdefghijklmnopqrstuv";
        Long timestamp = System.currentTimeMillis(); // 1643961623299L로 가정
        System.out.println(generateSignature(clientId, clientSecret, timestamp));
    }

}
