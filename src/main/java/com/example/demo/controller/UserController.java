package com.example.demo.controller;

import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.domain.dtos.UserDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.security.Principal;

//login페이지 연결을 위한 Controller
@Controller
@Slf4j
public class UserController {

    @Autowired
    private HttpServletResponse response;
    @Autowired
    private HttpServletRequest request;

    //Authentication이 존재하면 다시 /user로 리다이렉트 하도록 설정
    @GetMapping("/login") //templates내에 login.html 생성
    public void login(@AuthenticationPrincipal PrincipalDetails principalDetails) throws IOException {
        log.info("GET /login..." + principalDetails);
        if(principalDetails!=null)
            response.sendRedirect("/user");
    }

    //사용자 정보 확인방법 -1
    //user 전용페이지 연결을 위함
//    @GetMapping("/user")
//    public void user(Authentication authentication, Model model){ //SB 단독 시, Model 필요!
//
//        log.info("GET /user..." + authentication);
//        log.info("name..." + authentication.getName());
//        log.info("principal..." + authentication.getPrincipal());
//        log.info("authorities..." + authentication.getAuthorities());
//        log.info("details..." + authentication.getDetails());
//        log.info("credential..." + authentication.getCredentials()); //pw - Authentication수준의 pw는 null로 가려짐
//
//        model.addAttribute("auth_1",authentication);
//    }

    //사용자 정보 확인방법 -2
    //ContextHolder에서 Authentication 직접 꺼내오기
    @GetMapping("/user")
    public void user(Model model){ //SB 단독 시, Model 필요!

        Authentication authentication = 
        SecurityContextHolder.getContext().getAuthentication(); //MVC 패턴을 사용하고 있는 곳에서 꺼내올 수 있음(활용도 높음)
                
        log.info("GET /user..." + authentication);
        log.info("name..." + authentication.getName());
        log.info("principal..." + authentication.getPrincipal());
        log.info("authorities..." + authentication.getAuthorities());
        log.info("details..." + authentication.getDetails());
        log.info("credential..." + authentication.getCredentials()); //pw - Authentication수준의 pw는 null로 가려짐

        model.addAttribute("auth_1",authentication);
    }


    //확인방법 - 3 Authentication's Principal만 꺼내와 연결
    //manager 전용페이지 연결을 위함
    @GetMapping("/manager")
    public void manager(@AuthenticationPrincipal PrincipalDetails principalDetails){
        log.info("GET /manager..." + principalDetails);
    }

    //admin 전용페이지 연결을 위함
    @GetMapping("/admin")
    public void admin(){
        log.info("GET /admin...");
    }

    //join 페이지 연결
    @GetMapping("/join")
    public void join(){
        log.info("GET /join...");
    }

    //pw 암호화 연결
    @Autowired
    private PasswordEncoder passwordEncoder;

    //repository 연결
    @Autowired
    private UserRepository userRepository;


    @PostMapping("/join")
    public String join_post(UserDto dto){
        log.info("POST /join...");
        String pwd = passwordEncoder.encode(dto.getPassword()); //암호화(SecurityConfig 내 passwordEncoder사용)

        //dto -> entity
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(pwd); //암호화시킨 pw
        user.setRole("ROLE_USER"); //security에서는 권한체크시 ROLE_가 없으나 DB에 넣을때는 ROLE_가 들어가야 ROLE 판단가능
        userRepository.save(user);
        boolean isJoin = true;
        if(isJoin){
            return "redirect:/login";
        }
        return "join"; //비정상적이라면 join으로 다시 가도록 return!
    }
}
