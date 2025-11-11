package com.example.demo.config.auth.loginHandler;

import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.config.auth.jwt.JWTProperties;
import com.example.demo.config.auth.jwt.JWTTokenProvider;
import com.example.demo.config.auth.jwt.TokenInfo;
import com.example.demo.domain.entity.JwtToken;
import com.example.demo.domain.repository.JwtTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    JWTTokenProvider jwtTokenProvider; //토큰 생성기 연결

    @Autowired
    JwtTokenRepository jwtTokenRepository; //토큰 레파지토리 연결(DB로 저장하는 연습때만 사용)

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        log.info("CustomSuccessHandler's onAuthenticationSuccess invoke...!");

        //TOKEN을 COOKIE로 전달 - 로그인 최초 한 번만 전달
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        //tokenInfo에는 AccessToken과 RefreshToken이 있는 상태

        //브라우저 쿠키로 던져주기     //JWTProperties내의 기준값 이용
        Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME, tokenInfo.getAccessToken());
        cookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME);    //accesstoken 유지시간
        cookie.setPath("/");    //쿠키 적용경로(/:모든 경로)
        response.addCookie(cookie); //응답정보에 쿠키 포함

        //Role 꺼내기
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        String auth = principalDetails.getDto().getRole();

        //TOKEN을 DB로 저장
        JwtToken tokenEntity = JwtToken.builder()
                .accessToken(tokenInfo.getAccessToken())
                .refreshToken(tokenInfo.getRefreshToken())
                .username(authentication.getName())
                .auth(auth) //role(정보)꺼내기
                .createAt(LocalDateTime.now())
                .build();
        jwtTokenRepository.save(tokenEntity);


        log.info("CustomSuccessHandler's onAuthenticationSuccess invoke..genToken.." + tokenInfo);

        //Role 별로 redirect 경로 수정
        String redirectUrl = "/"; // 기본경로 : 최상위 경로
//        for(GrantedAuthority authority :  authentication.getAuthorities())
//        {
//            log.info("authority:" + authority);
//            String role = authority.getAuthority(); //String
//
//
//            //높은 권한 위주로 판단
//            if(role.contains("ROLE_ADMIN")){
//                // /admin 리다이렉트
//                redirectUrl = "/admin";
//                break;
//            }else if((role.contains("ROLE_MANAGER"))){
//                // /manager 리다이렉트
//                redirectUrl = "/manager";
//                break;
//            }else{
//                // /user 리다이렉트
//                redirectUrl ="/user";
//                break;
//            }


//        }
        response.sendRedirect(redirectUrl);
    }
}


