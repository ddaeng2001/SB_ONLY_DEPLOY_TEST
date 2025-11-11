package com.example.demo.config.auth.jwt;

public class JWTProperties {

    //외부에서 사용할 기준 상수값 생성 == static final 사용하는 이유
    //만료시간                                             //30초
    public static final int ACCESS_TOKEN_EXPIRATION_TIME=1000*20; //millisecond - 1000(기본)
    public static final int REFRESH_TOKEN_EXPIRATION_TIME=1000*60*10; //10분

    //쿠키명
    public static final String ACCESS_TOKEN_COOKIE_NAME="access-token";
    public static final String REFRESH_TOKEN_COOKIE_NAME="refresh-token";

    //AccessToken 만료시간 != AccessToken Cookie 만료시간
    //-> RefreshToken을 따로 관리하는 경우
    // 그래야 작업이 가능해짐
    // 만약에 accesstoken도 만료가 되고 accesstoken의 쿠키도 만료가 된다면 만료되는 시점에 accesstoken이 사라진다면
    //db에서 해당 사용자에 대한 정보를 추출해내야하는데 누군지 식별할 수 없게 됨
    // accesstoken을 만료시키되 cookie를 조금 더 길게 잡아둬야 refreshtoken 확인가능

    //if refreshtoken을 우리가 아닌 쿠키로 던져준다면 accesstoken과 accesstoken cookie 만료시간을 동일해도 상관없을 듯
    // 어차피 refreshtoken이 쿠키를 가지고 있기 때문에 이걸로 식별하면 됨!
    public static final int ACCESS_TOKEN_COOKIE_EXPIRATION_TIME=ACCESS_TOKEN_EXPIRATION_TIME;
    //토큰 만료는 됐지만 쿠키는 유지될 수있도록 잠깐만 설정해놓기


}
