package com.naver.naverspabackend.service.wifi;

import com.naver.naverspabackend.dto.WifiCategoryDto;
import com.naver.naverspabackend.dto.WifiDeviceDto;
import com.naver.naverspabackend.dto.WifiMemberDto;
import com.naver.naverspabackend.dto.WifiOrderDto;
import com.naver.naverspabackend.dto.WifiPointLogDto;
import com.naver.naverspabackend.dto.WifiProductDto;
import java.util.List;
import java.util.Map;

public interface WifiService {

    /** PayUp orderNumber 접두어. /external/payup/success 에서 이 접두어면 포켓와이파이 흐름으로 분기 */
    String ORDER_PREFIX = "WF";

    List<WifiDeviceDto> myDevices(WifiMemberDto member);

    /** 바로 내 장비로 등록 (먼저 등록한 계정이 주인). 다른 계정에 있으면 "이미 등록된 장비" */
    WifiDeviceDto registerDevice(WifiMemberDto member, String sn);

    void releaseDevice(WifiMemberDto member, String sn);

    /** 장비 상태(3.10, 캐시) + 요금제 목록(3.7, 캐시) */
    Map<String, Object> deviceDetail(WifiMemberDto member, String sn);

    List<WifiCategoryDto> categories(WifiMemberDto member, String sn);

    List<WifiProductDto> products(WifiMemberDto member, String sn, Long categoryId);

    WifiProductDto productDetail(WifiMemberDto member, String sn, String productCode);

    /** 주문 생성. period = 일(월)수 (기간선택 상품만, 나머지는 1). 금액 = 판매가 x period. 포인트로 전액이면 바로 충전 */
    Map<String, Object> createOrder(WifiMemberDto member, String sn, String productCode, int period, int usePoint);

    /** PayUp 결제 완료 콜백 처리 */
    String capturePayment(String transactionId, String orderNo, String amount);

    /** Thymeleaf 결제 페이지 모델 (PAY_READY 아니면 null) */
    Map<String, Object> payPageModel(String orderNo);

    Map<String, Object> orderResult(WifiMemberDto member, String orderNo);

    List<WifiOrderDto> myOrders(WifiMemberDto member);

    List<WifiPointLogDto> myPointLogs(WifiMemberDto member);

    /** TUGE 개통/주문. 재시도 배치에서도 호출 */
    void fulfill(WifiOrderDto order);

    /** 충전 실패 확정: PG 취소 + 포인트 복구 */
    void failAndRefund(WifiOrderDto order, String reason);
}
