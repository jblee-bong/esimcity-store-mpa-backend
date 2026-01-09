package com.naver.naverspabackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.naver.naverspabackend.util.DateConstants;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConstants.datePattern, timezone = DateConstants.timeZone)
    private Date registDt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConstants.datePattern, timezone = DateConstants.timeZone)
    private Date modifyDt;

    private String delYn;

}
