package com.naver.naverspabackend.controller;

import com.naver.naverspabackend.dto.WifiMemberDto;
import com.naver.naverspabackend.exception.WifiException;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.response.model.ResponseCode;
import com.naver.naverspabackend.service.wifi.WifiAuthService;
import com.naver.naverspabackend.service.wifi.WifiPricing;
import com.naver.naverspabackend.service.wifi.WifiService;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 포켓와이파이 사용자(React) API. 인증은 WIFI_SESSION 쿠키. 외부 주소는 {wifi.publicUrl}/wifi-api (운영: https://www.esimcity.co.kr/api/wifi-api) */
@Slf4j
@RestController
@RequestMapping("/wifi-api")
@RequiredArgsConstructor
public class WifiController {

    private final WifiAuthService authService;
    private final WifiService wifiService;
    private final WifiPricing pricing;

    /* ---------- 인증 ---------- */

    @PostMapping("/auth/otp/send")
    public ApiResult<?> sendOtp(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        authService.sendOtp(str(body.get("tel")), clientIp(req));
        return ApiResult.succeed(null, null);
    }

    @PostMapping("/auth/otp/verify")
    public ApiResult<?> verifyOtp(@RequestBody Map<String, Object> body, HttpServletResponse res) {
        return ApiResult.succeed(authService.verifyOtp(str(body.get("tel")), str(body.get("code")),
            Boolean.TRUE.equals(body.get("privacyAgree")), Boolean.TRUE.equals(body.get("marketingAgree")), res), null);
    }

    @PostMapping("/auth/logout")
    public ApiResult<?> logout(HttpServletRequest req, HttpServletResponse res) {
        authService.logout(authService.currentMember(req, null), res);
        return ApiResult.succeed(null, null);
    }

    /** 로그인 안 됐으면 data=null (화면 분기용, 401 아님) */
    @GetMapping("/me")
    public ApiResult<?> me(HttpServletRequest req, HttpServletResponse res) {
        WifiMemberDto m = authService.currentMember(req, res);
        if (m == null) return ApiResult.succeed(null, null);
        Map<String, Object> r = new HashMap<>();
        r.put("tel", WifiAuthService.maskTel(m.getTel()));
        r.put("pointBalance", m.getPointBalance());
        r.put("pointExpireDt", m.getPointExpireDt());
        r.put("earnRate", pricing.earnRate()); // 결제 적립률(%) - 결제 바 "적립 예정" 표시용
        return ApiResult.succeed(r, null);
    }

    @DeleteMapping("/me")
    public ApiResult<?> withdraw(HttpServletRequest req, HttpServletResponse res) {
        authService.withdraw(authService.requireMember(req, res), res);
        return ApiResult.succeed(null, null);
    }

    @GetMapping("/me/points")
    public ApiResult<?> points(HttpServletRequest req, HttpServletResponse res) {
        return ApiResult.succeed(wifiService.myPointLogs(authService.requireMember(req, res)), null);
    }

    @GetMapping("/me/orders")
    public ApiResult<?> orders(HttpServletRequest req, HttpServletResponse res) {
        return ApiResult.succeed(wifiService.myOrders(authService.requireMember(req, res)), null);
    }

    /* ---------- 장비 ---------- */

    @GetMapping("/me/devices")
    public ApiResult<?> devices(HttpServletRequest req, HttpServletResponse res) {
        return ApiResult.succeed(wifiService.myDevices(authService.requireMember(req, res)), null);
    }

    @PostMapping("/me/devices")
    public ApiResult<?> register(@RequestBody Map<String, Object> body, HttpServletRequest req, HttpServletResponse res) {
        return ApiResult.succeed(wifiService.registerDevice(authService.requireMember(req, res), str(body.get("sn")), str(body.get("nickname"))), null);
    }

    @PostMapping("/me/devices/{sn}/nickname")
    public ApiResult<?> rename(@PathVariable String sn, @RequestBody Map<String, Object> body, HttpServletRequest req, HttpServletResponse res) {
        wifiService.renameDevice(authService.requireMember(req, res), sn, str(body.get("nickname")));
        return ApiResult.succeed(null, null);
    }

    @DeleteMapping("/me/devices/{sn}")
    public ApiResult<?> release(@PathVariable String sn, HttpServletRequest req, HttpServletResponse res) {
        wifiService.releaseDevice(authService.requireMember(req, res), sn);
        return ApiResult.succeed(null, null);
    }

    @GetMapping("/devices/{sn}")
    public ApiResult<?> device(@PathVariable String sn, HttpServletRequest req, HttpServletResponse res) {
        return ApiResult.succeed(wifiService.deviceDetail(authService.requireMember(req, res), sn), null);
    }

    @GetMapping("/devices/{sn}/settings")
    public ApiResult<?> deviceSettings(@PathVariable String sn, HttpServletRequest req, HttpServletResponse res) {
        return ApiResult.succeed(wifiService.deviceSettings(authService.requireMember(req, res), sn), null);
    }

    /** 장비 설정 변경 전 재인증 번호 발송 */
    @PostMapping("/devices/{sn}/settings/otp")
    public ApiResult<?> deviceSettingsOtp(@PathVariable String sn, HttpServletRequest req, HttpServletResponse res) {
        WifiMemberDto member = authService.requireMember(req, res);
        wifiService.deviceSettings(member, sn); // 내 장비인지 먼저 확인
        return ApiResult.succeed(authService.sendActionOtp(member, clientIp(req)), null);
    }

    @PostMapping("/devices/{sn}/settings")
    public ApiResult<?> updateDeviceSettings(@PathVariable String sn, @RequestBody Map<String, Object> body,
                                             HttpServletRequest req, HttpServletResponse res) {
        wifiService.updateDeviceSettings(authService.requireMember(req, res), sn, str(body.get("wifiPwd")),
            str(body.get("deviceLang")), str(body.get("blackList")), str(body.get("code")));
        return ApiResult.succeed(null, null);
    }

    /* ---------- 상품 ---------- */

    @GetMapping("/devices/{sn}/categories")
    public ApiResult<?> categories(@PathVariable String sn, HttpServletRequest req, HttpServletResponse res) {
        return ApiResult.succeed(wifiService.categories(authService.requireMember(req, res), sn), null);
    }

    @GetMapping("/devices/{sn}/products")
    public ApiResult<?> products(@PathVariable String sn, @RequestParam(required = false) Long categoryId,
                                 HttpServletRequest req, HttpServletResponse res) {
        return ApiResult.succeed(wifiService.products(authService.requireMember(req, res), sn, categoryId), null);
    }

    /** productCode 에 '/' 가 들어있어 쿼리 파라미터로 받음 */
    @GetMapping("/devices/{sn}/product")
    public ApiResult<?> product(@PathVariable String sn, @RequestParam String code, HttpServletRequest req, HttpServletResponse res) {
        return ApiResult.succeed(wifiService.productDetail(authService.requireMember(req, res), sn, code), null);
    }

    /* ---------- 주문 ---------- */

    @PostMapping("/orders")
    public ApiResult<?> createOrder(@RequestBody Map<String, Object> body, HttpServletRequest req, HttpServletResponse res) {
        int period = body.get("period") == null ? 1 : (int) Double.parseDouble(body.get("period").toString());
        int usePoint = body.get("usePoint") == null ? 0 : (int) Double.parseDouble(body.get("usePoint").toString());
        return ApiResult.succeed(wifiService.createOrder(authService.requireMember(req, res), str(body.get("sn")),
            str(body.get("productCode")), period, usePoint), null);
    }

    @GetMapping("/orders/{orderNo}")
    public ApiResult<?> order(@PathVariable String orderNo, HttpServletRequest req, HttpServletResponse res) {
        return ApiResult.succeed(wifiService.orderResult(authService.requireMember(req, res), orderNo), null);
    }

    @PostMapping("/orders/{orderNo}/refund")
    public ApiResult<?> requestRefund(@PathVariable String orderNo, @RequestBody Map<String, Object> body,
                                      HttpServletRequest req, HttpServletResponse res) {
        wifiService.requestRefund(authService.requireMember(req, res), orderNo, str(body.get("reason")));
        return ApiResult.succeed(null, null);
    }

    /* ---------- 공통 ---------- */

    @ExceptionHandler(WifiException.class)
    public ResponseEntity<ApiResult<?>> wifiError(WifiException e) {
        return ResponseEntity.status(e.getResponseCode().getHttpStatus()).body(ApiResult.failed(e.getMessage(), e.getResponseCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<?>> error(Exception e) {
        log.error("[WIFI-API] error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResult.failed("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", ResponseCode.ABNORMAL));
    }

    private static String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return StringUtils.isNotBlank(xff) ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }
}
