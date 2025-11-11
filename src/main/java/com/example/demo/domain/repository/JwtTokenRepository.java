package com.example.demo.domain.repository;

import com.example.demo.domain.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken,Long> {//<엔티티 클래스이름, PK의 자료형>

    //REPOSITORY로 ACCESSTOKEN을 전달받았을때 부합하는 TOKEN의 전체 정보를 삭제하도록
    //accesstoken -> token 정보 삭제 - refreshtoken까지 만료되어서 더 이상 사용을 못할 때 전체 제거
    void deleteByAccessToken(String accessToken);

    //accesstoken -> token 정보 가져오기
    //ACCESSTOKEN이 만료가 되어있지 않았을 떄
    JwtToken findByAccessToken(String accessToken);
            //TOKEN반환

    //username -> token 정보가져오기
    //사용자 정보를 사용해서 전체 정보 가져오기
    JwtToken findByUsername(String username);
}
