package com.naver.naverspabackend.security;

import com.naver.naverspabackend.security.token.NaverRedisToken;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface NaverRedisRepository extends CrudRepository<NaverRedisToken, Long> {

    Optional<NaverRedisToken> findByStoreId(Long storeId);

}
