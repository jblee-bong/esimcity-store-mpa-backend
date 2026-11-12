package com.naver.naverspabackend.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WifiMemberDto {
    private Long id;
    private String tel;
    private String kakaoId;
    private String status;          // PRE / ACTIVE / DELETED
    private int pointBalance;
    private Date pointExpireDt;
    private int tokenVersion;
    private Date privacyAgreeDt;
    private String marketingAgreeYn;
    private Date registDt;
    private Date lastLoginDt;
    private int deviceCount;        // 목록 조회용
}
