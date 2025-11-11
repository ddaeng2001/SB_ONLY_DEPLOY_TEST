package com.example.demo.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data

public class User {
    @Id //최소한 하나의 PK설정필요!

    private String username;
    private String password;
    private String role;
}
