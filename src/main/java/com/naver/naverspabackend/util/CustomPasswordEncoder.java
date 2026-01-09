package com.naver.naverspabackend.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class CustomPasswordEncoder implements PasswordEncoder {

    private final static String PASSWORD = "PASSWORD";

    @Value("${naver.clientId}")
    private String clientId;

    @Value("${naver.clientSecret}")
    private String clientSecret;

    @Override
    public String encode(CharSequence rawPassword) {
        return BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt(12));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (!rawPassword.toString().equals("") && !rawPassword.toString().equals(clientId) && !rawPassword.toString().equals(clientSecret)) {
            List<Map<String, String>> mapList = JsonUtil.fromJson(encodedPassword, ArrayList.class);
            for (Map<String, String> result : mapList) {
                if (BCrypt.checkpw(rawPassword.toString(), result.get(PASSWORD))) {
                    return true;
                }
            }
            return false;
        } else {
            return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
        }
    }
}
