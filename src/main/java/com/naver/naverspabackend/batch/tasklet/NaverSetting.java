package com.naver.naverspabackend.batch.tasklet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.security.NaverRedisRepository;
import com.naver.naverspabackend.security.SignatureGenerator;
import com.naver.naverspabackend.security.token.NaverRedisToken;
import com.naver.naverspabackend.util.ApiUtil;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * 메인 DB에서 batch 관련 설정값을 불러와 ExcuteContext에 저장하는 Step
 *
 */

@Component
@Slf4j
public class NaverSetting {

    @Autowired
    private NaverRedisRepository naverRedisRepository;

    @Autowired
    private StoreMapper storeMapper;

    @Value("${naver-api.base}")
    private String baseUrl;

    @Value("${naver-api.token}")
    private String tokenUrl;


    /**
     * 전자 서명 및 인증 토큰 발급 요청
     * @param jobTimeKey
     * @return
     * @throws Exception
     */
    public void setting(String jobTimeKey)  {

            Map<String,Object> param = new HashMap<>();
            param.put("userAuthority","ROLE_ADMIN");
            List<StoreDto> storeDtoList = storeMapper.selectStoreList(param);

            for (StoreDto storeDto : storeDtoList) {
                if(storeDto.getPlatform()==null ||!storeDto.getPlatform().equals("naver")){
                    continue;
                }
                try{
                    Long storeId = storeDto.getId();

                    Optional<NaverRedisToken> byStoreId = naverRedisRepository.findById(storeId);

                    if(byStoreId.isPresent()){
                        NaverRedisToken naverRedisToken = byStoreId.orElse(null);

                        // mills
                        Long timeStamp = naverRedisToken.getTimeStamp();

                        if(naverRedisToken.getExpiresIn() !=null){
                            Long expiresIn = naverRedisToken.getExpiresIn() * 1000;

                            Date now = new Date();

                            long time = now.getTime();

                            // 인증시간 만료
                            if((timeStamp + expiresIn) < time){
                                requestOauth2Token(storeDto, requestSecretSign(storeDto));
                            }
                        }else{

                            requestOauth2Token(storeDto, requestSecretSign(storeDto));
                        }

                    }else{
                        requestOauth2Token(storeDto, requestSecretSign(storeDto));
                    }
                }catch (Exception e){
                    log.error("토큰 생성 실패 - Store ID: {}, Store Name: {}, Error: {}", 
                        storeDto.getId(), storeDto.getStoreName(), e.getMessage(), e);
                }
            }

    }

    /**
     * 전자서명 키 요청
     * @return
     */
    public NaverRedisToken requestSecretSign(StoreDto storeDto){

        String secretSign = "";

        String clientId = storeDto.getClientId();
        String clientSecret = storeDto.getClientSecret();

        Date now = new Date();

        Long timeStamp = now.getTime();

        String signature = SignatureGenerator.generateSignature(clientId, clientSecret, timeStamp);

        NaverRedisToken naverRedisToken = new NaverRedisToken();

        naverRedisToken.setStoreId(storeDto.getId());
        naverRedisToken.setSecretSign(signature);
        naverRedisToken.setTimeStamp(timeStamp);

        NaverRedisToken save = naverRedisRepository.save(naverRedisToken);

        return save;
    }

    public NaverRedisToken requestOauth2Token(StoreDto storeDto, NaverRedisToken naverRedisToken) throws IOException {

        Map<String, Object> bodyMap = new HashMap<>();

        bodyMap.put("client_id", storeDto.getClientId());
        bodyMap.put("client_secret_sign", naverRedisToken.getSecretSign());
        bodyMap.put("timestamp", naverRedisToken.getTimeStamp());
        bodyMap.put("grant_type", "client_credentials");
        bodyMap.put("type", "SELF");

        String res = ApiUtil.postWithQueryParam(baseUrl + tokenUrl, bodyMap, null, false);

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> resMap = objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {});

        String accessToken = Objects.toString(resMap.get("access_token"), "");
        String expiresIn = Objects.toString(resMap.get("expires_in"), "0");
        String tokenType = Objects.toString(resMap.get("token_type"), "");

        naverRedisToken.setOauthToken(accessToken);
        naverRedisToken.setExpiresIn(Long.parseLong(expiresIn));
        naverRedisToken.setTokenType(tokenType);

        NaverRedisToken save = naverRedisRepository.save(naverRedisToken);

        return save;
    }

}
