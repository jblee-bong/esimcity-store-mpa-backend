package com.naver.naverspabackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.naver.naverspabackend.util.DateConstants;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchAdKeywordDto extends BaseDto {

    private Long id;
    private String nccKeywordId;
    private String keyword;
    /** Y/N — 자동입찰 대상 */
    private String useYn;
    private Integer targetPosition;
    private Integer minBid;
    private Integer maxBid;
    /** Job이 조회/반영한 실제 입찰가 */
    private Integer currentBid;
    /** 참고용 추정입찰가(delta 모드에서는 미사용 가능) */
    private Integer estimateBid;
    /** 오늘 누적 평균노출순위 */
    private Double avgRnk;
    private Integer prevImpCnt;
    private Double prevAvgRnk;
    private Integer lastImpCnt;
    private Double deltaRnk;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConstants.datePattern, timezone = DateConstants.timeZone)
    private Date lastCheckedDt;
    private String memo;
}
