package com.example.demo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//설정Bean 생성
@Configuration
public class DataSourceConfig {

    //HikariDataSource 기반
    @Bean
    public HikariDataSource dataSource(){
        //DataSource 객체 생성
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver"); //드라이버 적재를 위한 작업
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/testdb");
        dataSource.setUsername("root"); //DB 접속 계정
        dataSource.setPassword("1234"); //DB 접속 시 사용할 패스워드

        return dataSource;
    }


}
