package com.naver.naverspabackend.dto;

import com.naver.naverspabackend.enums.UserAuthority;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
@Setter
@NoArgsConstructor
public class UserStoreMappDto extends BaseDto{

    private UserDto userDto;

    private StoreDto storeDto;

}
