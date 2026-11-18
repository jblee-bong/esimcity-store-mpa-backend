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

    List<Map<String, Object>> mccList();
    void saveMcc(Map<String, Object> param);
    void deleteMcc(Long id);

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

    /**
     * 충전 완료 주문 환불 (전액/부분). refundKrw = 돌려줄 결제금액, refundPoint = 돌려줄 사용 포인트.
     * cancelTuge = TUGE 요금제도 취소. 결제금액 전부면 PayUp 전액취소, 일부면 PayUp 부분취소.
     */
    void processRefund(String orderNo, int refundKrw, int refundPoint, String memo, String adminId);

    /** TUGE 요금제 취소 (결제 환불과 따로). 사용 전이면 비용이 채널 잔액으로 돌아오고, 사용 중이면 조기 종료 */
    void cancelTugeOrder(String orderNo, String adminId);

    void rejectRefund(String orderNo, String memo, String adminId);

    Map<String, Object> balance();
}
