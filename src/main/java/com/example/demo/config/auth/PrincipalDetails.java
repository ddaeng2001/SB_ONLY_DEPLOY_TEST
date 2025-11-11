package com.example.demo.config.auth;

import com.example.demo.domain.dtos.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


// UserDetails에 맞게 저장을 해줘야 Authentication에 저장이 되기에 만든 파일
// UserDetails와 상속관계를 둬서 DB에 있는 내용을 꺼내올 예정
// 데이터를 꺼내와서 Manager로 전달되면 Manager가 인증작업을 할꺼고 이게 끝나면 Authentication에 저장됨!


@Data
@NoArgsConstructor
@AllArgsConstructor
                                                    //PrincipalDetailsOAuth2Service의 loadUser의 반환형
public class PrincipalDetails implements UserDetails, OAuth2User {
    private UserDto dto; //UserDto 저장 //Override를 제외한 UserDto를 받는 생성자 만들어짐!

    //Security에서 다 꺼내 쓰기 때문에 아래처럼 오버라이딩해서 만들어줘야하는 형태

    //OAUTH2 속성
    Map<String, Object> attributes;

    public PrincipalDetails(UserDto dto){
        this.dto = dto;
    }


    //Dto로부터 Role(권한)꺼낼 때 사용함 !★★★중요★★★!
    @Override
                    //GrantedAuthority 클래서와 ?(:상속관계에 있는 하위클래스형)으로 return 해줘야함
    public Collection<? extends GrantedAuthority> getAuthorities() {

        //문자열로 되어있는 role들을 authentication으로 바꿔주는 작업
        //반환자료형
        Collection<GrantedAuthority> authorities = new ArrayList<>(); //security가 Dto에있는 role을 받아서 role_user인지 admin인지
                                                                      //권한 처리를 해주기 위해 security가 쓰는 함수
                                                                        //dto에 있는 role을 자료형에 맞게 전달해주는 작업
        // 계정이 단일 ROLE을 가질 때 ("ROLE_USER")
//        authorities.add(new SimpleGrantedAuthority(dto.getRole()));
                        //GrantedAuthority의 하위클래스인 SimpleGrantedAuthority를 구현체로 둠
                                                    //dto에 있는 Role을 전달


        // 계정이 여러 ROLE을 가질 때 ("ROLE_ADMIN, ROLE_USER")
        // 인가 처리(페이지 권한처리)할 때 문제발생예방을 위해
        String roles [] = dto.getRole().split(",");
        for(String role : roles){
            authorities.add(new SimpleGrantedAuthority(role));
        }


        return authorities;

        //--------------security가 role을 꺼내기 위해 사용되는 함수임!!----------
        //------- ★★이부분 설정을 안하면 로그인은 되지만 페이지 접근에 대한 권한이 없어서 페이지에 접근 불가능★★ -------
    }
    //--------------------------------------------------------
    // OAUTH2에 사용되는 메서드
    //--------------------------------------------------------

    @Override
    public Map<String, Object> getAttributes() {

        return attributes; //OAuth2에서 attribute를 사용하기 때문에 생성해야함
    }

    //--------------------------------------------------------
    //로컬인증에 사용되는 메서드
    //--------------------------------------------------------


    @Override
    public String getPassword() {
        return dto.getPassword();//pw는 security에서 꺼내올 예정이기에 여기에 맞게 해줘야함
                                 //인증을 할 수 있도록 pw를 꺼내오는 작업
    }

    @Override
    public String getUsername() {
        return dto.getUsername(); //security가 user명을 사용할 수 있도록 전달
    }

    @Override
    public boolean isAccountNonExpired() { //만료가 되지 않았나요?
        return true;
    }

    @Override
    public boolean isAccountNonLocked() { //계정이 잠겨있지 않죠?
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() { //pw가 만료되지 않았지?
        return true;
    }

    @Override
    public boolean isEnabled() { //사용가능하지?
        return true;
    }

    @Override
    public String getName() {
        return "";
    }
}
