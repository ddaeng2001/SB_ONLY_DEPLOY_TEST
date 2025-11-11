package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class JwtToken {

    //자동증가 pk열
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT", nullable = false) //columnDefinition = "TEXT" - 이 필드를 DB에서 TEXT타입으로 만듦으로써 VARCHAR(255) 길이제한에서 탈피가능해짐
    private String accessToken;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String refreshToken;
    @Column
    private String username;
    @Column
    private String auth; //"ROLE_USER, ROLE_ADMIN"이 ACCESSTOKEN으로 들어가야 권한 뺴내는 작업이 가능해짐
                         //추후에 없애고 REFRESH에 넣어도 됨!
    @Column(name="createdAt", columnDefinition = "DATETIME", nullable=false)
    private LocalDateTime createAt;

}
