package com.naver.naverspabackend.service.wifi;

import com.naver.naverspabackend.dto.WifiCategoryDto;
import com.naver.naverspabackend.dto.WifiPointLogDto;
import com.naver.naverspabackend.dto.WifiProductDto;
import java.util.List;
import java.util.Map;

public interface WifiAdminService {

    double exchangeRate();

    /* 상품 */
    Map<String, Object> productList(Map<String, Object> param);
    Map<String, Object> syncProducts();
    WifiProductDto productDetail(String productCode);
    void updateProduct(WifiProductDto dto);

    /* 카테고리 */
    List<WifiCategoryDto> categoryList();
    WifiCategoryDto saveCategory(WifiCategoryDto dto);
    void deleteCategory(Long id);

    /* 장비 */
    Map<String, Object> deviceList(Map<String, Object> param);
    Map<String, Object> syncDevices();
    void releaseDevice(String sn, String adminId);

    /* 회원/포인트 */
    Map<String, Object> memberList(Map<String, Object> param);
    List<WifiPointLogDto> pointLogs(Long memberId);
    void adjustPoint(Long memberId, int amount, String memo, String adminId);
    /** rows: [{tel, point}] (와디즈 후원자 엑셀). 미가입 번호는 PRE 회원으로 생성 */
    Map<String, Object> grantPoints(List<Map<String, Object>> rows, String memo, String adminId);
    void expirePoints();

    /* 주문 */
    Map<String, Object> orderList(Map<String, Object> param);
    void retryOrder(String orderNo);
    void refundOrder(String orderNo, String adminId);

    Map<String, Object> balance();
}
