package com.roadmap.roadmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadmap.config.AppProperties;
import com.roadmap.dto.member.form.TagForm;
import com.roadmap.dto.roadmap.form.RoadmapForm;
import com.roadmap.member.WithMember;
import com.roadmap.model.Member;
import com.roadmap.model.Roadmap;
import com.roadmap.model.Tag;
import com.roadmap.repository.MemberRepository;
import com.roadmap.repository.RoadmapRepository;
import com.roadmap.repository.TagRepository;
import com.roadmap.service.MemberService;
import com.roadmap.service.RoadmapService;
import com.roadmap.service.TagService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Log4j2
public class RoadmapSettingsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberService memberService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoadmapRepository roadmapRepository;
    @Autowired private RoadmapService roadmapService;
    @Autowired private AppProperties appProperties;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TagRepository tagRepository;
    @Autowired private TagService tagService;

    private final static String NICKNAME = "epepep";
    private Roadmap roadmap;

    @BeforeEach
    void createRoadmap(){
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);

        this.roadmap = roadmapRepository.findByPath("testpath");

        assertTrue(roadmap.getMembers().contains(member));
    }

    @DisplayName("소개 수정 화면")
    @Test
    @WithMember(NICKNAME)
    void updateDescriptionView() throws Exception {

        mockMvc.perform(get("/roadmap/"+roadmap.getEncodedPath()+"/settings/description"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("roadmap"))
                .andExpect(model().attributeExists("roadmapDescriptionForm"))
                .andExpect(view().name("roadmap/settings/description"))
                .andExpect(status().isOk())
                .andExpect(authenticated());
    }

    @DisplayName("소개 수정 화면 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateDescriptionSubmit_correct() throws Exception {

        mockMvc.perform(post("/roadmap/"+roadmap.getEncodedPath()+"/settings/description")
                .param("shortDescription","짧은 소개")
                .param("fullDescription","긴 소개")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + roadmap.getEncodedPath() +"/settings/description"))
                .andExpect(authenticated());

        assertTrue(roadmapRepository.findByPath(roadmap.getPath()).getShortDescription().equals("짧은 소개"));
        assertTrue(roadmapRepository.findByPath(roadmap.getPath()).getFullDescription().equals("긴 소개"));
    }

    @DisplayName("소개 수정 화면 - 입력값 오류")
    @Test
    @WithMember(NICKNAME)
    void updateDescriptionSubmit_error() throws Exception {

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/description")
                .param("shortDescription","..........................................................................................................................................")
                .param("fullDescription","긴 소개")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(authenticated());
    }

    @DisplayName("배너 이미지 수정 화면")
    @Test
    @WithMember(NICKNAME)
    void updateBannerView() throws Exception {

        mockMvc.perform(get("/roadmap/" + roadmap.getEncodedPath() + "/settings/banner"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("roadmap"))
                .andExpect(view().name("roadmap/settings/banner"))
                .andExpect(status().isOk())
                .andExpect(authenticated());
    }

    @DisplayName("배너 이미지 수정 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateBannerSubmit_correct() throws Exception {

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/banner")
                .param("image","/images/logo.png")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + roadmap.getEncodedPath() + "/settings/banner"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated());

        assertTrue(roadmapRepository.findByPath(roadmap.getPath()).getImage().equals("/images/logo.png"));
    }

    @DisplayName("배너 이미지 가능 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateBannerEnableSubmit_correct() throws Exception {

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/banner/enable")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + roadmap.getEncodedPath() + "/settings/banner"))
                .andExpect(authenticated());

        assertTrue(roadmapRepository.findByPath(roadmap.getPath()).isUseBanner());
    }

    @DisplayName("배너 이미지 불가능 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateBannerDisableSubmit_correct() throws Exception {

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/banner/disable")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + roadmap.getEncodedPath() + "/settings/banner"))
                .andExpect(authenticated());

        assertFalse(roadmapRepository.findByPath(roadmap.getPath()).isUseBanner());
    }

    @DisplayName("로드맵 태그 화면")
    @Test
    @WithMember(NICKNAME)
    void updateTagsView() throws Exception {

        mockMvc.perform(get("/roadmap/" + roadmap.getEncodedPath() + "/settings/tags"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("roadmap"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(view().name("roadmap/settings/tags"))
                .andExpect(status().isOk())
                .andExpect(authenticated());
    }

    @DisplayName("로드맵 태그 추가 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateTagsSubmit_remove_correct() throws Exception {
        assertFalse(tagRepository.existsByTitle("태그 타이틀"));
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("태그 타이틀");

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(authenticated());

        Tag newTag = tagRepository.findByTitle("태그 타이틀");
        assertNotNull(newTag);
        assertTrue(roadmapRepository.findByPath(roadmap.getPath()).getTags().contains(newTag));
    }

    @DisplayName("로드맵 태그 제거 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateTagsSubmit_correct() throws Exception {
        tagService.findOrCreateNew("태그 타이틀");

        assertTrue(tagRepository.existsByTitle("태그 타이틀"));
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("태그 타이틀");

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/tags/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(authenticated());

        Tag newTag = tagRepository.findByTitle("태그 타이틀");
        assertNotNull(newTag);
        assertTrue(tagRepository.existsByTitle("태그 타이틀"));
        assertFalse(roadmapRepository.findByPath(roadmap.getPath()).getTags().contains(newTag));
    }

    @DisplayName("로드맵 태그 제거 - 입력값 오류")
    @Test
    @WithMember(NICKNAME)
    void updateTagsSubmit_error() throws Exception {
        assertFalse(tagRepository.existsByTitle("태그 타이틀"));
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("태그 타이틀");

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/tags/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(authenticated());

        Tag newTag = tagRepository.findByTitle("태그 타이틀");
        assertNull(newTag);
        assertFalse(tagRepository.existsByTitle("태그 타이틀"));
    }

    @DisplayName("로드맵 수정 화면")
    @Test
    @WithMember(NICKNAME)
    void updateRoadmapView() throws Exception {

        mockMvc.perform(get("/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("roadmap"))
                .andExpect(view().name("roadmap/settings/roadmap"))
                .andExpect(status().isOk())
                .andExpect(authenticated());
    }

    @DisplayName("로드맵 수정 발행 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateRoadmapSubmit_publish_correct() throws Exception {

        roadmap.setPublished(false);
        assertFalse(roadmap.isPublished());

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap/publish")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + roadmap.getEncodedPath() +"/settings/roadmap"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated());

        assertTrue(roadmapRepository.findByPath(roadmap.getPath()).isPublished());
    }

    @DisplayName("로드맵 수정 정지 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateRoadmapSubmit_close_correct() throws Exception {

        roadmap.setPublished(true);
        roadmap.setClosed(false);
        assertTrue(roadmap.isPublished());
        assertFalse(roadmap.isClosed());

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap/close")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + roadmap.getEncodedPath() +"/settings/roadmap"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated());

        assertTrue(roadmapRepository.findByPath(roadmap.getPath()).isClosed());
    }

    @DisplayName("로드맵 수정 모집 시작 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateRoadmapSubmit_recruit_start_correct() throws Exception {

        roadmap.setPublished(true);
        roadmap.setClosed(false);
        roadmap.setRecruiting(false);
        roadmap.setRecruitingUpdatedDateTime(LocalDateTime.now().minusHours(1).minusMinutes(2));
        assertTrue(roadmap.canUpdateRecruiting());
        assertTrue(roadmap.isPublished());
        assertFalse(roadmap.isClosed());
        assertFalse(roadmap.isRecruiting());

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/recruit/start")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + roadmap.getEncodedPath() +"/settings/roadmap"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated());

        assertTrue(roadmapRepository.findByPath(roadmap.getPath()).isRecruiting());
    }

    @DisplayName("로드맵 수정 모집 시작 - 입력값 오류")
    @Test
    @WithMember(NICKNAME)
    void updateRoadmapSubmit_recruit_start_error() throws Exception {

        roadmap.setPublished(true);
        roadmap.setClosed(false);
        roadmap.setRecruiting(false);
        roadmap.setRecruitingUpdatedDateTime(LocalDateTime.now());
        assertFalse(roadmap.canUpdateRecruiting());
        assertTrue(roadmap.isPublished());
        assertFalse(roadmap.isClosed());
        assertFalse(roadmap.isRecruiting());

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/recruit/start")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + roadmap.getEncodedPath() +"/settings/roadmap"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated());

        assertFalse(roadmapRepository.findByPath(roadmap.getPath()).isRecruiting());
    }

    @DisplayName("로드맵 수정 모집 중단 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateRoadmapSubmit_recruit_stop_correct() throws Exception {

        roadmap.setPublished(true);
        roadmap.setClosed(false);
        roadmap.setRecruiting(true);
        roadmap.setRecruitingUpdatedDateTime(LocalDateTime.now().minusHours(1).minusMinutes(2));
        assertTrue(roadmap.canUpdateRecruiting());
        assertTrue(roadmap.isPublished());
        assertFalse(roadmap.isClosed());
        assertTrue(roadmap.isRecruiting());

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/recruit/stop")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + roadmap.getEncodedPath() +"/settings/roadmap"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated());

        assertFalse(roadmapRepository.findByPath(roadmap.getPath()).isRecruiting());
    }

    @DisplayName("로드맵 수정 경로 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateRoadmapSubmit_path_correct() throws Exception {

        assertNull(roadmapRepository.findByPath(roadmap.getPath()+"_unique"));
        assertFalse(roadmap.getPath().equals("testpath_unique"));

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap/path")
                .param("newPath",roadmap.getPath()+"_unique")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + roadmap.getEncodedPath() +"/settings/roadmap"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated());

        Roadmap result = roadmapRepository.findByPath(roadmap.getPath());
        log.info("result : "+result);
        assertNotNull(result);
        assertTrue(result.getPath().equals("testpath_unique"));
    }

    @DisplayName("로드맵 수정 경로 - 입력값 오류")
    @Test
    @WithMember(NICKNAME)
    void updateRoadmapSubmit_path_error() throws Exception {

        assertNotNull(roadmapRepository.findByPath(roadmap.getPath()));
        assertFalse(roadmap.getPath().equals("testpath_unique"));

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap/path")
                .param("newPath",roadmap.getPath())
                .with(csrf()))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("roadmap"))
                .andExpect(model().attributeExists("roadmapPathError"))
                .andExpect(view().name("roadmap/settings/roadmap"))
                .andExpect(status().isOk())
                .andExpect(authenticated());
    }

    @DisplayName("로드맵 수정 타이틀 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateRoadmapSubmit_title_correct() throws Exception {

        assertFalse(roadmap.getTitle().equals("타이틀 수정"));

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap/title")
                .param("newTitle","타이틀 수정")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/roadmap/" + roadmap.getEncodedPath() +"/settings/roadmap"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated());

        Roadmap result = roadmapRepository.findByPath(roadmap.getPath());
        assertNotNull(result);
        assertTrue(result.getTitle().equals("타이틀 수정"));
    }

    @DisplayName("로드맵 수정 타이틀 - 입력값 오류")
    @Test
    @WithMember(NICKNAME)
    void updateRoadmapSubmit_title_error() throws Exception {

        assertFalse(roadmap.getTitle().equals(""));

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap/title")
                .param("newTitle","")
                .with(csrf()))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("roadmap"))
                .andExpect(model().attributeExists("roadmapTitleError"))
                .andExpect(model().hasErrors())
                .andExpect(view().name("roadmap/settings/roadmap"))
                .andExpect(status().isOk());

        Roadmap result = roadmapRepository.findByPath(roadmap.getPath());
        assertNotNull(result);
        assertFalse(result.getTitle().equals(""));
    }

    @DisplayName("로드맵 수정 삭제 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateRoadmapSubmit_remove_correct() throws Exception {

        roadmap.setPublished(true);
        assertFalse(roadmap.isRemovable());
        roadmap.setPublished(false);
        assertTrue(roadmap.isRemovable());

        mockMvc.perform(post("/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap/remove")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Roadmap result = roadmapRepository.findByPath(roadmap.getPath());
        assertNull(result);
    }


}
