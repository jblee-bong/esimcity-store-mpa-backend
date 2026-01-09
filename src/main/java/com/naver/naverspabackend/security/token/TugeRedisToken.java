package com.naver.naverspabackend.security.token;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@RedisHash(value = "tuge_token")
public class TugeRedisToken {

    @Id
    private String accountId;

    private String accessToken;

    private Long timeStamp;


    // oauth2 인증 유효 기간(초)
    private Long expiresIn;


    public HttpHeaders returnHeaderMap(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

}
