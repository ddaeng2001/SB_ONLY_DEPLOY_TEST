package com.example.demo.config.auth.jwt;


//인증 이후의 권한 부여에 사용되는 필터로써 토큰 전달 예정

import com.example.demo.domain.entity.JwtToken;
import com.example.demo.domain.repository.JwtTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {//security용 filter 추가

    @Autowired
    JWTTokenProvider jwtTokenProvider; //authentication만드는 함수 포함되어있음
    @Autowired
    JwtTokenRepository jwtTokenRepository; //현재 RefreshToken은 DB에 있는 상태이기에 RefreshToken을 포함한 엔티티 꺼내오기

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //전
        //access-token 쿠키 받아 Authentication 생성 이후 SecurityContextHolder에 저장
        System.out.println("[JWTAuthorizationFilter] doFilterInternal...");

        //access-token 쿠키 받기
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

        System.out.println("TOKEN: " + token); //token값 확인
        if(token!=null){ //access-token이 있다면

            //access-token 쿠키 받아 Authentication 생성 이후 SecurityContextHolder에 저장
            //1)access-token 만료되었는지 확인
            //Provider에서 예외를 던져줬으니 여기서 예외처리를 해줌
            try {
                if (jwtTokenProvider.validateToken(token)) { //위에서 전달받은 token값을 넣었을때 True(만료X 유지가능)/False값이 나올 예정
                    //1-1) access-token ==만료 x? authentication 생성 작업(Security에 문제가 없게끔!)
                    //Authentication 생성 작업도 jwtTokenProvider에서 토큰을 받아서 Authentication을 완성시키는 함수 생성
                    Authentication authentication = jwtTokenProvider.getAuthentication(token); //Authentication 생성 완
                    if(authentication != null)
                    {
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }

                }
            }catch(ExpiredJwtException e){
                //1-2) access-token ==만료 o? refresh-token 만료여부 확인(추후에 작업) - return False;
                System.out.println("ExpiredJwtException...AccessToken Expired" + e.getMessage()); //엑세스 토큰이 만료된 이후에 들어옴

                //2) RefreshToken의 만료유무
                //AccessToken은 만료되었기 때문에 들어온 상태
                JwtToken entity = jwtTokenRepository.findByAccessToken(token); //db로부터 access-token 전달
                if(entity!=null){ //AccessToken이 null이 아니라면


                    //예외던지기
                     try {                  //유효성 Token에(entity에서 검색했던 RefreshToken 전달)
                         if (jwtTokenProvider.validateToken(entity.getRefreshToken())) {
                             //2-1) RefreshToken!=만료 ? -> AccessToken 재발급 -> 쿠키로 전달 + DB Token Info 갱신
                             //AccessToken만 만료된 상황!
                             //->AccessToken 재발급
                             long now = (new Date()).getTime();   //현재시간을 기준으로 만료날짜를 설정해야하기 때문에 현재시간
                             // 아래 코드 전부 암호문으로 바뀔 예정
                             String accessToken = Jwts.builder()
                                     .setSubject(entity.getUsername()) //Payload(암호화된 암호문)에 들어가는 Token타이틀
                                     .setExpiration(new Date(now + (JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME))) //만료날짜(밀리초단위)
                                     .signWith(jwtTokenProvider.getKey(), SignatureAlgorithm.HS256) //서명값

                                     //사용자 정보를 담을 내용들
                                     .claim("username",entity.getUsername()) //본문 내용
                                     .claim("auth",entity.getAuth()) //본문 내용 "ROLE_UESR, ROLE_ADMIN"
                                     .compact();

                             //쿠키로 전달
                             Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME, accessToken);
                             cookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME);    //accesstoken 유지시간
                             cookie.setPath("/");    //쿠키 적용경로(/:모든 경로)
                             response.addCookie(cookie); //응답정보에 쿠키 포함
                             //DB's AccessToken 값 갱신 - refreshtoken값은 그대로 두고!
                             entity.setAccessToken(accessToken); //받아온 entity에 AccessToken만 바꿔주면 됨
                             jwtTokenRepository.save(entity); //UPDATE됨
                         }
                     }catch(ExpiredJwtException e2) {
                         //2-2) RefreshToken==만료 ? -> DB's Token Info 삭제
                         System.out.println("ExpiredJwtException ...RefreshToken Expired..." + e2.getMessage());
                         //access-token 제거(자동제거는 됨)
                         Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME, null);
                         cookie.setMaxAge(0);
                         response.addCookie(cookie);
                         //결과 : 로그인 후 30초 뒤 accesstoken이 만료되면서 사라지고 /user로 들어가면 다시 login을 해야함
                         //;
                         //DB로 가서 삭제
                         jwtTokenRepository.deleteById(entity.getId());
                     }catch(Exception e2){

                     }
                }



            }catch (Exception e){ //그 외 나머지

            }

            //2)
        }else{
            //access-token == null
            //1) 최초로그인(DB에도 없고, 최초발급)
            //2) access-token을 발급o - DB에는 있지만 쿠키만료(==token만료)시간에 의해서 제거된 상태

        }

        filterChain.doFilter(request,response); //다음 위치로 이동

        //후

    }

}
