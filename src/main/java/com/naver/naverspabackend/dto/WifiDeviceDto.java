package com.naver.naverspabackend.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WifiDeviceDto {
    private String sn;
    private String deviceType;
    private String tugeStatus;
    private Long memberId;
    private String memberTel;       // 조인 조회용
    private String qrUrl;           // 관리자 목록용: TUGE 에 전달할 QR URL
    private Date registeredDt;
    private String memo;
    private Date registDt;
    private Date modifyDt;
}
