package com.naver.naverspabackend.dto;

public class ChannelProductDto extends BaseDto{

    // 채널 product id
    private Long channelProductNo;

    // origin product id FK references to TB_PRODUCT
    private Long originProductNo;

    private String channelServiceType;

    private String categoryId;

    private String channelProductName;

    private String sellerManagementCode;


}
