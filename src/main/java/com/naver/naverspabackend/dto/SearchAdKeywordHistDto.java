package com.naver.naverspabackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.naver.naverspabackend.util.DateConstants;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchAdKeywordHistDto {

    private Long id;
    private Long keywordId;
    private String nccKeywordId;
    private String keyword;
    /** RAISE / LOWER */
    private String action;
    private Integer beforeBid;
    private Integer afterBid;
    private Integer targetPosition;
    private Double avgRnk;
    private Double deltaRnk;
    private Integer lastImpCnt;
    private Integer deltaImp;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConstants.datePattern, timezone = DateConstants.timeZone)
    private Date registDt;
}
