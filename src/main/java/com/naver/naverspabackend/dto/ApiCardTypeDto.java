package com.naver.naverspabackend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApiCardTypeDto extends BaseDto {

    private String cardType;//카드타입

    private String timeZone;//타임존

    private boolean renewYn;//충전가능여부

    private boolean supportGetUsageYn;//사용량조회가능여부

    private Integer renewCount; // 확인필요. 예약충전가능한지아닌지같긴한데


    public ApiCardTypeDto() {
        super();
    }
}
