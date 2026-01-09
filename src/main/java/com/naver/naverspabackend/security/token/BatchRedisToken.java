package com.naver.naverspabackend.security.token;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@NoArgsConstructor
@RedisHash(value = "batch_token")
public class BatchRedisToken {

    @Id
    private String batchId;

    private Long timeStamp;


}
