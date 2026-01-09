package com.naver.naverspabackend.service.auth;


import com.naver.naverspabackend.dto.TokenDto;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.dto.wrapper.TokenUserWrapper;
import com.naver.naverspabackend.enums.UserAuthority;
import com.naver.naverspabackend.exception.CustomException;
import com.naver.naverspabackend.response.model.ResponseCode;
import com.naver.naverspabackend.security.token.TokenProvider;
import com.naver.naverspabackend.service.user.UserService;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class AuthBusinessLogic {

    private final AuthService authService;
    private final UserService userService;
    private final TokenProvider tokenProvider;

    public void createMember(UserDto user) {
        if (!userService.isValidStateForRegisterId(user)) {
            throw new CustomException("가입이 불가능합니다. 관리자에게 문의 바랍니다.");
        }
        userService.createUser(user);
    }

    public TokenUserWrapper login(UserDto userRequestDto) {
        Authentication authentication = null;
        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = userRequestDto.toAuthentication();
        authentication = null;

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        authentication = authService.authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenDto tokenDto = authService.generateTokenDto(authentication);

        UserDto user = userService.findUserByEmail(userRequestDto.getEmail());

        TokenUserWrapper tokenUserWrapper = new TokenUserWrapper(tokenDto, user);

        // 5. 토큰 발급
        return tokenUserWrapper;
    }



    public TokenUserWrapper reissue(TokenDto tokenDto) {
        log.error("reissue 호출 ----------------------------------------------------------");
        // 1. Refresh Token 검증
        if (!tokenProvider.validateToken(tokenDto.getRefreshToken())) {
            throw new CustomException("Refresh Token 이 유효하지 않습니다.");
        }

        // 2. Access Token 에서 Member ID 가져오기
        Authentication authentication = tokenProvider.getAuthentication(tokenDto.getRefreshToken());

        // 3. 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져옴
        //RefreshToken refreshToken = refreshTokenService.findByKey(authentication.getName()).orElseThrow(() -> new CustomException("로그아웃 된 사용자입니다."));

        // 4. Refresh Token 일치하는지 검사
        //if (!refreshToken.getValue().equals(tokenDto.getRefreshToken())) {
        //    throw new CustomException("로그인 정보가 일치하지 않습니다.");
        //}
        // 5. 새로운 토큰 생성
        TokenDto tokenDtoTo = tokenProvider.generateTokenDto(authentication);

        // 6. 저장소 정보 업데이트
        //refreshToken.setValue(tokenDto1.getRefreshToken());
        //refreshTokenService.save(refreshToken);

        UserDto user = userService.findUserByEmail(authentication.getName());

        TokenUserWrapper tokenUserWrapper = new TokenUserWrapper(tokenDtoTo, user);

        // 토큰 발급
        return tokenUserWrapper;
    }

    public TokenUserWrapper adminLogin(UserDto userRequestDto, HttpServletResponse response) {
        TokenUserWrapper tokenUserWrapper = null;
        try {
            Authentication authentication = null;
            // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
            UsernamePasswordAuthenticationToken authenticationToken = userRequestDto.toAuthentication();

            // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
            //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
            authentication = authService.authenticate(authenticationToken);

            // 3. 인증 정보를 기반으로 JWT 토큰 생성
            TokenDto tokenDto = authService.generateTokenDto(authentication);

            UserDto user = userService.findUserByEmail(userRequestDto.getEmail());

            /*
            if (!UserAuthority.ROLE_ADMIN.equals(user.getUserAuthority())){
                throw new CustomException("접근권한이 없습니다.");
            }*/

            tokenUserWrapper = new TokenUserWrapper(tokenDto, user);
        }catch (BadCredentialsException e) {
            log.error("로그인 에러 :{}", e.getMessage());
            throw new CustomException(ResponseCode.USER_NOT_FOUND);
        } catch (Exception e ) {
            log.error("로그인 에러 :{}", e.getMessage());
            throw new CustomException(e.getMessage());
        }

        // 5. 토큰 발급
        return tokenUserWrapper;
    }

}
