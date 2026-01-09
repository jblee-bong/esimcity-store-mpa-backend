package com.naver.naverspabackend.security;

import com.naver.naverspabackend.security.token.BatchRedisToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BatchRedisRepository extends CrudRepository<BatchRedisToken, String> {

    Optional<BatchRedisToken> findByBatchId(String batchId);

}
