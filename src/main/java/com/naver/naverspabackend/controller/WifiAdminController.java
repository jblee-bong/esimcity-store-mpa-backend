package com.naver.naverspabackend.controller;

import com.naver.naverspabackend.annotation.TokenUser;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.dto.WifiCategoryDto;
import com.naver.naverspabackend.dto.WifiProductDto;
import com.naver.naverspabackend.exception.WifiException;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.wifi.WifiAdminService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 포켓와이파이 관리자 API. SecurityConfig 에서 /wifi-admin/** 는 ROLE_ADMIN 만 허용 */
@RestController
@RequestMapping("/wifi-admin")
@RequiredArgsConstructor
public class WifiAdminController {

    private final WifiAdminService adminService;

    /* ---------- 상품 ---------- */

    @PostMapping("/product/list")
    public ApiResult<?> productList(@RequestBody Map<String, Object> param) {
        return ApiResult.succeed(adminService.productList(param), null);
    }

    @PostMapping("/product/sync")
    public ApiResult<?> productSync() {
        return ApiResult.succeed(adminService.syncProducts(), null);
    }

    @PostMapping("/product/detail")
    public ApiResult<?> productDetail(@RequestBody Map<String, Object> param) {
        return ApiResult.succeed(adminService.productDetail(String.valueOf(param.get("productCode"))), null);
    }

    @PostMapping("/product/update")
    public ApiResult<?> productUpdate(@RequestBody WifiProductDto dto) {
        adminService.updateProduct(dto);
        return ApiResult.succeed(null, null);
    }

    /* ---------- 카테고리 ---------- */

    @PostMapping("/category/list")
    public ApiResult<?> categoryList() {
        return ApiResult.succeed(adminService.categoryList(), null);
    }

    @PostMapping("/category/save")
    public ApiResult<?> categorySave(@RequestBody WifiCategoryDto dto) {
        return ApiResult.succeed(adminService.saveCategory(dto), null);
    }

    @PostMapping("/category/delete")
    public ApiResult<?> categoryDelete(@RequestBody Map<String, Object> param) {
        adminService.deleteCategory(Long.valueOf(param.get("id").toString()));
        return ApiResult.succeed(null, null);
    }

    /* ---------- 장비 ---------- */

    @PostMapping("/device/list")
    public ApiResult<?> deviceList(@RequestBody Map<String, Object> param) {
        return ApiResult.succeed(adminService.deviceList(param), null);
    }

    @PostMapping("/device/sync")
    public ApiResult<?> deviceSync() {
        return ApiResult.succeed(adminService.syncDevices(), null);
    }

    @PostMapping("/device/release")
    public ApiResult<?> deviceRelease(@RequestBody Map<String, Object> param, @TokenUser UserDto admin) {
        adminService.releaseDevice(String.valueOf(param.get("sn")), admin.getEmail());
        return ApiResult.succeed(null, null);
    }

    /* ---------- 회원/포인트 ---------- */

    @PostMapping("/member/list")
    public ApiResult<?> memberList(@RequestBody Map<String, Object> param) {
        return ApiResult.succeed(adminService.memberList(param), null);
    }

    @PostMapping("/member/points")
    public ApiResult<?> pointLogs(@RequestBody Map<String, Object> param) {
        return ApiResult.succeed(adminService.pointLogs(Long.valueOf(param.get("memberId").toString())), null);
    }

    @PostMapping("/member/point/adjust")
    public ApiResult<?> adjustPoint(@RequestBody Map<String, Object> param, @TokenUser UserDto admin) {
        adminService.adjustPoint(Long.valueOf(param.get("memberId").toString()),
            (int) Double.parseDouble(param.get("amount").toString()), (String) param.get("memo"), admin.getEmail());
        return ApiResult.succeed(null, null);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/member/point/grant")
    public ApiResult<?> grantPoints(@RequestBody Map<String, Object> param, @TokenUser UserDto admin) {
        List<Map<String, Object>> rows = (List<Map<String, Object>>) param.get("rows");
        if (rows == null || rows.isEmpty()) throw new WifiException("지급할 데이터가 없습니다.");
        return ApiResult.succeed(adminService.grantPoints(rows, (String) param.get("memo"), admin.getEmail()), null);
    }

    /* ---------- 주문 ---------- */

    @PostMapping("/order/list")
    public ApiResult<?> orderList(@RequestBody Map<String, Object> param) {
        return ApiResult.succeed(adminService.orderList(param), null);
    }

    @PostMapping("/order/retry")
    public ApiResult<?> orderRetry(@RequestBody Map<String, Object> param) {
        adminService.retryOrder(String.valueOf(param.get("orderNo")));
        return ApiResult.succeed(null, null);
    }

    @PostMapping("/order/refund")
    public ApiResult<?> orderRefund(@RequestBody Map<String, Object> param, @TokenUser UserDto admin) {
        adminService.refundOrder(String.valueOf(param.get("orderNo")), admin.getEmail());
        return ApiResult.succeed(null, null);
    }

    /** 충전 완료 주문 환불 (전액/부분) */
    @PostMapping("/order/refund/process")
    public ApiResult<?> refundProcess(@RequestBody Map<String, Object> param, @TokenUser UserDto admin) {
        adminService.processRefund(String.valueOf(param.get("orderNo")), (int) num(param.get("refundKrw")), (int) num(param.get("refundPoint")),
            Boolean.TRUE.equals(param.get("cancelTuge")),
            param.get("memo") == null ? null : String.valueOf(param.get("memo")), admin.getEmail());
        return ApiResult.succeed(null, null);
    }

    @PostMapping("/order/refund/reject")
    public ApiResult<?> refundReject(@RequestBody Map<String, Object> param, @TokenUser UserDto admin) {
        adminService.rejectRefund(String.valueOf(param.get("orderNo")), param.get("memo") == null ? null : String.valueOf(param.get("memo")), admin.getEmail());
        return ApiResult.succeed(null, null);
    }

    @PostMapping("/balance")
    public ApiResult<?> balance() {
        Map<String, Object> r = adminService.balance();
        r.put("exchangeRate", adminService.exchangeRate());
        return ApiResult.succeed(r, null);
    }

    /** 관리자 화면은 HTTP 200 + responseCode 로 오류를 판단하므로 상태코드는 200 유지 */
    private static double num(Object o) {
        try {
            return o == null ? 0 : Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            throw new WifiException("숫자를 확인해주세요.");
        }
    }

    @ExceptionHandler(WifiException.class)
    public ResponseEntity<ApiResult<?>> wifiError(WifiException e) {
        return ResponseEntity.ok(ApiResult.failed(e.getMessage(), e.getResponseCode()));
    }
}
