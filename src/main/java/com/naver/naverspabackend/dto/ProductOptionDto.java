package com.naver.naverspabackend.dto;

import java.util.Objects;

import com.naver.naverspabackend.annotation.ExcelColumnName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductOptionDto extends BaseDto {

    // id
    @ExcelColumnName(headerName = "ID")
    private Long optionId;

    // fk
    @ExcelColumnName(headerName = "상품번호")
    private Long originProductNo;
    @ExcelColumnName(headerName = "판매자_상품코드")
    private String originProductSellerManagementCode;
    @ExcelColumnName(headerName = "옵션명1")
    private String optionName1;
    @ExcelColumnName(headerName = "옵션명2")
    private String optionName2;
    @ExcelColumnName(headerName = "옵션명3")
    private String optionName3;
    @ExcelColumnName(headerName = "가격")
    private Integer price;
    @ExcelColumnName(headerName = "사용유무")
    private Boolean usable;
    //
    private Long storeId;


    private String optionName4;


    private boolean updateFlag;
    private boolean deleteFlag;
    private boolean insertFlag;

    // N:1
    private ProductDto productDto;


    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }
        ProductOptionDto productOptionDto = (ProductOptionDto) o;
        /**
         * clubId
         * bookingDay
         * bookingTime
         * courseCode
         */
        return optionId == productOptionDto.getOptionId();
    }

    @Override
    public int hashCode() {
        String s = optionId + originProductNo + "";
        return 31 * s.hashCode() + optionId.intValue();
    }

    public boolean compare(ProductOptionDto productOptionDto){

        String optionName1NullSafe = Objects.toString(optionName1, "");
        String optionName2NullSafe = Objects.toString(optionName2, "");
        String optionName3NullSafe = Objects.toString(optionName3, "");

        if(!optionName1NullSafe.equals(Objects.toString(productOptionDto.getOptionName1(), "")) ||
            !optionName2NullSafe.equals(Objects.toString(productOptionDto.getOptionName2(), "")) ||
            !optionName3NullSafe.equals(Objects.toString(productOptionDto.getOptionName3(), "")) ||
            !price.equals(productOptionDto.getPrice()) ||
            !usable.equals(productOptionDto.getUsable())
        ){
            return false;
        }
        return true;
    }

}
