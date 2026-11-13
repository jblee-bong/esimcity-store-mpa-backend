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
    private int retryCount;
    private String lastError;
    private Date registDt;
    private Date modifyDt;
}
