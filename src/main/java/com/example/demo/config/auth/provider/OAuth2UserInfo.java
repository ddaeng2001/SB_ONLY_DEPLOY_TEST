package com.example.demo.config.auth.provider;

import java.util.Map;
//서버마다 다른 속성들을 지니기에 공통요소를 추출해서 인터페이스를 통해 kakao/google...등 상관없이 원하는 함수를 추출하기 위한
//인터페이스 필요함

public interface OAuth2UserInfo {

    String getName();       //이름 반환
    String getEmail();      //접속 이메일계정 반환
    String getProvider();   //PROVIDER 이름 반환
    String getProviderId(); //
    Map<String,Object> getAttributes(); //!!!계정정보 반환!!!

}
