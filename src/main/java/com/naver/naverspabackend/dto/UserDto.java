package com.naver.naverspabackend.dto;

import com.naver.naverspabackend.enums.UserAuthority;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
@NoArgsConstructor
public class UserDto extends BaseDto{

    private String email;

    private String password;

    private String name;

    private String phone;

    private UserAuthority userAuthority;

    private List<UserStoreMappDto> userStoreMappDtoList;

    public UserDto(String email) {
        this.email = email;
    }

    public UserDto(String email, String userAuthority) {
        this.email = email;
        if(userAuthority.equals("ROLE_USER")){

            this.userAuthority = UserAuthority.ROLE_USER;
        }else{
            this.userAuthority = UserAuthority.ROLE_ADMIN;

        }
    }

    public UsernamePasswordAuthenticationToken toAuthentication() {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        return usernamePasswordAuthenticationToken;
    }
}
