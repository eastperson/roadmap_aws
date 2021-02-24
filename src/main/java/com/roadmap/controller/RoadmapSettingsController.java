package com.roadmap.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadmap.dto.member.CurrentUser;
import com.roadmap.dto.member.form.TagForm;
import com.roadmap.dto.roadmap.form.RoadmapDescriptionForm;
import com.roadmap.dto.roadmap.form.RoadmapPathForm;
import com.roadmap.dto.roadmap.form.RoadmapTitleForm;
import com.roadmap.model.Member;
import com.roadmap.model.Roadmap;
import com.roadmap.model.Tag;
import com.roadmap.repository.TagRepository;
import com.roadmap.service.RoadmapService;
import com.roadmap.service.TagService;
import com.roadmap.validation.RoadmapPathFormValidator;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/roadmap/{path}/settings")
@Log4j2
public class RoadmapSettingsController {

    private final RoadmapService roadmapService;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final TagService tagService;
    private final RoadmapPathFormValidator roadmapPathFormValidator;

    @InitBinder("roadmapPathForm")
    public void roadmapPathValidator(WebDataBinder webDataBinder){
        webDataBinder.addValidators(roadmapPathFormValidator);
    }

    @GetMapping("/description")
    public String updateDescription(@CurrentUser Member member, @PathVariable String path, Model model) {

        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
        model.addAttribute(member);
        model.addAttribute(roadmap);
        model.addAttribute(modelMapper.map(roadmap, RoadmapDescriptionForm.class));

        return "roadmap/settings/description";
    }

    @PostMapping("/description")
    public String updateDescriptionSubmit(@CurrentUser Member member, @PathVariable String path, @Valid RoadmapDescriptionForm roadmapDescriptionForm, Errors errors, Model model, RedirectAttributes attributes) {
        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
        if(errors.hasErrors()){
            model.addAttribute(member);
            model.addAttribute(roadmap);
            return "roadmap/settings/description";
        }
        roadmapService.updateRoadmapDescription(roadmap, roadmapDescriptionForm);
        attributes.addFlashAttribute("message","로드맵 소개를 수정했습니다.");

        return "redirect:/roadmap/"+roadmap.getEncodedPath()+"/settings/description";
    }

    @GetMapping("/banner")
    public String updateImageForm(@CurrentUser Member member, @PathVariable String path, Model model) {

        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member, path);
        model.addAttribute(member);
        model.addAttribute(roadmap);

        return "roadmap/settings/banner";
    }

    @PostMapping("/banner")
    public String roadmapImageSubmit(@CurrentUser Member member, @PathVariable String path, String image, RedirectAttributes attributes){

        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
        roadmapService.updateRoadmapImage(roadmap,image);
        attributes.addFlashAttribute("message","로드맵 이미지를 수정했습니다.");
        return "redirect:/roadmap/" + roadmap.getEncodedPath() + "/settings/banner";

    }

   @PostMapping("/banner/enable")
   public String enableRoadmapBanner(@CurrentUser Member member, @PathVariable String path) {

        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
        roadmapService.enableRoadmapBanner(roadmap);
        return "redirect:/roadmap/" + roadmap.getEncodedPath() + "/settings/banner";
   }

   @PostMapping("/banner/disable")
    public String disableRoadmap(@CurrentUser Member member, @PathVariable String path){

        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
        roadmapService.disableRoadmapBanner(roadmap);
        return "redirect:/roadmap/" + roadmap.getEncodedPath() + "/settings/banner";
   }

   @GetMapping("/tags")
    public String roadmapTags(@CurrentUser Member member, @PathVariable String path, Model model) throws JsonProcessingException {

        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
        model.addAttribute(member);
        model.addAttribute(roadmap);

        model.addAttribute("tags",roadmap.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allTags));

        return "roadmap/settings/tags";
   }

   @PostMapping("/tags/add")
    public ResponseEntity RoadmapAddTag(@CurrentUser Member member, @PathVariable String path, @RequestBody TagForm tagForm, Model model){
        String title = tagForm.getTagTitle();

        Tag tag = tagService.findOrCreateNew(title);

        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);

        roadmapService.addTag(roadmap,tag);
        return ResponseEntity.ok().build();
   }

   @PostMapping("/tags/remove")
    public ResponseEntity roadmapRemoveTag(@CurrentUser Member member, @PathVariable String path,@RequestBody TagForm tagForm, Model model){

        String title = tagForm.getTagTitle();

        Tag tag = tagRepository.findByTitle(title);
        if(tag == null) {
            return ResponseEntity.badRequest().build();
        }

        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);

        roadmapService.removeTag(roadmap,tag);
        return ResponseEntity.ok().build();

   }

   @GetMapping("/roadmap")
    public String roadmapSettingsForm(@CurrentUser Member member, @PathVariable String path, Model model){
        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
        model.addAttribute(member);
        model.addAttribute(roadmap);
        return "roadmap/settings/roadmap";
   }

   @PostMapping("/roadmap/publish")
    public String publishRoadmap(@CurrentUser Member member, @PathVariable String path, RedirectAttributes attributes){
        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
        roadmapService.publish(roadmap);
        attributes.addFlashAttribute("message","스터디를 공개했습니다.");
        return "redirect:/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap";
   }

    @PostMapping("/roadmap/close")
    public String closeRoadmap(@CurrentUser Member member, @PathVariable String path, RedirectAttributes attributes,Model model){
        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
        roadmapService.close(roadmap);
        attributes.addFlashAttribute("message","스터디를 종료했습니다.");
        return "redirect:/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap";
    }

   @PostMapping("/recruit/start")
    public String startRecruit(@CurrentUser Member member, @PathVariable String path, Model model,RedirectAttributes attributes) {
        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
        if(!roadmap.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message","1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap";
        }
        roadmapService.startRecruit(roadmap);
        attributes.addFlashAttribute("message","인원 모집을 시작합니다.");
        return "redirect:/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap";
   }

   @PostMapping("/recruit/stop")
   public String stopRecruit(@CurrentUser Member member, @PathVariable String path, Model model, RedirectAttributes attributes){
        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
        if(!roadmap.canUpdateRecruiting()){
            attributes.addFlashAttribute("message","1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap";
        }

        roadmapService.stopRecruit(roadmap);
        attributes.addFlashAttribute("message","인원 모집을 종료합니다.");
        return "redirect:/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap";
   }

   @PostMapping("/roadmap/path")
    public String updateRoadmapPath(@CurrentUser Member member, @PathVariable String path, @Valid RoadmapPathForm roadmapPathForm, Errors errors
                                        , Model model, RedirectAttributes attributes){
       Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
       log.info(roadmapPathForm);
        if(errors.hasErrors()){
            log.info("----------------------error : " + errors);
            model.addAttribute(member);
            model.addAttribute(roadmap);
            model.addAttribute("roadmapPathError","해당 로드맵 경로는 사용할 수 없습니다.");
            return "roadmap/settings/roadmap";
        }
        roadmapService.updateRoadmapPath(roadmap,roadmapPathForm);
        attributes.addFlashAttribute("message","스터디 경로를 수정했습니다.");
        return "redirect:/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap";
   }

   @PostMapping("/roadmap/title")
    public String updateRoadmapTitle(@CurrentUser Member member, @PathVariable String path, @Valid RoadmapTitleForm roadmapTitleForm, Errors errors
                                        , Model model, RedirectAttributes attributes){
       Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
       if(errors.hasErrors()){
           model.addAttribute(member);
           model.addAttribute(roadmap);
           model.addAttribute("roadmapTitleError","해당 로드맵 타이틀은 사용할 수 없습니다.");
           return "roadmap/settings/roadmap";
       }
       roadmapService.updateRoadmapTitle(roadmap,roadmapTitleForm);
       attributes.addFlashAttribute("message","스터디 이름을 수정했습니다.");
       return "redirect:/roadmap/" + roadmap.getEncodedPath() + "/settings/roadmap";
   }

    @PostMapping("/roadmap/remove")
    public String removeRoadmap(@CurrentUser Member member, @PathVariable String path, Model model, RedirectAttributes attributes){
        Roadmap roadmap = roadmapService.getRoadmapToUpdate(member,path);
        roadmapService.remove(roadmap);
        return "redirect:/";
    }

}
