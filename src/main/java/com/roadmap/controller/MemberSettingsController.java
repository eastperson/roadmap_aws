package com.roadmap.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadmap.config.AppProperties;
import com.roadmap.dto.member.CurrentUser;
import com.roadmap.dto.member.form.LocationForm;
import com.roadmap.dto.member.form.NicknameForm;
import com.roadmap.dto.member.form.NotificationForm;
import com.roadmap.dto.member.form.PasswordForm;
import com.roadmap.dto.member.form.ProfileForm;
import com.roadmap.dto.member.form.TagForm;
import com.roadmap.model.Member;
import com.roadmap.model.Tag;
import com.roadmap.repository.MemberRepository;
import com.roadmap.repository.TagRepository;
import com.roadmap.service.MemberService;
import com.roadmap.service.TagService;
import com.roadmap.validation.NicknameFormValidator;
import com.roadmap.validation.PasswordFormVailidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/settings")
public class MemberSettingsController {

    private final NicknameFormValidator nicknameFormVailidator;
    private final ModelMapper modelMapper;
    private final MemberService memberService;
    private final PasswordFormVailidator passwordFormVailidator;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final TagService tagService;
    private final MemberRepository memberRepository;
    private final AppProperties appProperties;

    @InitBinder("passwordForm")
    public void passwordFormBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(passwordFormVailidator);
    }

    @InitBinder("nicknameForm")
    public void nicknameFormBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(nicknameFormVailidator);
    }

    @GetMapping("/profile")
    public String profileView(@CurrentUser Member member, Model model){
        Member curMember = memberRepository.findByNickname(member.getNickname());
        ProfileForm profileForm = modelMapper.map(curMember,ProfileForm.class);
        model.addAttribute(profileForm);
        model.addAttribute(curMember);

        return "member/settings/profile";
    }

    @PostMapping("/profile")
    public String profileView_submit(@CurrentUser Member member, Model model, @Valid ProfileForm profileForm, Errors errors, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(member);
            return "member/settings/profile";
        }

        memberService.updateProfile(member,profileForm);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");

        return "redirect:/settings/profile";
    }

    @GetMapping("/password")
    public String updatePassword(@CurrentUser Member member, Model model){

        model.addAttribute(new PasswordForm());
        model.addAttribute(member);

        return "member/settings/password";
    }

    @PostMapping("/password")
    public String updatePassword_submit(@CurrentUser Member member, @Valid PasswordForm passwordForm, Errors errors, Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(member);
            return "member/settings/password";
        }

        memberService.updatePassword(member,passwordForm.getNewPassword());
        attributes.addFlashAttribute("message","비밀번호를 변경했습니다.");

        return "redirect:/settings/password";
    }

    @GetMapping("/notifications")
    public String updateNotification(@CurrentUser Member member, Model model){
        model.addAttribute(member);
        model.addAttribute(modelMapper.map(member, NotificationForm.class));

        return "member/settings/notifications";
    }

    @PostMapping("/notifications")
    public String updateNotification_submit(@CurrentUser Member member, @Valid NotificationForm notificationForm, Errors errors, Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(member);
            return "member/settings/notifications";
        }

        memberService.updateNotification(member,notificationForm);
        attributes.addFlashAttribute("message","알림 설정을 변경했습니다.");

        return "redirect:/settings/notifications";
    }

    @GetMapping("/tags")
    public String updateTags(@CurrentUser Member member, Model model) throws JsonProcessingException {
        model.addAttribute(member);
        Set<Tag> tags = memberService.getTags(member);
        model.addAttribute("tags",tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allTags));

        return "member/settings/tags";
    }

    @PostMapping("tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Member member, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagService.findOrCreateNew(title);

        memberService.addTag(member, tag);

        log.info("member tag add..........................");

        return ResponseEntity.ok().build();
    }

    @PostMapping("tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Member member, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        memberService.removeTag(member, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/location")
    public String locationView(@CurrentUser Member member, Model model){
        Member withLoc = memberRepository.findWithLocByNickname(member.getNickname());
        if(withLoc.getLocation() != null) model.addAttribute(modelMapper.map(withLoc.getLocation(), LocationForm.class));
        else model.addAttribute(new LocationForm());
        model.addAttribute(member);

        model.addAttribute("kakaoJsKey",appProperties.getKakaoJsKey());
        model.addAttribute("kakaoRestKey",appProperties.getKakaoRestKey());

        return "member/settings/location";
    }

    @PostMapping("/location")
    public String locationView_submit(@CurrentUser Member member, LocationForm locationForm,  Model model,RedirectAttributes attributes){

        memberService.updateLocation(member,locationForm);
        attributes.addFlashAttribute("message","주소 정보 변경이 완료되었습니다.");

        return "redirect:/settings/location";
    }


    @GetMapping("/account")
    public String updateMemberView(@CurrentUser Member member, Model model){

        model.addAttribute(member);
        model.addAttribute(modelMapper.map(member,NicknameForm.class));

        return "member/settings/account";

    }

    @PostMapping("/account")
    public String updateMemberSubmit(@CurrentUser Member member, @Valid NicknameForm nicknameForm, Errors errors, Model model,RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(member);
            return "member/settings/account";
        }

        model.addAttribute(member);
        memberService.updateNickname(member,nicknameForm);
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");

        return "redirect:/settings/account";

    }

}
