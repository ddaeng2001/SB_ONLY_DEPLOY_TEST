package com.example.demo.config.auth.jwt;

import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.domain.dtos.UserDto;
import com.example.demo.domain.entity.Signature;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.SignatureRepository;
import com.example.demo.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Component
@Slf4j
public class JWTTokenProvider {

    @Autowired
    private UserRepository userRepository; //access-token과 일치하는 계정이 실제로 존재하는지부터 체크해서 Authentication을 만들어줌
    @Autowired
    private SignatureRepository signatureRepository; //하나의 signature만 저장할 예정인 상황

    //암호화에 사용되는 key가 필요하기에
    private Key key;

    //Tests에서 사용하기 위한 임의
    public Key getKey(){
        return key;
    }

    //기본 함수 생성
    @PostConstruct //생성자가 생성된 이후 동작하는 기본 함수를 지정할 때 사용, 처리해야할 초기값 사용시
    public void init(){
        List<Signature> list = signatureRepository.findAll();
        if(list.isEmpty()){ //list가 텅 비어있다면 == signature 테이블에 서명key가 하나도 없다 == key를 새로 생성해야함
            byte [] keyBytes = KeyGenerator.keyGen();

            //암호문 생성 시 key 이용가능해짐
            this.key = Keys.hmacShaKeyFor(keyBytes);
            //복호화 - 여러가지 알고리즘이 들어간 함수 사용 가능, hmac(해쉬값)Sha(암호화)KeyFor(키생성)

            Signature signature = new Signature(); //db에 넣어주기 위해 signature단위 생성
            signature.setKeyBytes(keyBytes);
            signature.setCreateAt(LocalDate.now());
            signatureRepository.save(signature);
        }else{
            Signature signature = list.get(0);
            //키를 만들어서 전달
            this.key = Keys.hmacShaKeyFor(signature.getKeyBytes()); //DB에 있는 것을 적용시킴

        }

    }



    //함수 3개 생성
                                    //Authentication안에 있는 계정을 가져오도록 할 예
    public TokenInfo generateToken(Authentication authentication){ //GeneratorToken에서 인증 인자인 Authentication을 받도록 해줌

        //계정정보 - 계정명 / auth(role) 꺼내오도록
        String authorities = authentication  .getAuthorities() // Collection<SimpleGrantedAuthority> authorities 반환
                        .stream() //collection이기 때문에 stream 사용 가능
                        .map((role)->{return role.getAuthority();}) //문자열로 반환됨, 각각의 GrantedAuthority("ROLE~")들을 문자열값으로 반환해서 map처리
                        .collect(Collectors.joining(",")); //각가의 role(ROLE_ADMIN ROLE_USER...)를 ','를 기준으로 묶음 ("ROLE_USER, ROLE_ADMIN")

        //  AccessToken 생성
        //  용도 : 서버의 서비스를 이용제한 - 어떤 권한을 가지고있느냐를 기준으로
        long now = (new Date()).getTime();   //현재시간을 기준으로 만료날짜를 설정해야하기 때문에 현재시간
        // 아래 코드 전부 암호문으로 바뀔 예정
        String accessToken = Jwts.builder()
                                .setSubject(authentication.getName()) //Payload(암호화된 암호문)에 들어가는 Token타이틀
                                .setExpiration(new Date(now + (JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME))) //만료날짜(밀리초단위)
                                .signWith(key, SignatureAlgorithm.HS256) //서명값

                                //사용자 정보를 담을 내용들
                                .claim("username",authentication.getName()) //본문 내용
                                .claim("auth",authorities) //본문 내용

                                .compact();

        // RefreshToken 생성
        // AccessToken보다 길게 잡아주는 것이 원칙! - eX> 기간 늘리기 for 갱신!
        // 용도 : AccessToken 만료 시 갱신처리를 하기 위함
        String refreshToken = Jwts.builder()
                .setSubject("Refresh_Token_Title") //Payload(암호화된 암호문)에 들어가는 Token타이틀
                .setExpiration(new Date(now + (JWTProperties.REFRESH_TOKEN_EXPIRATION_TIME))) //만료날짜(밀리초단위)
                .signWith(key, SignatureAlgorithm.HS256) //서명값
                .compact();



        //TokenInfo로 반환
        //Token 생성하는 생성Bean 추가
        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

//    Authentication 생성 작업도 jwtTokenProvider에서 토큰을 받아서 Authentication을 완성시키는 함수 생성
                                                                //token을 claims로 변환시키는 과정에서 예외가 발생 가능
    public Authentication getAuthentication(String accessToken) throws ExpiredJwtException //예외발생 시 실행했던 위치(JWTAuthorizationFilter)에 던지기
                                                                                           //시간 차이에 의한 만료발생(예외발생)처리
    {    //파싱작업
        Claims claims =Jwts.parser().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();


        //<토큰으로부터 값 추출>
        String username = claims.getSubject(); //username(1) - 둘 중 하나 선택하면됨
        username = (String)claims.get("username"); //username(2)
        String auth =(String)claims.get("auth"); //"ROLE_USER,ROLE_ADMIN" - 이를 Authentication에 넣기 위해서는 granted형태로 전달해야함

        //<PrincipalDetails에 맞게 가공>
        //문자열로 되어있는 role들을 authentication으로 바꿔주는 작업
        Collection<GrantedAuthority> authorities = new ArrayList<>(); //security가 Dto에있는 role을 받아서 role_user인지 admin인지
        String roles [] = auth.split(","); //auth = accesstoken에서 받았던 role의미("ROLE_USER,ROLE_ADMIN")
        for(String role : roles){
            authorities.add(new SimpleGrantedAuthority(role)); //,로 나눠진 각각의 role 요소들을 SimpleGranted로 만들어줘야함
                                                                //-> 그래야 authentication에서 role 확인 가능해짐
        //---------------------accesstoken을 server가 받아 계정명과 권한을 빼와서 security에 맞게 가공 ------------------------
        }


        //Authentication에 들어갈 항목
        PrincipalDetails principalDetails = null;
        UserDto dto = null; //로컬 로그인이니까 dto만
        if(userRepository.existsById(username)) { //ID를 통해서 계정의 유무 체크

            //PrincipalDetails 만들 준비
            dto = new UserDto(); //dto 객체 생성
            dto.setUsername(username);
            dto.setRole(auth);
            dto.setPassword(null);

            principalDetails = new PrincipalDetails(dto);
        }
        //계정이 없으면 PrincipalDetails가 null일수도 있기 때문에 계정체크!
        if(principalDetails!=null) {
            //Authentication 생성클래스 == Authentication 직접 생성한 것!
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(principalDetails, null, authorities);
            return authenticationToken;
        }
        return null; //문제가 있다면
    }


    //token 유효한지 확인(검증) - 만료 검증
    public boolean validateToken(String token) throws Exception { //예외발생시 호출위치로 토큰 던지기
        boolean isValid = false;
        try { //JWT token관련 오류 정리 - 오류가 발생하는 경우 : 토큰이 만료 or 유효하지 않은 서명키(서명에 사용했던 키와 토큰에 들어간 키가 다름)

            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            isValid = true; // 파싱이 제대로 돼서 예외가 발생하지 않으면

        } catch(ExpiredJwtException e){//JWT 토큰 만료 Exception
            log.info("[ExpiredJwtException]..." + e.getMessage());
            //예외객체 생성
            throw new ExpiredJwtException(null, null, null); //header, claims, message를 전달하면 이 정보를 취합해서 내용 전달

        }
//        catch(){
//
//        }catch(){
//
//        }

        return isValid;
    }


}
