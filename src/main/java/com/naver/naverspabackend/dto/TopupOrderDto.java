package com.naver.naverspabackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.naver.naverspabackend.util.DateConstants;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TopupOrderDto extends BaseDto {

    private Long id;
    private Long storeId;
    private Long orderId;
    private Integer topupStatus; // 0:충전전, 1:충전완료, 2:충전실패
    private Integer paymentStatus; // 0: 결제전1:결제완료  2:결제실패 3: 환불완료 4:환불실패

    private String esimCorp; //이심 업체
    private String esimIccid;

    private String shippingTel; //이심 업체
    private String shippingName; //이심 업체

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConstants.datePattern, timezone = DateConstants.timeZone)
    private Date sendDateTime;


    private String topupUsdprice; // 충전달러가격
    private String topupKrwprice; // 충전한국가격


    private String shippingMail; //이심 업체

    private String topupParamJson;//각회사별 필요 JSON 데이터
    private String productOption;
    private String tokenId;


    private String topupOrderNo; //충전주문번호
}
