package com.example.demo.config.auth.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NaverUserInfo implements OAuth2UserInfo{ //이 인터페이스를 통해 어떤 데이터가
                                                      //빠져나가게 할 건지 설정
    //속성인 message나 result 제외! 굳이 필요 없을 듯?
    //response에 있는 속성만 담을 작업 시작>>
//    private String id;
//    private String profile;
//    private String email;
//    private String name;
    private Map<String, Object> response; //attribute가 있어서 삽입

    @Override
    public String getName() {
        return (String)response.get("name"); //찾는 작업
    }

    @Override
    public String getEmail() {
        return (String)response.get("email");
    }

    @Override
    public String getProvider() {
        return "Naver";
    }

    @Override
    public String getProviderId() {
        return (String)response.get("id");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return response;
    }
}
