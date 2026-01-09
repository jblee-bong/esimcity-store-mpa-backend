package com.naver.naverspabackend.security;

import com.naver.naverspabackend.security.token.TugeRedisToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TugeRedisRepository extends CrudRepository<TugeRedisToken, String> {


}
