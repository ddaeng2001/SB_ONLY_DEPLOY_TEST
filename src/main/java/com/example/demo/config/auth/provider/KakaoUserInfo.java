package com.example.demo.config.auth.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoUserInfo implements OAuth2UserInfo {
    //카카오 전용 속성 - getAttributes() 결과값 참조
    private Long id;
    private LocalDateTime connected_at;
    private Map<String,Object> properties;
    private Map<String,Object> kakao_account;


    // 공통화된 데이터를 빼내오기 위함
    @Override
    public String getName() {
        return (String)properties.get("nickname");// properties에서 nickname 반환
    }


    @Override
    public String getEmail() {
        return (String)kakao_account.get("email"); //kakao_account의 email 꺼내기
    }

    @Override
    public String getProvider() {
        return "Kakao";
    }

    @Override
    public String getProviderId() {
        return id!=null ? id.toString() : "0";
    }

    @Override
    public Map<String, Object> getAttributes() {
        return kakao_account;
    }
}
