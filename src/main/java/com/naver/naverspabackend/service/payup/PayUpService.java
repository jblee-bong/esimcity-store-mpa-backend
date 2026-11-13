package com.naver.naverspabackend.service.payup;

import org.springframework.ui.Model;

import java.util.Map;

public interface PayUpService {

    Map<String, Object> createOrder(Map<String,Object> params) throws Exception;

    void captureOrder(Model model, String transactionId, String token, String amount);

    /** 결제 승인 확인. "SUCCESS" 면 성공 (포켓와이파이용) */
    String confirmPayment(String transactionId, String orderNumber, String amount);

    /** 결제 취소. "SUCCESS" 면 성공 (포켓와이파이용) */
    String cancelPayment(String transactionId);

    /** 결제 부분취소. 성공하면 부분취소 거래번호, 실패하면 null (포켓와이파이용) */
    String partCancelPayment(String transactionId, int cancelAmount, String cancelReason);

    /** 결제창 SDK 에 넘길 merchantId, redirectUrl */
    Map<String, Object> getPayConfig();

}
