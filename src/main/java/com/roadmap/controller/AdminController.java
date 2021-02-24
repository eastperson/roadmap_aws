package com.roadmap.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadmap.dto.admin.member.MemberInfoForm;
import com.roadmap.dto.member.MemberDTO;
import com.roadmap.dto.page.PageRequestDTO;
import com.roadmap.dto.page.PageResultDTO;
import com.roadmap.model.Member;
import com.roadmap.model.Tag;
import com.roadmap.repository.MemberRepository;
import com.roadmap.repository.TagRepository;
import com.roadmap.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Log4j2
public class AdminController {

    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final MemberService memberService;
    private final ModelMapper modelMapper;

    @GetMapping("")
    public String index(Model model){

        Long count = memberRepository.count();
        model.addAttribute("count",count);

        return "admin/index";
    }

    @GetMapping("/member")
    public String memberList(Model model, Integer page,Integer size, String type, String keyword){
        if(page == null || page <= 0) page = 1;
        if(size == null || size <= 0) size = 10;
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder().size(size).page(page).type(type).keyword(keyword).build();

        PageResultDTO<MemberDTO,Member> pageResultDTO = memberService.getList(pageRequestDTO);
        List<Member> memberList = new ArrayList<>();
        for (MemberDTO memberDTO : pageResultDTO.getDtoList()) {
            memberList.add(memberDTO.dtoToEntity(modelMapper));
        }
        model.addAttribute("memberList",memberList);
        model.addAttribute("pageResultDTO",pageResultDTO);
        model.addAttribute("pageRequestDTO",pageRequestDTO);

        return "admin/member/list";
    }

    @GetMapping("/member/{id}")
    public String memberView(@PathVariable Long id, Model model,PageRequestDTO pageRequestDTO) throws JsonProcessingException {
        Optional<Member> result = memberRepository.findById(id);

        if(result.isPresent()) {
            Member member = result.get();

            model.addAttribute(member);

            Set<Tag> tags = memberService.getTags(member);
            model.addAttribute("tags",tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
            List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
            model.addAttribute("whitelist",objectMapper.writeValueAsString(allTags));
        }
        model.addAttribute(pageRequestDTO);

        return "admin/member/view";
    }

    @GetMapping("/member/{id}/modify")
    public String memberModify(@PathVariable Long id, Model model,@ModelAttribute  PageRequestDTO pageRequestDTO) throws JsonProcessingException {
        Optional<Member> result = memberRepository.findById(id);

        if(result.isPresent()) {
            Member member = result.get();

            model.addAttribute(member);

            Set<Tag> tags = memberService.getTags(member);
            model.addAttribute("tags",tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
            List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
            model.addAttribute("whitelist",objectMapper.writeValueAsString(allTags));
            model.addAttribute(pageRequestDTO);
        }

        return "admin/member/modify";
    }

    @PostMapping("/member/{id}/modify")
    public String memberModify_submit(@Valid MemberInfoForm memberInfoForm, Errors errors, Model model,PageRequestDTO pageRequestDTO, @PathVariable Long id, RedirectAttributes attributes) throws JsonProcessingException {
        Member member = memberRepository.findById(id).orElseThrow();
        if(errors.hasErrors()){
            model.addAttribute(member);
            Set<Tag> tags = memberService.getTags(member);
            model.addAttribute("tags",tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
            List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
            model.addAttribute("whitelist",objectMapper.writeValueAsString(allTags));
            model.addAttribute(pageRequestDTO);
            return "admin/member/modify";
    }
        memberService.updateInfo(memberInfoForm,member);

        attributes.addFlashAttribute("message","수정이 완료되었습니다.");
        attributes.addAttribute("keyword",pageRequestDTO.getKeyword());
        attributes.addAttribute("page",pageRequestDTO.getPage());
        attributes.addAttribute("size",pageRequestDTO.getSize());

        return "redirect:/admin/member/"+id;
    }



    @GetMapping("/member_admin")
    public String memberAdminList(Model model, Integer page,Integer size, String type, String keyword){
        if(page == null || page <= 0) page = 1;
        if(size == null || size <= 0) size = 10;
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder().size(size).page(page).type(type).keyword(keyword).build();

        PageResultDTO<MemberDTO,Member> pageResultDTO = memberService.getAdminList(pageRequestDTO);

        List<Member> memberList = new ArrayList<>();

        for (MemberDTO memberDTO : pageResultDTO.getDtoList()) {
            memberList.add(memberDTO.dtoToEntity(modelMapper));
        }
        model.addAttribute("memberList",memberList);
        model.addAttribute("pageResultDTO",pageResultDTO);
        model.addAttribute("pageRequestDTO",pageRequestDTO);

        return "admin/member/list";
    }


}
