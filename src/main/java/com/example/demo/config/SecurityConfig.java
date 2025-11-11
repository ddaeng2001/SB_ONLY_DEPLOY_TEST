package com.example.demo.config;

import com.example.demo.config.auth.exceptionHandler.CustomAccessDeniedHandler;
import com.example.demo.config.auth.exceptionHandler.CustomAuthenticationEntryPoint;
import com.example.demo.config.auth.jwt.JWTAuthorizationFilter;
import com.example.demo.config.auth.loginHandler.CustomSuccessHandler;
import com.example.demo.config.auth.loginHandler.CustomFailureHandler;
import com.example.demo.config.auth.logoutHandler.CustomLogoutHandler;
import com.example.demo.config.auth.logoutHandler.CustomLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;


@Configuration
@EnableWebSecurity //기존 Security 설정이 아니라 여기서 직접 Security를 관리하겠다고 명시

public class SecurityConfig {
    //Bean 의존 주입 받기
    @Autowired
    CustomLogoutSuccessHandler customLogoutSuccessHandler;
    @Autowired
    CustomAccessDeniedHandler customAccessDeniedHandler;
    @Autowired
    CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    @Autowired
    CustomFailureHandler customFailureHandler;
    @Autowired
    CustomSuccessHandler customSuccessHandler;
    @Autowired
    CustomLogoutHandler customLogoutHandler;
    @Autowired
    JWTAuthorizationFilter jwtAuthorizationfilter;

    //기본 설정 코드(전반적인 사항들 관리)
    @Bean                                               //Security로부터 외부값 전달, Security option 설정
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {

        //csrf 비활성화(비활성화하지 않으면 logout 요청은 기본적으로 POST방식을 따른다)
        http.csrf((config)->{config.disable();}); //취약해지기때문에 나중에는 POST로 바꿔주기!

        //권한처리
        //페이지 권한처리 - 일단은 다 허용
        http.authorizeHttpRequests((auth)->{
            //패턴 매칭, 기본 페이지와 로그인 페이지는 허용할 수 있도록!
            auth.requestMatchers("/", "/login","/join").permitAll();
            auth.requestMatchers("/").permitAll(); //기본 경로는 permitAll로!

            // 특정 권한이 있어야 들어갈 수 있도록(최소한의 권한은 있어야 함!)
            // 전용페이지에 대한 권한이 있어야 접근이 가능하도록
            auth.requestMatchers("/user").hasAnyRole("USER"); // user전용 페이지
            auth.requestMatchers("/manager").hasAnyRole("MANAGER"); // manager전용 페이지
            auth.requestMatchers("/admin").hasAnyRole("ADMIN"); // admin 전용 페이지


            //그 외 나머지 요청은 인증이 필요함
            auth.anyRequest().authenticated();
        });


        //로그인, 로그아웃을 따로 만들지 않아도 기본적인 폼이 생성되긴 함

        //로그인
        //람다식으로 셋업 필수!
        http.formLogin((login)->{

            //옵션 값 삽입

            //아무나 login페이지에 접근할 수 있도록!
            login.permitAll();

            //endpoint지정
            login.loginPage("/login"); //login페이지를 직접 만들어서 연결해줘야하기 때문에
                                        // Controller도 필요!

            //로그인 성공 시 동작하는 핸들
            login.loginPage("/login");
            login.successHandler(customSuccessHandler);

            //로그인 실패 시 동작하는 핸들러
            login.failureHandler(customFailureHandler);

        });

        //로그아웃(설정 시 POST처리)
        //람다식 셋업 필수
        http.logout((logout)->{
            //로그인 상태에서 모두가 logout에 접근할 수 있도록!
            logout.permitAll();

            //로그아웃 직접 처리하는 핸들러
            logout.addLogoutHandler(customLogoutHandler);

            //로그아웃 성공 핸들러 추가
            logout.logoutSuccessHandler(customLogoutSuccessHandler);

        });

        //예외처리
        http.exceptionHandling((ex)->{

            // 미인증된 상태 + 권한이 필요한 Endpoint 접근 시 예외발생
            ex.authenticationEntryPoint(customAuthenticationEntryPoint); //인증 실패 시

            ex.accessDeniedHandler(customAccessDeniedHandler); //인증 이후 권한이 부족할 때 발생되는 예외
                                                                     // 로그인 한 상태에서 또 다른 or 높은 권한이 필요할 때 발생되는 권한
        });

        //Oauth2-Client 활성화
        http.oauth2Login((oauth2)->{
            oauth2.loginPage("/login");
        });

        //SESSION 비활성화
        http.sessionManagement((sessionConfig)->{
            sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS); //세션 생성정책
                                                                //STATELESS : SESSION을 만들지 x!
        });

        //TokenFilter 추가
        http.addFilterBefore(jwtAuthorizationfilter, LogoutFilter.class); //LogoutFilter 이전에

        return http.build(); //필터 체인이 return됨
    }


    //임시계정생성 - DB없이 권한 확인을 위한 작업이므로 추후에 삭제 예정
//    @Bean
//    UserDetailsService users() {
//        UserDetails user = User.withUsername("user")
//                .password("{noop}1234")   // 비밀번호 인코딩 없음 (실습용)
//                .roles("USER")            // ROLE_USER
//                .build();
//
//        UserDetails manager = User.withUsername("manager")
//                .password("{noop}1234")
//                .roles("MANAGER")         // ROLE_MANAGER
//                .build();
//
//        UserDetails admin = User.withUsername("admin")
//                .password("{noop}1234")
//                .roles("ADMIN")           // ROLE_ADMIN
//                .build();
//
//        return new InMemoryUserDetailsManager(user, manager, admin);
//    }
    // 패스워드 암호화작업(해시값생성)에 사용되는 Bean
    @Bean
    public PasswordEncoder passwordEncoder(){ //계정 생성 시, 이곳에서 pw를 암호화시킴
        // 사용할 때는 @Autowired해서 연결시키면 됨
        return new BCryptPasswordEncoder();
    }

}
