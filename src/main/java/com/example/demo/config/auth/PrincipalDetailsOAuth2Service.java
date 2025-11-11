package com.example.demo.config.auth;


import com.example.demo.config.auth.provider.GoogleUserInfo;
import com.example.demo.config.auth.provider.KakaoUserInfo;
import com.example.demo.config.auth.provider.NaverUserInfo;
import com.example.demo.config.auth.provider.OAuth2UserInfo;
import com.example.demo.domain.dtos.UserDto;
import com.example.demo.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.example.demo.domain.entity.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class PrincipalDetailsOAuth2Service extends DefaultOAuth2UserService { //OAUTH2 의존도구를 받아 사용할 수 있는 DefaultOAuth2UserServices

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
                        //loadUser를 통해 사용자 인증이 완료되고 OAUTH2서버로부터 전달된 내용은 userRequest로 받음
        System.out.println("");
//        System.out.println("userRequest:" + userRequest);
//
//        //oauth2에 대한 정보
//        System.out.println("userRequest.getClientRegistration() : " + userRequest.getClientRegistration());
//
//        //★★AccessToken 꺼내올 수 있음★★
          //인증 서비스에 대한 접근이 가능해지면서 어떻게 토큰을 저장하느냐에 따라 카카오 메시지 보내기등이 가능해짐
//        System.out.println("userRequest.getAccessToken() : " + userRequest.getAccessToken());

//
//        System.out.println("userRequest.getAdditionalParameters(): "  + userRequest.getAdditionalParameters());
//        System.out.println("userRequest.getAccessToken().getTokenValue() : " + userRequest.getAccessToken().getTokenValue());
//
//        //Token값 확인
//        System.out.println("userRequest.getAccessToken().getTokenType().getValue(): " + userRequest.getAccessToken().getTokenType().getValue());
//
//        //동의항목
//        System.out.println("userRequest.getAccessToken().getScopes() : " + userRequest.getAccessToken().getScopes());
        System.out.println("");

        //반환자료형이 oAuth2User이고 반환방법이 상위 클래스인 loadUser함수를 이용해서
        //결과물을 만들어내고 반환시킴
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("oAuth2User : " + oAuth2User);

        // 카카오 로그인 후 임시 계정을 만들어서 접근함 -> DB에 저장을 해야함 -> Attribute를 꺼내는 이유
        System.out.println("oAuth2User : " + oAuth2User.getAttributes()); //사용자 정보를 담고 있는 Attribute확인
        // 카카오/네이버/구글에 따른 Attribute의 클래스를 만들어서 따로 객체화시키는 작업 -> 정보가 KakaoUserInfo로 받아진 이유!
        System.out.println("Provider name : " + userRequest.getClientRegistration().getClientName());
                            //Provider name을 하는 이유, kakao/google/naver 구분을 위해서

        //어느 인증서버를 쓰고 있는지 인증서버명 가져오기
        String provider = userRequest.getClientRegistration().getClientName();

        //데이터 꺼내오기
        Map<String,Object> attributes = oAuth2User.getAttributes();
        OAuth2UserInfo oAuth2UserInfo = null;

        //username 사용을 위한 전역화
        String username = null;
        if(provider.startsWith("Kakao"))
        {
            //카카오에서 반환하는 속성들(카카오에 맞는 속성 작성)

            Long id = (Long)attributes.get("id");
            LocalDateTime connected_at = OffsetDateTime.parse(attributes.get("connected_at").toString()).toLocalDateTime();
            Map<String,Object> properties = (Map<String,Object>)attributes.get("properties");
            Map<String,Object> kakao_account = (Map<String,Object>)attributes.get("kakao_account");
            System.out.println("id:" + id);
            System.out.println("connected_at:" + connected_at);
            System.out.println("properties:" + properties);
            System.out.println("kakao_account:" + kakao_account);

            //카카오 객체 생성(상위 클래스를 이용한 업캐스팅 방법)
            oAuth2UserInfo = KakaoUserInfo.builder()
                    .id(id)
                    .connected_at(connected_at)
                    .properties(properties)
                    .kakao_account(kakao_account)
                    .build();

            //DB 등록예정 계정명
            username=oAuth2UserInfo.getProvider() +"_"+ oAuth2UserInfo.getProviderId();


        }
        else if(provider.startsWith("Naver")) //네이버가 전달하는 내용에 맞게 NaverUserInfo를 만들어서 객체화 필요!
        {
            Map<String,Object> response = (Map<String,Object>)attributes.get("response");
            System.out.println("response : " + response);

            //네이버 객체 생성
            oAuth2UserInfo = NaverUserInfo.builder()
                    .response(response)
                    .build();

            //DB 등록예정 계정명
            username = oAuth2UserInfo.getEmail();
        }
        else if(provider.startsWith("Google"))
        {
            oAuth2UserInfo = GoogleUserInfo.builder()
                    .attributes(attributes)
                    .build();

            // DB 등록예정 계정명
            username = oAuth2UserInfo.getEmail();
        }
        System.out.println("oAuth2UserInfo: " + oAuth2UserInfo);

        // OAuth2정보 -> 로컬계정생성(계정x : 생성 , 계정o : 불러오기)
        // 로컬계정에 임시 계정을 생성 후 저장한 뒤 접근해야함!
        // 카카오 로그인을 하더라도 로컬 계정에 등록시키는 과정임
        String password= passwordEncoder.encode("1234"); // 비번 암호화시킴

        //DB에 동일 계정 있는지 확인
        // 기존 계 존재 여부에 따라 DB저장
        Optional<User> userOptional = userRepository.findById(username);
        UserDto dto = null;
        if(userOptional.isEmpty()){
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setRole("ROLE_USER");
            userRepository.save(user);

            // 계정이 없다면
            // 엔티티(PrincipalDetails에 들어갈)를 만들어서 저장
            dto = new UserDto(username, password, "ROLE_USER");

        }else{ //계정이 있다면 꺼내와서 DTO에 담아줌
            User user = userOptional.get();
            dto = new UserDto(username,user.getPassword(), user.getRole());

        }
        // PrincipalDetails로 변환해서 반환 for localSecurity에서 사용하기 위해서!
        dto.setProvider(provider); //어떤 OAuth2의 서버를 이용했는지 확인하기 위해
        dto.setProviderId(oAuth2UserInfo.getProviderId());
        return new PrincipalDetails(dto, oAuth2UserInfo.getAttributes()); //dto, attributes 전달
                                    //기존의 dto와 Interface화 시켰던 Attribute를 꺼내서 PrincipalDetails에 전달해서
                                    //local security에 전달함
    }
}
