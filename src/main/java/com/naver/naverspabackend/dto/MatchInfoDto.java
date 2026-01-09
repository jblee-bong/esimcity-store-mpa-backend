package com.naver.naverspabackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.naver.naverspabackend.annotation.ExcelColumnName;
import com.naver.naverspabackend.util.DateConstants;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchInfoDto extends BaseDto{

    @ExcelColumnName(headerName="ID")
    private Long id;

    @ExcelColumnName(headerName="조건명")
    private String matchInfoName;


    @ExcelColumnName(headerName="상품ID")
    private Long originProductNo;

    @ExcelColumnName(headerName="옵션Id")
    private Long optionId;

    @ExcelColumnName(headerName = "옵션명")
    private String optionName;

    @ExcelColumnName(headerName = "옵션명1")
    private String optionName1;

    @ExcelColumnName(headerName = "옵션명2")
    private String optionName2;

    @ExcelColumnName(headerName = "옵션명3")
    private String optionName3;

    @ExcelColumnName(headerName = "옵션명4")
    private String optionName4;

    @ExcelColumnName(headerName = "메세지_타입")
    private String sendMethod;

    @ExcelColumnName(headerName = "카카오_TEMPLATE_KEY")
    private String kakaoTemplateKey;


    @ExcelColumnName(headerName = "MMS타이틀")
    private String title;

    @ExcelColumnName(headerName = "MMS본문")
    private String body;

    @ExcelColumnName(headerName = "SMS본문")
    private String smsBody;


    @ExcelColumnName(headerName = "메일제목")
    private String mailTitle;

    @ExcelColumnName(headerName = "메일본문")
    private String mailContents;



    @ExcelColumnName(headerName = "esim코드")
    private String esimCode;


    @ExcelColumnName(headerName = "esim카카오_TEMPLATE_KEY")
    private String eKakaoTemplateKey;


    @ExcelColumnName(headerName = "esimMMS타이틀")
    private String eTitle;
    @ExcelColumnName(headerName = "esimMMS본문")
    private String eBody;


    @ExcelColumnName(headerName = "esimSMS본문")
    private String eSmsBody;
    @ExcelColumnName(headerName = "esim메일제목")
    private String eMailTitle;
    @ExcelColumnName(headerName = "esim메일본문")
    private String eMailContents;


    @ExcelColumnName(headerName = "COMFIRM-SMS본문")
    private String comfirmSmsBody;


    @ExcelColumnName(headerName = "COMFIRM-MMS타이틀")
    private String comfirmTitle;

    @ExcelColumnName(headerName = "COMFIRM-MMS본문")
    private String comfirmBody;

    @ExcelColumnName(headerName = "COMFIRM-카카오_TEMPLATE_KEY")
    private String comfirmKakaoTemplateKey;


    @ExcelColumnName(headerName = "COMFIRM-메일제목")
    private String comfirmMailTitle;

    @ExcelColumnName(headerName = "COMFIRM-메일본문")
    private String comfirmMailContents;



    @ExcelColumnName(headerName = "esim_TYPE")
    private String esimType;

    @ExcelColumnName(headerName = "esim_상품_ID")
    private String esimProductId;

    @ExcelColumnName(headerName = "esim_상품_설명")
    private String esimDescription;

    @ExcelColumnName(headerName = "esim_상품_DAYS")
    private String esimProductDays;


    @ExcelColumnName(headerName = "발주처리_Y_N")
    private String orderingUseYn;

    @ExcelColumnName(headerName = "발송처리_Y_N")
    private String transUseYn;


    @ExcelColumnName(headerName = "생성일")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConstants.datePattern, timezone = DateConstants.timeZone)
    private Date createDt;


    @ExcelColumnName(headerName = "esim카카오_대체발송유무")
    private String eKakaoResendFlag;
    
    private Long mainId;

    private String originProductSellerManagementCode;

    private String productName;








    // pk


    // fk 옵션





    private String useYn;



    private ProductDto productDto;

    private ProductOptionDto productOptionDto;




    private Integer groupCnt;



    private String esimFlag;


    private String comfirmFlag;
    //return data 용


    public String geteKakaoTemplateKey() {
        return eKakaoTemplateKey;
    }

    public String geteTitle() {
        return eTitle;
    }

    public String geteBody() {
        return eBody;
    }

    public String geteSmsBody() {
        return eSmsBody;
    }

    public String geteMailContents() {
        return eMailContents;
    }

    public String geteMailTitle() {
        return eMailTitle;
    }


}
