package com.roadmap.roadmap;

import com.roadmap.config.AppProperties;
import com.roadmap.dto.roadmap.form.RoadmapForm;
import com.roadmap.member.WithMember;
import com.roadmap.model.Member;
import com.roadmap.model.Roadmap;
import com.roadmap.repository.MemberRepository;
import com.roadmap.repository.RoadmapRepository;
import com.roadmap.service.MemberService;
import com.roadmap.service.RoadmapService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class RoadmapControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberService memberService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoadmapRepository roadmapRepository;
    @Autowired private RoadmapService roadmapService;
    @Autowired private AppProperties appProperties;

    private final static String NICKNAME = "epepep";

    @Test
    @DisplayName("로드맵 생성 화면 테스트")
    @WithMember(NICKNAME)
    void newRoadmapView() throws Exception {
        mockMvc.perform(get("/new-roadmap"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("roadmapForm"))
                .andExpect(view().name("roadmap/form"))
                .andExpect(authenticated());
    }

    @Test
    @DisplayName("로드맵 생성 화면 - 입력값 정상")
    @WithMember(NICKNAME)
    void newRoadmapView_submit_correct() throws Exception {

        Member member = memberRepository.findByNickname(NICKNAME);

        mockMvc.perform(post("/new-roadmap")
                .param("path","testpath")
                .param("title","제목 테스트")
                .param("shortDescription","짧은 소개 테스트")
                .param("fullDescription", "긴 소개 테스트")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/testpath"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated());

        Roadmap roadmap = roadmapRepository.findByPath("testpath");
        assertNotNull(roadmap);
        assertTrue(roadmap.getOwner().equals(member));
        assertTrue(roadmap.getFullDescription().equals("긴 소개 테스트"));
        assertTrue(roadmap.getShortDescription().equals("짧은 소개 테스트"));
        assertTrue(roadmap.getPath().equals("testpath"));
        assertTrue(roadmap.getTitle().equals("제목 테스트"));

    }

    @Test
    @DisplayName("로드맵 생성 화면 - 입력값 정상 한글주소")
    @WithMember(NICKNAME)
    void newRoadmapView_submit_korean() throws Exception {

        Member member = memberRepository.findByNickname(NICKNAME);

        mockMvc.perform(post("/new-roadmap")
                .param("path","한글주소")
                .param("title","제목 테스트")
                .param("shortDescription","짧은 소개 테스트")
                .param("fullDescription", "긴 소개 테스트")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + URLEncoder.encode("한글주소", StandardCharsets.UTF_8)))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated());

        Roadmap roadmap = roadmapRepository.findByPath("한글주소");
        assertNotNull(roadmap);
        assertTrue(roadmap.getOwner().equals(member));
        assertTrue(roadmap.getFullDescription().equals("긴 소개 테스트"));
        assertTrue(roadmap.getShortDescription().equals("짧은 소개 테스트"));
        assertTrue(roadmap.getPath().equals("한글주소"));
        assertTrue(roadmap.getTitle().equals("제목 테스트"));

    }

    @Test
    @DisplayName("로드맵 생성 화면 - 입력값 오류")
    @WithMember(NICKNAME)
    void newRoadmapView_submit_error() throws Exception {

        mockMvc.perform(post("/new-roadmap")
                .param("path","testpath#@")
                .param("title","제목 테스트")
                .param("shortDescription","짧은 소개 테스트")
                .param("fullDescription", "긴 소개 테스트")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("member"))
                .andExpect(model().hasErrors())
                .andExpect(view().name("roadmap/form"))
                .andExpect(authenticated());

        Roadmap roadmap = roadmapRepository.findByPath("testpath#@");
        assertNull(roadmap);

    }

    @Test
    @DisplayName("로드맵 화면 테스트")
    @WithMember(NICKNAME)
    void roadmapView() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);

        Roadmap roadmap = roadmapRepository.findByPath("testpath");

        mockMvc.perform(get("/roadmap/" + roadmap.getEncodedPath()))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("roadmap"))
                .andExpect(status().isOk())
                .andExpect(view().name("roadmap/view"))
                .andExpect(authenticated());
    }

    @Test
    @DisplayName("로드맵 멤버 화면 테스트")
    @WithMember(NICKNAME)
    void roadmapMembersView() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);

        Roadmap roadmap = roadmapRepository.findByPath("testpath");

        mockMvc.perform(get("/roadmap/" + roadmap.getEncodedPath() + "/members"))
                .andExpect(model().attributeExists("roadmap"))
                .andExpect(status().isOk())
                .andExpect(view().name("roadmap/member"))
                .andExpect(authenticated());
    }

    @Test
    @DisplayName("로드맵 맵 화면 테스트")
    @WithMember(NICKNAME)
    void roadmapMapView() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);

        Roadmap roadmap = roadmapRepository.findByPath("testpath");

        mockMvc.perform(get("/roadmap/" + roadmap.getEncodedPath() + "/map"))
                .andExpect(model().attributeExists("roadmap"))
                .andExpect(model().attributeExists("host"))
                .andExpect(model().attributeExists("roadmapAppKey"))
                .andExpect(status().isOk())
                .andExpect(view().name("roadmap/map"))
                .andExpect(authenticated());
    }

    @Test
    @DisplayName("로드맵 멤버 가입")
    @WithMember(NICKNAME)
    void roadmapJoin() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);

        Roadmap roadmap = roadmapRepository.findByPath("testpath");


        roadmapService.removeMember(roadmap,member);
        assertFalse(roadmap.getMembers().contains(member));

        mockMvc.perform(get("/roadmap/" + roadmap.getEncodedPath() + "/join")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + roadmap.getEncodedPath() + "/members"))
                .andExpect(authenticated());

        Roadmap updateRoadmap = roadmapRepository.findByPath("testpath");
        assertTrue(updateRoadmap.getMembers().contains(member));

    }

    @Test
    @DisplayName("로드맵 멤버 탈퇴")
    @WithMember(NICKNAME)
    void roadmapLeave() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);

        Roadmap roadmap = roadmapRepository.findByPath("testpath");

        assertTrue(roadmap.getMembers().contains(member));

        mockMvc.perform(get("/roadmap/" + roadmap.getEncodedPath() + "/leave")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + roadmap.getEncodedPath() + "/members"))
                .andExpect(authenticated());

        Roadmap updateRoadmap = roadmapRepository.findByPath("testpath");
        assertFalse(updateRoadmap.getMembers().contains(member));

    }

    @DisplayName("appProperties key 확인")
    @Test
    void appProperties() throws Exception {
        System.out.println("host : "+appProperties.getHost());
        System.out.println("juso confirm key : "+appProperties.getJusoConfirmKey());
        System.out.println("kakao rest key : "+appProperties.getKakaoRestKey());
        System.out.println("kakao js key : "+appProperties.getKakaoJsKey());
        System.out.println("roadmap api key : "+appProperties.getRoadmapApiKey());

    }

}
