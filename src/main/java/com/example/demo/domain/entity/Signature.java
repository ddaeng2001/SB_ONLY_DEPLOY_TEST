package com.example.demo.domain.entity;

//서명값이 프로그램을 재부팅할 때마다 계속 바뀌기 때문에 껐다키게 되면 그전에 저장했던 토큰의 값을 확인할 수 없기 때문에
//서명값을 일정기간마다 유지시키는 작업
// -> 서버를 껐다 키더라도 로그인 상태가 그대로 유지되도록

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Signature {
    @Id
    @Column(name="signKey")
    private byte[] keyBytes;
    @Column(name="createAt")
    private LocalDate createAt;
}
