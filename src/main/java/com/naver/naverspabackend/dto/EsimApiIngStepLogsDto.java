package com.naver.naverspabackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsimApiIngStepLogsDto extends BaseDto{

    private Long id;
    private String esimLogs;
    private Long orderId;

}
