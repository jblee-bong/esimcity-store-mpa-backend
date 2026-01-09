package com.naver.naverspabackend.security.token;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@NoArgsConstructor
@RedisHash(value = "naver_token")
public class NaverRedisToken {

    @Id
    private Long storeId;

    private String secretSign;

    private Long timeStamp;

    // oauth2 access_token
    private String oauthToken;

    // oauth2 인증 유효 기간(초)
    private Long expiresIn;

    // oauth2 인증 토큰 종류
    private String tokenType;

    public Map<String, String> returnHeaderMap(){
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Bearer " + this.oauthToken);
        return headerMap;
    }

}
