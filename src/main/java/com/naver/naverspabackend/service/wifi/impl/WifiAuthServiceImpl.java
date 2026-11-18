package com.naver.naverspabackend.service.wifi.impl;

import com.naver.naverspabackend.dto.WifiDeviceDto;
import com.naver.naverspabackend.dto.WifiMemberDto;
import com.naver.naverspabackend.dto.WifiPointLogDto;
import com.naver.naverspabackend.exception.WifiException;
import com.naver.naverspabackend.mybatis.mapper.WifiMapper;
import com.naver.naverspabackend.response.model.ResponseCode;
import com.naver.naverspabackend.service.wifi.WifiAuthService;
import com.naver.naverspabackend.service.wifi.WifiSmsSender;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class WifiAuthServiceImpl implements WifiAuthService {

    static final String COOKIE = "WIFI_SESSION";
    private static final long TOKEN_DAYS = 365;
    private static final long RENEW_AFTER_MS = TimeUnit.DAYS.toMillis(7);
    private static final int OTP_TTL_SEC = 180;
    private static final int OTP_MAX_TRY = 5;
    private static final int OTP_MAX_PER_TEL_HOUR = 5;
    private static final int OTP_MAX_PER_IP_HOUR = 20;

    @Autowired
    private WifiMapper wifiMapper;
    @Autowired
    private WifiSmsSender smsSender;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${wifi.jwtKey:}")
    private String jwtKey;
    @Value("${wifi.cookieSecure:true}")
    private boolean cookieSecure;

    private Key key;
    private final SecureRandom random = new SecureRandom();

    @PostConstruct
    void init() {
        byte[] bytes;
        if (StringUtils.isBlank(jwtKey) || jwtKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            // 설정이 없으면 임시 키 (재시작 시 전원 로그아웃). 운영에서는 반드시 32바이트 이상 wifi.jwtKey 설정
            log.warn("[WIFI] wifi.jwtKey 미설정 또는 32바이트 미만 - 임시 키 사용 (재시작하면 전원 로그아웃)");
            bytes = new byte[32];
            random.nextBytes(bytes);
        } else {
            bytes = jwtKey.getBytes(StandardCharsets.UTF_8);
        }
        key = Keys.hmacShaKeyFor(bytes);
    }

    /* ---------------- OTP ---------------- */

    @Override
    public void sendOtp(String rawTel, String ip) {
        String tel = WifiAuthService.normalizeTel(rawTel);
        if (!tel.matches("01[016789][0-9]{7,8}")) throw new WifiException("휴대폰 번호를 확인해주세요.");
        if (!underLimit("wifi:otp:cnt:" + tel, OTP_MAX_PER_TEL_HOUR) || !underLimit("wifi:otp:ip:" + ip, OTP_MAX_PER_IP_HOUR)) {
            throw new WifiException("인증번호 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
        }
        issueCode("wifi:otp:", tel);
    }

    @Override
    public String sendActionOtp(WifiMemberDto member, String ip) {
        String tel = member.getTel();
        if (!underLimit("wifi:otp:cnt:" + tel, OTP_MAX_PER_TEL_HOUR) || !underLimit("wifi:otp:ip:" + ip, OTP_MAX_PER_IP_HOUR)) {
            throw new WifiException("인증번호 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
        }
        issueCode("wifi:otp:act:", tel);
        return WifiAuthService.maskTel(tel);
    }

    @Override
    public void verifyActionOtp(WifiMemberDto member, String code) {
        checkCode("wifi:otp:act:", member.getTel(), code);
        redisTemplate.delete("wifi:otp:act:" + member.getTel());
    }

    private void issueCode(String prefix, String tel) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        redisTemplate.opsForValue().set(prefix + tel, code, OTP_TTL_SEC, TimeUnit.SECONDS);
        redisTemplate.delete(prefix + "try:" + tel);
        try {
            smsSender.sendOtp(tel, code);
        } catch (Exception e) {
            log.error("[WIFI] OTP 발송 실패: {}", e.getMessage());
            throw new WifiException("인증번호 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /** 만료·시도 횟수·일치 확인 (일치해도 코드는 지우지 않음) */
    private void checkCode(String prefix, String tel, String code) {
        Object saved = redisTemplate.opsForValue().get(prefix + tel);
        if (saved == null) throw new WifiException("인증번호가 만료되었습니다. 다시 요청해주세요.");
        Long tries = redisTemplate.opsForValue().increment(prefix + "try:" + tel);
        if (tries != null && tries == 1) redisTemplate.expire(prefix + "try:" + tel, OTP_TTL_SEC, TimeUnit.SECONDS);
        if (tries != null && tries > OTP_MAX_TRY) {
            redisTemplate.delete(prefix + tel);
            throw new WifiException("인증 시도 횟수를 초과했습니다. 다시 요청해주세요.");
        }
        if (code == null || !MessageDigest.isEqual(saved.toString().getBytes(StandardCharsets.UTF_8),
            code.trim().getBytes(StandardCharsets.UTF_8))) {
            throw new WifiException("인증번호가 일치하지 않습니다.");
        }
    }

    /** 1시간 단위 카운터. 한도 이내면 true */
    private boolean underLimit(String key, int max) {
        Long n = redisTemplate.opsForValue().increment(key);
        if (n != null && n == 1) redisTemplate.expire(key, 1, TimeUnit.HOURS);
        return n != null && n <= max;
    }

    @Override
    @Transactional
    public Map<String, Object> verifyOtp(String rawTel, String code, boolean privacyAgree, boolean marketingAgree, HttpServletResponse res) {
        String tel = WifiAuthService.normalizeTel(rawTel);
        checkCode("wifi:otp:", tel, code);

        WifiMemberDto member = wifiMapper.findMemberByTel(tel);
        boolean needsAgree = member == null || "PRE".equals(member.getStatus());
        if (needsAgree && !privacyAgree) {
            Map<String, Object> r = new HashMap<>();
            r.put("needAgree", true);
            return r; // 인증번호 유지 → 동의 후 같은 번호로 다시 요청
        }
        String marketingYn = marketingAgree ? "Y" : "N";
        if (member == null) {
            member = new WifiMemberDto();
            member.setTel(tel);
            member.setStatus("ACTIVE");
            member.setMarketingAgreeYn(marketingYn);
            wifiMapper.insertMember(member);
        } else if ("PRE".equals(member.getStatus())) {
            wifiMapper.activateMember(member.getId(), marketingYn);
        }
        redisTemplate.delete("wifi:otp:" + tel);
        redisTemplate.delete("wifi:otp:try:" + tel);

        member = wifiMapper.findMemberById(member.getId());
        wifiMapper.updateLastLogin(member.getId());
        issueCookie(member, res);

        Map<String, Object> r = new HashMap<>();
        r.put("needAgree", false);
        r.put("tel", WifiAuthService.maskTel(member.getTel()));
        r.put("pointBalance", member.getPointBalance());
        return r;
    }

    /* ---------------- 세션 (쿠키 + JWT) ---------------- */

    private void issueCookie(WifiMemberDto member, HttpServletResponse res) {
        long now = System.currentTimeMillis();
        String token = Jwts.builder()
            .setSubject(String.valueOf(member.getId()))
            .claim("v", member.getTokenVersion())
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + TimeUnit.DAYS.toMillis(TOKEN_DAYS)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
        setCookie(res, token, Duration.ofDays(TOKEN_DAYS));
    }

    private void setCookie(HttpServletResponse res, String value, Duration maxAge) {
        ResponseCookie c = ResponseCookie.from(COOKIE, value)
            .httpOnly(true).secure(cookieSecure).sameSite("Lax").path("/").maxAge(maxAge).build();
        res.addHeader(HttpHeaders.SET_COOKIE, c.toString());
    }

    @Override
    public WifiMemberDto currentMember(HttpServletRequest req, HttpServletResponse res) {
        String token = null;
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) if (COOKIE.equals(c.getName())) token = c.getValue();
        }
        if (StringUtils.isBlank(token)) return null;
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            WifiMemberDto member = wifiMapper.findMemberById(Long.valueOf(claims.getSubject()));
            Integer v = claims.get("v", Integer.class);
            if (member == null || !"ACTIVE".equals(member.getStatus()) || v == null || v != member.getTokenVersion()) {
                return null;
            }
            // 사실상 평생 로그인: 7일 지난 토큰은 방문 시 1년짜리로 재발급
            if (res != null && claims.getIssuedAt() != null
                && System.currentTimeMillis() - claims.getIssuedAt().getTime() > RENEW_AFTER_MS) {
                issueCookie(member, res);
            }
            return member;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public WifiMemberDto requireMember(HttpServletRequest req, HttpServletResponse res) {
        WifiMemberDto m = currentMember(req, res);
        if (m == null) throw new WifiException("로그인이 필요합니다.", ResponseCode.UNAUTHORIZED);
        return m;
    }

    @Override
    public void logout(WifiMemberDto member, HttpServletResponse res) {
        if (member != null) wifiMapper.incrementTokenVersion(member.getId());
        setCookie(res, "", Duration.ZERO);
    }

    @Override
    @Transactional
    public void withdraw(WifiMemberDto member, HttpServletResponse res) {
        if (member.getPointBalance() > 0) {
            WifiPointLogDto pl = new WifiPointLogDto();
            pl.setMemberId(member.getId());
            pl.setAmount(-member.getPointBalance());
            pl.setReason("WITHDRAW");
            pl.setMemo("회원 탈퇴로 소멸");
            wifiMapper.insertPointLog(pl);
        }
        for (WifiDeviceDto d : wifiMapper.myDevices(member.getId())) {
            wifiMapper.insertDeviceLog(d.getSn(), member.getId(), "RELEASE", null);
        }
        wifiMapper.releaseAllDevices(member.getId());
        wifiMapper.withdrawMember(member.getId());
        setCookie(res, "", Duration.ZERO);
    }
}
