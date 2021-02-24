package com.roadmap.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadmap.dto.member.form.TagForm;
import com.roadmap.model.Member;
import com.roadmap.model.Tag;
import com.roadmap.repository.MemberRepository;
import com.roadmap.repository.TagRepository;
import com.roadmap.service.MemberService;
import com.roadmap.service.TagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
public class MemberSettingsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberService memberService;
    @Autowired private ModelMapper modelMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TagRepository tagRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TagService tagService;

    private final static String NICKNAME = "epepep";


    @DisplayName("프로필 화면")
    @WithMember(NICKNAME)
    @Test
    void profileView() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);

        mockMvc.perform(get("/profile/" + member.getNickname()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("isOwner"))
                .andExpect(model().attributeExists("member"))
                .andExpect(view().name("member/profile"));

    }

    @DisplayName("프로필 화면 - 에러(없는 회원)")
    @WithMember(NICKNAME)
    @Test
    void profileView_error() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);

        mockMvc.perform(get("/profile/" + member.getNickname() + "error"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("member/profile"));

    }

    @DisplayName("프로필 수정 화면")
    @WithMember(NICKNAME)
    @Test
    void profileUpdateView() throws Exception {

        mockMvc.perform(get("/settings/profile"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().attributeExists("member"))
                .andExpect(view().name("member/settings/profile"));

    }

    @DisplayName("프로필 수정 - 입력값 정상")
    @WithMember(NICKNAME)
    @Test
    void profileUpdate_submit_correct() throws Exception {
        mockMvc.perform(post("/settings/profile")
                .param("bio","자기소개")
                .param("profileImage","hththththththt")
                .param("occupation","백엔드개발자")
                .param("url","htttp://dsadasdasda")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));

        Member member = memberRepository.findWithLocByNickname(NICKNAME);
        assertTrue(member.getBio().equals("자기소개"));
        assertTrue(member.getProfileImage().equals("hththththththt"));
        assertTrue(member.getOccupation().equals("백엔드개발자"));
        assertTrue(member.getUrl().equals("htttp://dsadasdasda"));

    }

    @DisplayName("프로필 수정 - 입력값 에러")
    @WithMember(NICKNAME)
    @Test
    void profileUpdate_submit_error() throws Exception {
        mockMvc.perform(post("/settings/profile")
                .param("bio","자기소개dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd")
                .param("profileImage","hththththththt")
                .param("occupation","백엔드개발자")
                .param("url","htttp://dsadasdasda")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().hasErrors())
                .andExpect(view().name("member/settings/profile"));

        Member member = memberRepository.findByNickname(NICKNAME);
        assertFalse("자기소개".equals(member.getBio()));
        assertFalse("hththththththt".equals(member.getProfileImage()));
        assertFalse("백엔드개발자".equals(member.getOccupation()));
        assertFalse("htttp://dsadasdasda".equals(member.getUrl()));

    }

    @DisplayName("패스워드 수정 화면")
    @WithMember(NICKNAME)
    @Test
    void updatePasswordView() throws Exception {
        mockMvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("member"))
                .andExpect(authenticated())
                .andExpect(view().name("member/settings/password"));
    }

    @DisplayName("패스워드 수정 - 입력값 정상")
    @WithMember(NICKNAME)
    @Test
    void updatePassword_submit_correct() throws Exception {
        mockMvc.perform(post("/settings/password")
                .param("newPassword","11111111")
                .param("newPasswordConfirm","11111111")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(authenticated());

        Member member = memberRepository.findByNickname(NICKNAME);
        assertTrue(passwordEncoder.matches("11111111",member.getPassword()));
    }

    @DisplayName("패스워드 수정 - 입력값 오류_blank")
    @WithMember(NICKNAME)
    @Test
    void updatePassword_submit_error_blank() throws Exception {
        mockMvc.perform(post("/settings/password")
                .param("newPassword","")
                .param("newPasswordConfirm","")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("member"))
                .andExpect(authenticated());
    }

    @DisplayName("패스워드 수정 - 입력값 오류_형식")
    @WithMember(NICKNAME)
    @Test
    void updatePassword_submit_error_input() throws Exception {
        mockMvc.perform(post("/settings/password")
                .param("newPassword","11")
                .param("newPasswordConfirm","11")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("member"))
                .andExpect(authenticated());
    }

    @DisplayName("패스워드 수정 - 입력값 오류_불일치")
    @WithMember(NICKNAME)
    @Test
    void updatePassword_submit_error_incorrect() throws Exception {
        mockMvc.perform(post("/settings/password")
                .param("newPassword","11111111")
                .param("newPasswordConfirm","11111112")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("member"))
                .andExpect(authenticated());
    }

    @DisplayName("알림 설정 변경 화면")
    @WithMember(NICKNAME)
    @Test
    void updateNotificationView() throws Exception {
        mockMvc.perform(get("/settings/notifications"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("notificationForm"))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(view().name("member/settings/notifications"));
    }

    @DisplayName("알림 설정 변경 - 입력값 정상")
    @WithMember(NICKNAME)
    @Test
    void updateNotification_submit_correct() throws Exception {
        mockMvc.perform(post("/settings/notifications")
                .param("roadmapCreatedByEmail","true")
                .param("roadmapCreatedByWeb","true")
                .param("roadmapEnrollmentResultByEmail","true")
                .param("roadmapEnrollmentResultByWeb","true")
                .param("roadmapUpdatedByEmail","true")
                .param("roadmapUpdatedByWeb","true")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/notifications"));

        Member member = memberRepository.findByNickname(NICKNAME);
        assertTrue(member.isRoadmapCreatedByEmail());
        assertTrue(member.isRoadmapCreatedByWeb());
        assertTrue(member.isRoadmapEnrollmentResultByEmail());
        assertTrue(member.isRoadmapEnrollmentResultByWeb());
        assertTrue(member.isRoadmapUpdatedByEmail());
        assertTrue(member.isRoadmapUpdatedByWeb());
    }

    @DisplayName("알림 설정 변경 - 입력값 오ꇜ")
    @WithMember(NICKNAME)
    @Test
    void updateNotification_submit_error() throws Exception {
        mockMvc.perform(post("/settings/notifications")
                .param("roadmapCreatedByEmail","true")
                .param("roadmapCreatedByWeb",".")
                .param("roadmapEnrollmentResultByEmail","true")
                .param("roadmapEnrollmentResultByWeb","true")
                .param("roadmapUpdatedByEmail","true")
                .param("roadmapUpdatedByWeb","true")
                .with(csrf()))
                .andExpect(model().hasErrors())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("member"));

        Member member = memberRepository.findByNickname(NICKNAME);
        assertFalse(member.isRoadmapCreatedByEmail());
        assertTrue(member.isRoadmapCreatedByWeb());
        assertFalse(member.isRoadmapEnrollmentResultByEmail());
        assertTrue(member.isRoadmapEnrollmentResultByWeb());
        assertFalse(member.isRoadmapUpdatedByEmail());
        assertTrue(member.isRoadmapUpdatedByWeb());
    }

    @DisplayName("태그 설정 변경")
    @Test
    @WithMember(NICKNAME)
    void updateTagsView() throws Exception{
        mockMvc.perform(get("/settings/tags"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/settings/tags"));

    }

    @Transactional
    @DisplayName("태그 설정 - 태그 추가")
    @Test
    @WithMember(NICKNAME)
    void updateTags_add_correct() throws Exception{
        assertNull(tagRepository.findByTitle("new"));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("new");

        mockMvc.perform(post("/settings/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());
        Tag newTag = tagRepository.findByTitle("new");
        assertNotNull(newTag);
        assertTrue(memberRepository.findWithAllByNickname(NICKNAME).getTags().contains(newTag));

    }

    @Transactional
    @DisplayName("태그 설정 - 태그 삭제")
    @Test
    @WithMember(NICKNAME)
    void updateTags_remove_correct() throws Exception{
        Member member = memberRepository.findByNickname(NICKNAME);
        Tag tag = tagRepository.save(Tag.builder().title("new").build());
        memberService.addTag(member,tag);

        assertTrue(member.getTags().contains(tag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("new");

        mockMvc.perform(post("/settings/tags/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("new");

        assertFalse(memberRepository.findWithAllByNickname(NICKNAME).getTags().contains(newTag));

    }

    @DisplayName("주소 설정 변경 화면")
    @Test
    @WithMember(NICKNAME)
    void updateLocationView() throws Exception {
        mockMvc.perform(get("/settings/location"))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(model().attributeExists("locationForm"))
                .andExpect(model().attributeExists("kakaoJsKey"))
                .andExpect(model().attributeExists("kakaoRestKey"))
                .andExpect(model().attributeExists("member"))
                .andExpect(view().name("member/settings/location"));

    }

    @DisplayName("주소 설정 변경 화면 - 입력값 정상")
    @Test
    @WithMember(NICKNAME)
    void updateLocation_submit_correct() throws Exception {
        assertNull(memberRepository.findWithLocByNickname(NICKNAME).getLocation());

        mockMvc.perform(post("/settings/location")
                .param("addr","주소")
                .param("addrDetail","aaa")
                .param("siNm","bbb")
                .param("sggNm","ccc")
                .param("emdNm","ddd")
                .param("lat","43.00043")
                .param("lng","24.33334"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/location"));

        Member member = memberRepository.findWithLocByNickname(NICKNAME);
        assertNotNull(member.getLocation());
        assertTrue(member.getLocation().getAddr().equals("주소"));
        assertTrue(member.getLocation().getAddrDetail().equals("aaa"));
        assertTrue(member.getLocation().getSiNm().equals("bbb"));
        assertTrue(member.getLocation().getSggNm().equals("ccc"));
        assertTrue(member.getLocation().getEmdNm().equals("ddd"));
        assertTrue(member.getLocation().getLat().equals(43.00043));
        assertTrue(member.getLocation().getLng().equals(24.33334));

    }

    @DisplayName("주소 팝업 화면")
    @Test
    @WithMember(NICKNAME)
    public void jusoPopupView() throws Exception {
        mockMvc.perform(get("/popup/jusoPopup"))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(model().attributeExists("confmKey"))
                .andExpect(model().attributeDoesNotExist("inputYn"))
                .andExpect(model().attributeDoesNotExist("roadAddrPart1"))
                .andExpect(model().attributeDoesNotExist("addrDetail"))
                .andExpect(model().attributeDoesNotExist("siNm"))
                .andExpect(model().attributeDoesNotExist("sggNm"))
                .andExpect(model().attributeDoesNotExist("emdNm"))
                .andExpect(view().name("popup/jusoPop"));
    }

    @DisplayName("주소 팝업 제출")
    @Test
    @WithMember(NICKNAME)
    public void jusoPopup_submit() throws Exception {
        mockMvc.perform(post("/popup/jusoPopup")
                .param("inputYn","Y")
                .param("roadAddrPart1","주소")
                .param("addrDetail","aaa")
                .param("siNm","bbb")
                .param("sggNm","ccc")
                .param("emdNm","ddd"))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(model().attributeExists("confmKey"))
                .andExpect(model().attributeExists("inputYn"))
                .andExpect(model().attributeExists("roadAddrPart1"))
                .andExpect(model().attributeExists("addrDetail"))
                .andExpect(model().attributeExists("siNm"))
                .andExpect(model().attributeExists("sggNm"))
                .andExpect(model().attributeExists("emdNm"))
                .andExpect(view().name("popup/jusoPop"));
    }

    @DisplayName("계정 수정 화면")
    @Test
    @WithMember(NICKNAME)
    public void accountView() throws Exception {
        mockMvc.perform(get("/settings/account"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("nicknameForm"))
                .andExpect(view().name("member/settings/account"))
                .andExpect(authenticated());
    }

    @DisplayName("계정 수정 입력 - 입력값 에러")
    @Test
    @WithMember(NICKNAME)
    public void accountSubmit_error_incorrect() throws Exception {
        mockMvc.perform(post("/settings/account")
                .param("nickname","d")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("member"))
                .andExpect(model().hasErrors())
                .andExpect(view().name("member/settings/account"))
                .andExpect(authenticated());

    }

    @DisplayName("계정 수정 입력 - 이미 존재하는 닉네임")
    @Test
    @WithMember(NICKNAME)
    public void accountSubmit_error_exist() throws Exception {
        mockMvc.perform(post("/settings/account")
                .param("nickname",NICKNAME)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("member"))
                .andExpect(model().hasErrors())
                .andExpect(view().name("member/settings/account"))
                .andExpect(authenticated());
    }

    @DisplayName("계정 수정 입력 - 에러")
    @Test
    @WithMember(NICKNAME)
    @Transactional
    public void accountSubmit_error() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);

        mockMvc.perform(post("/settings/account")
                .param("nickname","eastperson321")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/account"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated());

        member = memberRepository.findByEmail(member.getEmail());

        assertTrue(member.getNickname().equals("eastperson321"));
    }
}
