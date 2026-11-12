package com.naver.naverspabackend.service.wifi;

import com.naver.naverspabackend.dto.WifiMemberDto;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WifiAuthService {

    void sendOtp(String tel, String ip);

    /** 성공 시 쿠키 발급. 신규/미가입(PRE)인데 동의가 없으면 {needAgree:true} 반환(인증번호는 유지) */
    Map<String, Object> verifyOtp(String tel, String code, boolean privacyAgree, boolean marketingAgree, HttpServletResponse res);

    /** 로그인 회원 또는 null. 오래된 토큰은 자동 갱신 */
    WifiMemberDto currentMember(HttpServletRequest req, HttpServletResponse res);

    /** 로그인 필수. 없으면 401 WifiException */
    WifiMemberDto requireMember(HttpServletRequest req, HttpServletResponse res);

    void logout(WifiMemberDto member, HttpServletResponse res);

    void withdraw(WifiMemberDto member, HttpServletResponse res);

    static String normalizeTel(String tel) {
        return tel == null ? "" : tel.replaceAll("[^0-9]", "");
    }

    static String maskTel(String tel) {
        if (tel == null || tel.length() < 8) return tel;
        return tel.substring(0, 3) + "-****-" + tel.substring(tel.length() - 4);
    }
}
