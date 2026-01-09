package com.naver.naverspabackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeDto extends BaseDto{

    private String codeCd;
    private String codeGroup;
    private String codePrefix;
    private String codeName;
    private String codeSubName;
    private String codeValue;
    private String useYn;
    private int codeOrder;

}
