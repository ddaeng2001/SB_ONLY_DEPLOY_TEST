package com.example.demo.config.auth.jwt;

//token이 생성되면 이 토큰을 담기위한 Dto 역할을 하는 파일 생성

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class TokenInfo {
    private String grantType;
    private String accessToken;
    private String refreshToken;
}
