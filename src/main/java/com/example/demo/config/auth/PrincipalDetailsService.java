package com.example.demo.config.auth;

import com.example.demo.domain.dtos.UserDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;


//DB의 내용을 꺼내오는 파일! - for 계정 정보 확인
//인증작업이 이루어지는 곳 XX
@Service
@Slf4j
            //인증작업은 일치하는지 하지 않는지
public class PrincipalDetailsService implements UserDetailsService {//DB내용 가져올 때 사용

    //UserDetails 단위로 내용을 담아서 만들어줘야하는 상황
    //User 정보를 받아오면 UserDetails 형태에 맞게 만들어서 받아올 예정
    //계정 정보 찾을 준비 ㄱㄱ>>
    @Autowired
    private UserRepository userRepository; //어떤 DB연결방식을 쓰느냐에 따라 달라짐(ex>MyBatis, DataSource, JPA,,,)

    @Override                           //로그인 폼으로분터 받아지는 username
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            //UserDetails 형태에 맞춰서 user정보를 던져줘야함 - 그래야 Authentication으로 저장이 됨
            //Authentication Manager에게 DB에 있는 내용 던죠줌

        System.out.println("PrincipalDetailService's loadUserByUsername: " + username);

        //동일 계정이 있는지 여부를 UserDetails폼으로 Authentication(인증하는 위치)로 서빙해줌
        //Repository를 통해 User정보를 꺼내와서 유저 정보가 있는지 없는지 확인
        Optional<User> userOptional= //User = demo.entity
            userRepository.findById(username);
        //계정 정보가 없다면
        if(userOptional.isEmpty())
            throw new UsernameNotFoundException(username+ " 계정이 존재하지 않습니다.");

        //ENTITY -> DTO(계정이 있다면)
        //PrincipalDetails가 dto로 속성을 저장할 수 있게 만들어놓았기에 거기에 정보를 넣기 위해 Entity에서 Dto로 변경작업
        User user = userOptional.get();
        UserDto dto = new UserDto();
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setRole(user.getRole());

        return new PrincipalDetails(dto); //만들었던 dto 인자로 반환하면 Authentication Manager가 받아서 인증작업 실행




    }
    //LOGIN FORM    -> SECURITY POST /login    PrincipalDetailsService
                    //->user1/1234              DB로부터 user1/암호화된 pwd를 SECURITY로 전달해줌

    //lOGIN POST를 만드는 것이 아닌 입력 계정에 해당되는 사용자 정보를 전달하는 작업

}
