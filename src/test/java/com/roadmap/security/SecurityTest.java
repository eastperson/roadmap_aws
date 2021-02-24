package com.roadmap.security;

import com.roadmap.member.WithMember;
import com.roadmap.model.Member;
import com.roadmap.repository.MemberRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Log4j2
@AutoConfigureMockMvc
@Transactional
public class SecurityTest {

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;

//    @AfterEach
//    void afterEach() {
//        memberRepository.deleteAll();
//    }

    @DisplayName("패스워드 인코딩 테스트")
    @Test
    void passwordTest(){

        String password = "1111";

        String encoded = passwordEncoder.encode(password);

        assertFalse(encoded.equals(password));

        assertTrue(passwordEncoder.matches(password,encoded));

        log.info("encoded password : " + encoded);

    }

    @DisplayName("메인 페이지 화면 테스트 - 비회원")
    @Test
    void mainGetTest() throws Exception{
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(unauthenticated());
    }

    @DisplayName("로그인 페이지 화면 테스트 - 비회원")
    @Test
    void loginGetTest() throws Exception{
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(unauthenticated());
    }

    @DisplayName("로그인 페이지 화면 테스트 - 회원")
    @WithMember("epepep")
    @Test
    void loginGetAuthTest() throws Exception{
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(authenticated());
    }

    @DisplayName("권한 페이지 화면 테스트 - 비회원")
    @Test
    void unauthTest() throws Exception{
        mockMvc.perform(get("/check-email-token"))
                .andExpect(unauthenticated());
    }

    @DisplayName("권한 페이지 화면 테스트 - 회원")
    @WithMember("epepep")
    @Test
    void authTest() throws Exception{
        mockMvc.perform(get("/check-email-token"))
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }

    @DisplayName("WithMember 테스트")
    @WithMember("epepep")
    @Test
    void withMember(){
        Member member = memberRepository.findByNickname("epepep");

        assertNotNull(member);
        assertTrue(member.getNickname().equals("epepep"));
        assertTrue(member.getEmail().equals("epepep" + "@email.com"));
        assertTrue(passwordEncoder.matches("123123123",member.getPassword()));

    }

}
