package com.naver.naverspabackend.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WifiPointLogDto {
    private Long id;
    private Long memberId;
    private int amount;
    private String reason;      // GRANT/USE/REFUND/EXPIRE/ADMIN/WITHDRAW
    private Long orderId;
    private String adminId;
    private String memo;
    private Date registDt;
}
