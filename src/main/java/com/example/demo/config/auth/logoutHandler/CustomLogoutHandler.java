package com.example.demo.config.auth.logoutHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomLogoutHandler implements LogoutHandler {

    //로그아웃 직접 처리하는 Handler
    //자체 로그아웃 처리
    //해당 서버의 로그아웃
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info("CustomLogoutHandler's logout invoke...!");

//        //세션기반(기본값) - 세션 제거하는 작업
//        HttpSession session = request.getSession(false);
//        if(session!=null)
//            session.invalidate();

        //LogoutHandler가 끝나면 SuccessHandler로 넘어감
        //Token 기반으로 방식이 바뀌면 이 기반으로 다시 처리해줘야함
    }
}
