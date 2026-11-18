package com.naver.naverspabackend.dto;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WifiOrderDto {
    private Long id;
    private String orderNo;
    private Long memberId;
    private String memberTel;       // 관리자 조회용
    private String deviceSn;
    private String productCode;
    private Long optionId;
    private String productName;
    private int orderPeriod;
    private String startDate;       // yyyy-MM-dd (KST). 날짜 지정 상품(ACTIVEDBYORDER)만, 그 외 null
    private int totalKrw;
    private int pointUsed;
    private int payKrw;
    private int earnPoint;          // 충전 완료 시 적립된 포인트
    private BigDecimal settlementUsd;
    private String paymentStatus;   // PAY_READY/PAID/POINT_ONLY/PAY_FAILED/REFUNDED/PARTIAL_REFUNDED/REFUND_FAILED
    private String topupStatus;     // READY/RETRY/DONE/FAILED
    private String refundStatus;    // null/REQUESTED/DONE/REJECTED
    private String refundReason;
    private Date refundRequestDt;
    private int refundKrw;
    private int refundPoint;
    private String refundMemo;
    private Date refundDt;
    private String pgTransactionId;
    private String tugeOrderNo;
    private String tugeFlowOrderNos;
    private Date tugeCancelDt;      // TUGE 요금제 취소 시각 (결제 환불과 별개)
    private String planStatus;      // TUGE 요금제 상태 NOTACTIVE/INUSE/EXPIRED/GONE (WifiPlanChecker 가 1시간마다 갱신)
    private String planStartDt;     // 사용 시작 KST yyyy-MM-dd HH:mm
    private String planEndDt;       // 사용 종료 KST
    private Date expireNotifiedDt;  // 종료 하루 전 알림 보낸 시각
    private int retryCount;
    private String lastError;
    private Date registDt;
    private Date modifyDt;
}
