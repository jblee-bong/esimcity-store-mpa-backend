package com.naver.naverspabackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.naver.naverspabackend.util.DateConstants;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchAdShoppingDto extends BaseDto {

    private Long id;
    private String nccAdId;
    private String adName;
    private String useYn;
    private Integer targetPosition;
    private Integer minBid;
    private Integer maxBid;
    private Integer currentBid;
    private Integer estimateBid;
    /** 오늘 누적 평균노출순위 */
    private Double avgRnk;
    /** 직전 스냅샷 누적 노출수 */
    private Integer prevImpCnt;
    /** 직전 스냅샷 누적 평균순위 */
    private Double prevAvgRnk;
    /** 최근 조회 누적 노출수 */
    private Integer lastImpCnt;
    /** 구간(약 1시간) 역산 평균순위 */
    private Double deltaRnk;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConstants.datePattern, timezone = DateConstants.timeZone)
    private Date lastCheckedDt;
    private String memo;
}
