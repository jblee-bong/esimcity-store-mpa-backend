package com.naver.naverspabackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.naver.naverspabackend.annotation.ExcelColumnName;
import com.naver.naverspabackend.util.DateConstants;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderDto extends BaseDto {

    //excel
    @ExcelColumnName(headerName = "상품번호")
    private Long originProductNo;

    /**
     * order 관련
     */
    // order id 주문
    @ExcelColumnName(headerName = "주문번호")
    private String orderId;

    @ExcelColumnName(headerName = "판매자_상품코드")
    private String originProductSellerManagementCode;

    @ExcelColumnName(headerName = "판매수량")
    private Integer quantity; // 주문 수량


    @ExcelColumnName(headerName = "주문수량")
    private Integer allQuantity; // 주문 수량



    @ExcelColumnName(headerName = "취소수량")
    private Integer cancelQuantity; // 주문 수량



    @ExcelColumnName(headerName = "판매가")
    private String totalPaymentAmount; //할인가


    // orderName
    @ExcelColumnName(headerName = "구매자")
    private String ordererName;


    //배송지 전화번호1
    @ExcelColumnName(headerName = "수령인_번호")
    private String shippingTel1;
    // 주문자 연락처
    @ExcelColumnName(headerName = "주문자_번호")
    private String ordererTel;

    @ExcelColumnName(headerName = "결제일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConstants.datePattern, timezone = DateConstants.timeZone)
    private Date paymentDate;

    @ExcelColumnName(headerName = "발송결과")
    private String sendStatusNm;





    @ExcelColumnName(headerName = "옵션명1")
    private String optionName1;
    @ExcelColumnName(headerName = "옵션명2")
    private String optionName2;
    @ExcelColumnName(headerName = "옵션명3")
    private String optionName3;


    @ExcelColumnName(headerName = "전체옵션")
    private String productOption;
    
    @ExcelColumnName(headerName = "추가상품")
    private String productName;

    // option id
    @ExcelColumnName(headerName = "옵션번호")
    private Long optionId;

    @ExcelColumnName(headerName = "esim_업체")
    private String esimCorp; //이심 업체

    @ExcelColumnName(headerName = "esim_ID")
    private String esimProductId;


    @ExcelColumnName(headerName = "esim_ICCID")
    private String esimIccid;


    @ExcelColumnName(headerName = "esim_설명")
    private String esimDescription;


    @ExcelColumnName(headerName = "esim_상품_DAYS")
    private String esimProductDays;

    @ExcelColumnName(headerName = "발송방법(성공)")
    private String sendMethod;

    @ExcelColumnName(headerName = "발송방법(실패)")
    private String failMethod;



    @ExcelColumnName(headerName = "재 발송방법(성공)")
    private String reSendMethod;

    @ExcelColumnName(headerName = "재 발송방법(실패)")
    private String reFailMethod;



    @ExcelColumnName(headerName = "발송일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConstants.datePattern, timezone = DateConstants.timeZone)
    private Date sendDateTime;

    @ExcelColumnName(headerName = "가격")
    private String orderAllPrice;
    @ExcelColumnName(headerName = "단위")
    private String orderPriceCurrency;


    private String shipmentBoxId;

    // productOrder id 상품주문
    private String productOrderId;



    private Integer orderingUseStatus;

    private String transUseYn;

    private String transSuccessYn;

    private String transMail; //메일주소




    private String esimActivationCode; //이심 코드









    private String deliveryFeeAmount; //택배비

    private String optionName4;



    private String esimQrcodeImgUrl;

    private String esimQrcodeImgText;

    private String esimRcode;

    // id auto_increment
    private Long id;

    //
    private Long storeId;

    /**
     * product 관련
     */



    //발송결과
    private String sendStatus;


    //발송결과
    private String beforeSendStatus;


    private String unitPrice; //원가

    private String changeUnitPrice; //원가


    private String changeTotalPaymentAmount; //할인가





    private String esimApn;





    // 주문자 id
    private String ordererId;

    // 주문자 번호
    private String ordererNo;


    //esimaccess용 조회번호
    private String esimTranNo;


    private String shippingMemo;


    private boolean updateFlag;
    private boolean deleteFlag;
    private boolean insertFlag;


    private String esimApiRequestId;


    // 배송지정보
    //배송지 주소
    private String shippingBaseAddress;
    //배송지 우편번호
    private String shippingZipCode;
    //배송지 상세주소
    private String shippingDetailedAddress;
    //배송지 전화번호2
    private String shippingTel2;
    //배송지 대상자 이름
    private String shippingName;


    //카카오 재발송 리스트
    private List<KakaoContentsDto> kakaoContentsDtoList;

    //ESIM API 로그 테이블 리스트
    private List<EsimApiIngStepLogsDto> esimApiIngStepLogsDtoList;

    //return data 용


    //구매확정
    private String orderDecidedStatus; // 0: 확정전 1 :확정완료

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConstants.datePattern, timezone = DateConstants.timeZone)
    private Date orderDecidedDate; //확정완료일시


    private String sendDecidedMethod; //확정발송 성공 내역

    private String failDecidedMethod;//확정발송 실패 내역


    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }
        OrderDto orderDto = (OrderDto) o;
        /**
         * clubId
         * bookingDay
         * bookingTime
         * courseCode
         */
        return orderId == orderDto.getOrderId();
    }

    @Override
    public int hashCode() {
        String s = orderId + originProductNo + "";
        return 31 * s.hashCode() + id.intValue();
    }

}
