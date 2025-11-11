//package com.example.demo.config.auth.jwt;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwtParser;
//import io.jsonwebtoken.Jwts;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.security.Key;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class JWTTokenProviderTest {
//
//    @Autowired
//    private JWTTokenProvider tokenProvider;
//
//    @Test
//    public void t1() throws Exception
//    {
//        //토큰 생성해서 확인해보기
//        TokenInfo tokenInfo = tokenProvider.generateToken();
//        System.out.println(tokenInfo);
//
//        //AccessToken key를 꺼내는 작업
//        //암호화 == 복호화 키 : 대칭키 암호화 방식
//        Key key = tokenProvider.getKey(); //키 꺼내오기
//
//        //내용 꺼내기
//        //입력했던 토큰의 내용 정보들을 확인하기 위한 클래스
//        JwtParser parser= Jwts.parser()
//                                .setSigningKey(key)
//                                        //Provider에 만들어놓은 key를 전달해서 복호화에 사용 예정
//                                .build();
//
//        //어떤 내용을 파싱한건지 넣어주기
//        String accessToken = tokenInfo.getAccessToken();//클레임 정보를 꺼내기 위해서는 Token이 필요하기에
//        Claims claims = parser.parseClaimsJws(accessToken).getBody();
//
//        //accessToken을 복호화 후 Payload부분(Claims)를 꺼내오기
//        String username = claims.get("username").toString(); //object형이기에 String으로
//        String role = claims.get("role").toString();
//        System.out.println("username:" + username);
//        System.out.println("role:" + role);
//    }
//    @Test
//    public void t2() throws Exception
//    {
//
//    }
//}