package com.naver.naverspabackend.dto.wrapper;

import com.naver.naverspabackend.dto.TokenDto;
import com.naver.naverspabackend.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenUserWrapper {

    private TokenDto tokenDto;

    private UserDto userDto;

}
