package com.naver.naverspabackend.response.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
@JsonFormat(shape = Shape.OBJECT)
public enum ResponseCode {

    SUCC(HttpStatus.OK, "20000", "성공"),


    /* 400 BAD_REQUEST : 잘못된 요청 */
    /* 401 UNAUTHORIZED : 인증되지 않은 사용자 */

    /* 404 NOT_FOUND : Resource를 찾을 수 없음 */
    //common
    /* 409 : CONFLICT : Resource의 현재 상태와 충돌. 보통 중복된 데이터 존재 */
    NOT_EXIST(HttpStatus.BAD_REQUEST, "C0001","데이터가 존재하지 않습니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "C0002","데이터가 이미 존재합니다."),
    ABNORMAL(HttpStatus.BAD_REQUEST, "C0003","비정상적인 접근입니다."),


    //user
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U0001","해당하는 정보의 사용자를 찾을 수 없습니다."),
    NOT_SAME_PASSWORD(HttpStatus.BAD_REQUEST, "U0002","패스워드가 일치하지 않습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A0001", "인증이 필요합니다."),
    COUNTERFEIT(HttpStatus.UNAUTHORIZED, "A0002", "위조 및 변조된 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A0003", "만료된 토큰입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A0004", "권한이없는 접근입니다."),
    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}