package com.naver.naverspabackend.dto;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Slf4j
public class ProductDto extends BaseDto{

    // id
    private Long originProductNo;

    private String originProductSellerManagementCode;

    private Long storeId;

    private String productName;

    private String statusType;

    private String representativeImageUrl;

    private Integer salePrice;
    private Integer discountedPrice;


    private String optionGroupName1;
    private String optionGroupName2;
    private String optionGroupName3;
    private String optionGroupName4;

    private boolean updateFlag;
    private boolean deleteFlag;
    private boolean insertFlag;

    private int relationProductOptionCnt;

    // N:1
    private StoreDto storeDto;

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }
        ProductDto productDto = (ProductDto) o;
        /**
         * clubId
         * bookingDay
         * bookingTime
         * courseCode
         */
        return originProductNo == productDto.getOriginProductNo();
    }

    @Override
    public int hashCode() {
        String s = originProductNo + storeId + productName;
        return 31 * s.hashCode() + originProductNo.intValue();
    }

    public boolean compare(ProductDto productDto){
        String originProductSellerManagementCodeNullSafe = Objects.toString(originProductSellerManagementCode, "");
        String productNameNullSafe = Objects.toString(productName, "");
        String statusTypeNullSafe = Objects.toString(statusType, "");
        String representativeImageUrlNullSafe = Objects.toString(representativeImageUrl, "");
        String optionGroupName1NullSafe = Objects.toString(optionGroupName1, "");
        String optionGroupName2NullSafe = Objects.toString(optionGroupName2, "");
        String optionGroupName3NullSafe = Objects.toString(optionGroupName3, "");


        if(
            !originProductSellerManagementCodeNullSafe.equals(Objects.toString(productDto.getOriginProductSellerManagementCode(), "")) ||
            !productNameNullSafe.equals(Objects.toString(productDto.getProductName(), "")) ||
            !statusTypeNullSafe.equals(Objects.toString(productDto.getStatusType(), "")) ||
            !representativeImageUrlNullSafe.equals(Objects.toString(productDto.getRepresentativeImageUrl(), "")) ||
            !optionGroupName1NullSafe.equals(Objects.toString(productDto.getOptionGroupName1(), "")) ||
            !optionGroupName2NullSafe.equals(Objects.toString(productDto.getOptionGroupName2(), "")) ||
            !optionGroupName3NullSafe.equals(Objects.toString(productDto.getOptionGroupName3(), "")) ||
            !salePrice.equals(productDto.getSalePrice()) ||
            !discountedPrice.equals(productDto.getDiscountedPrice())
        ){
            return false;
        }
        return true;
    }

}
