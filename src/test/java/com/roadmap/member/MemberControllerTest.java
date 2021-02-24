package com.roadmap.member;

import com.roadmap.dto.member.form.SignUpForm;
import com.roadmap.model.Member;
import com.roadmap.repository.MemberRepository;
import com.roadmap.service.EmailService;
import com.roadmap.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class MemberControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private EmailService emailService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberService memberService;

//    @AfterEach
//    void afterEach() {
//        memberRepository.deleteAll();
//    }

    @DisplayName("회원 가입 폼 화면")
    @Test
    void SignUpFormView () throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(unauthenticated())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(view().name("member/sign-up"));

    }

    @DisplayName("회원 가입 폼 입력 - 정상")
    @Test
    void SignUpFormSubmit_correct () throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname","epepep")
                .param("password","123123123")
                .param("email","epepep@email.com")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Member member = memberRepository.findByNickname("epepep");
        assertNotNull(member);

    }

    @DisplayName("회원 가입 폼 입력 - 데이터 입력 에러")
    @Test
    void SignUpFormSubmit_input_error () throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname","e")
                .param("password","123123123")
                .param("email","epepep@email.com")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(view().name("member/sign-up"));
        Member member = memberRepository.findByNickname("epepep");
        assertNull(member);
    }

    @DisplayName("회원 가입 폼 입력 - 데이터 이미 존재시")
    @Test
    void SignUpFormSubmit_exist_error () throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setPassword("123123123");
        signUpForm.setEmail("epepep@email.com");
        signUpForm.setNickname("epepep");
        memberService.saveNewMember(signUpForm);

        mockMvc.perform(post("/sign-up")
                .param("nickname","epepep")
                .param("password","123123123")
                .param("email","epepep@email.com")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(view().name("member/sign-up"));
    }

    @DisplayName("이메일 토큰 확인 화면 - 토큰이 없을 때")
    @WithMember("epepep")
    @Test
    void emailCheckView_token_error() throws Exception {
        Member member = memberRepository.findByNickname("epepep");
        mockMvc.perform(get("/check-email-token")
                .param("email",member.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("member/check-email-token"));
    }

    @DisplayName("이메일 토큰 확인 화면 - 이메일이 다를 때")
    @WithMember("epepep")
    @Test
    void emailCheckView_email_error() throws Exception {
        Member member = memberRepository.findByNickname("epepep");
        mockMvc.perform(get("/check-email-token")
                .param("email",member.getEmail() + "error"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("member/check-email-token"));
    }

    @DisplayName("이메일 토큰 확인 화면 - 정상")
    @WithMember("epepep")
    @Test
    void emailCheckView_correct() throws Exception {
        Member member = memberRepository.findByNickname("epepep");
        mockMvc.perform(get("/check-email-token")
                .param("email",member.getEmail())
                .param("toekn",member.getEmailCheckToken()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(view().name("member/check-email-token"));
    }

    @DisplayName("이메일 보내기 화면")
    @WithMember("epepep")
    @Test
    void emailSendView() throws Exception {
        Member member = memberRepository.findByNickname("epepep");
        mockMvc.perform(get("/check-email-send"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("email"));
    }

    @DisplayName("이메일 보내기 화면 - 전송 실패")
    @WithMember("epepep")
    @Test
    void emailResend_error() throws Exception {
        Member member = memberRepository.findByNickname("epepep");
        member.setEmailCheckTokenGeneratedAt(LocalDateTime.now());
        mockMvc.perform(get("/resend-confirm-email"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("email"))
                .andExpect(view().name("member/check-email-send"));
    }

    @DisplayName("이메일 보내기 화면 - 전송 성공")
    @WithMember("epepep")
    @Test
    void emailResend_correct() throws Exception {
        Member member = memberRepository.findByNickname("epepep");
        member.setEmailCheckTokenGeneratedAt(LocalDateTime.now().minusHours(2));
        mockMvc.perform(get("/resend-confirm-email"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertTrue(member.getEmailCheckTokenGeneratedAt().isAfter(LocalDateTime.now().minusMinutes(1)));
    }

}
