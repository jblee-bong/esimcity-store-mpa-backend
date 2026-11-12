package com.naver.naverspabackend.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsimInstallTokenDto {
    private Long id;
    private String token;
    private Long orderId;
    private String payloadJson;
    private Date expireDt;
    private Date registDt;
}
