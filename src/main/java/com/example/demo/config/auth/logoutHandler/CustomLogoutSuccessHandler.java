package com.example.demo.config.auth.logoutHandler;

import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.config.auth.jwt.JWTProperties;
import com.example.demo.domain.repository.JwtTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;


@Slf4j
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {



    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")//application.properties에서 경로값 가져오기
                                                                           //properties 내용을 그대로 긁어서 KAKAO_CLIENT_ID에 삽입해주는 역할
    private String KAKAO_CLIENT_ID;

    @Value("${spring.security.oauth2.client.registration.kakao.logout.redirect.uri}")//application.properties에서 경로값 가져오기
    private String KAKAO_REDIRECT_URI;
    //OAuth2 연결 끊는 작업 담당

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    //로컬서버 로그아웃 이후 추가 처리(ex. 카카오인증서버 연결해제...)
    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "jpaTransactionManager")
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("CustomLogoutSuccessHandler's onLogoutSuccess invoke...!" + authentication);

        //AccessToken cookie 삭제
        String token=null; //access-token 쿠키 받아 token=null;
        Cookie[] cookies = request.getCookies(); //cookie가 null일수도 있기에 예외를 방지하고자
        //request.getCookies(); //사용자가 요청하게 되면 브러우저에서 전달하는 모든 쿠키를 확인 가능
        if(cookies!=null) {

            //배열이기에 쿠키 내의 accessToken이라고 하는 쿠키만 빼내오기
            token = Arrays.stream(cookies) //배열이라도 stream함수를 통해 filter, 재구성 작업 가능해짐
                    .filter((cookie) -> {
                        return cookie.getName().equals(JWTProperties.ACCESS_TOKEN_COOKIE_NAME);//배열형태의 쿠키들을 하나씩 가져옴
                        //조건식 : 받아온 쿠키가 JWTPorperties의 쿠키 이름과 일치하는지 여부 확인
                    })
                    .findFirst() //배열리스트로 반환된 쿠키 중 하나를 꺼내와서
                    .map((cookie) -> {
                        return cookie.getValue();//그 쿠키에 대한  value값 꺼내옴
                    })
                    .orElse(null); //없으면 null값 반환
        }
        if(token!=null) {
            //db 제거
            jwtTokenRepository.deleteByAccessToken(token); //토큰 전달해서 해당 행 삭제
            //access-token 쿠키 제거(자동제거는 됨)
            Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME, null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }









        //OAUTH2 확인
        //Authentication 정보 확인
        PrincipalDetails principalDetails = (PrincipalDetails)authentication.getPrincipal();

        //provider, dto 꺼내기
        String provider = principalDetails.getDto().getProvider();
        System.out.println("provider : " + provider);

        if (provider!=null && provider.startsWith("Kakao")) {
            System.out.println("!!!" + KAKAO_CLIENT_ID + " " + KAKAO_REDIRECT_URI);                                                //로그아웃 이후 이동할 위치
            response.sendRedirect("https://kauth.kakao.com/oauth/logout?client_id=" + KAKAO_CLIENT_ID + "&logout_redirect_uri=" + KAKAO_REDIRECT_URI);
            return ; //redirect가 여러 개 있으면 끊어주는 작업이 필요하기에!
        } else if (provider!=null && provider.startsWith("Naver")) {
            response.sendRedirect("https://nid.naver.com/nidlogin.logout?returl=https://www.naver.com/");
            return;
        } else if (provider!=null && provider.startsWith("Google")) {
            response.sendRedirect("https://accounts.google.com/Logout");
            return;
        }

        response.sendRedirect("/");
    }
}
