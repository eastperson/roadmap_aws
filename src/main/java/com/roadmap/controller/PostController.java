package com.roadmap.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadmap.dto.member.CurrentUser;
import com.roadmap.dto.member.form.TagForm;
import com.roadmap.dto.post.form.PostModifyForm;
import com.roadmap.dto.roadmap.form.PostForm;
import com.roadmap.model.Member;
import com.roadmap.model.Post;
import com.roadmap.model.Tag;
import com.roadmap.repository.PostRepository;
import com.roadmap.repository.TagRepository;
import com.roadmap.service.PostService;
import com.roadmap.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Log4j2
public class PostController {

    private final PostRepository postRepository;
    private final PostService postService;
    private final TagRepository tagRepository;
    private final TagService tagService;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;


    @GetMapping("/new-post/{nodeId}")
    public String newPostForm(@CurrentUser Member member, Model model, @PathVariable("nodeId") Long nodeId) throws JsonProcessingException {
        Post post = postRepository.findByNodeId(nodeId);
        if (post == null) {
            postService.createNewPost(nodeId);
        }

        model.addAttribute("nodeId", nodeId);
        model.addAttribute(member);
        model.addAttribute(new PostForm());
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "roadmap/post/form";
    }

    @GetMapping("/post/{postId}")
    public String postView(@CurrentUser Member member, Model model, @PathVariable Long postId){
        Post post = postRepository.findById(postId).orElseThrow();

        model.addAttribute(member);
        model.addAttribute(post);

        return "roadmap/post/view";
    }

    @PostMapping("/new-post/{nodeId}")
    public String newPostFormSubmit(@CurrentUser Member member, @Valid PostForm postForm, Errors errors, Model model, @PathVariable("nodeId") Long nodeId) throws JsonProcessingException {
        if (errors.hasErrors()) {
            model.addAttribute("nodeId", nodeId);
            model.addAttribute(member);
            model.addAttribute(postForm);
            return "roadmap/post/form";
        }
        Post post = postRepository.findByNodeId(nodeId);
        Post newPost = postService.update(member,post, postForm);

        return "redirect:/post/" + newPost.getId();
    }

    @PostMapping("/new-post/{nodeId}/tags/add")
    public ResponseEntity newPostAddTag(@CurrentUser Member member, @PathVariable Long nodeId, @RequestBody TagForm tagForm, Model model) {

        log.info("---------------------post add tag");

        String title = tagForm.getTagTitle();

        Tag tag = tagService.findOrCreateNew(title);

        Post post = postRepository.findByNodeId(nodeId);

        postService.addTag(post, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/new-post/{nodeId}/tags/remove")
    public ResponseEntity newPostRemoveTag(@CurrentUser Member member, @PathVariable Long nodeId, @RequestBody TagForm tagForm) {

        String title = tagForm.getTagTitle();

        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        Post post = postRepository.findByNodeId(nodeId);

        postService.removeTag(post, tag);
        return ResponseEntity.ok().build();

    }

    @GetMapping("/post/{postId}/modify")
    public String postModifyView(@CurrentUser Member member, @PathVariable Long postId, Model model) throws JsonProcessingException {
        Post post = postRepository.findById(postId).orElseThrow();

        model.addAttribute(member);
        model.addAttribute("writer",post.getWriter());
        model.addAttribute(modelMapper.map(post,PostModifyForm.class));
        model.addAttribute("postId",postId);
        model.addAttribute("nodeId",post.getNode().getId());
        model.addAttribute("tags",post.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "/roadmap/post/modify";
    }

    @PostMapping("/post/{postId}/modify")
    public String postModifySuvmit(@CurrentUser Member member, @PathVariable Long postId, Model model, @Valid PostModifyForm postModifyForm, Errors errors){
        Post post = postRepository.findById(postId).orElseThrow();
        if(errors.hasErrors()){
            model.addAttribute(member);
            model.addAttribute(modelMapper.map(post,PostModifyForm.class));
            return "/roadmap/post/modify";
        }
        postService.modify(post,postModifyForm);

        return "redirect:/post/"+post.getId()+"/modify";
    }

    @PostMapping("/post/{postId}/remove")
    public String postRemove(@CurrentUser Member member, @PathVariable Long postId, Model model){
        Post post = postRepository.findById(postId).orElseThrow();
        postRepository.delete(post);

        return "redirect:/post/list";
    }

    @PostMapping("/post/{postId}/tags/add")
    public ResponseEntity postAddTag(@CurrentUser Member member, @PathVariable Long postId, @RequestBody TagForm tagForm, Model model) {
        log.info("tags add : " + tagForm);

        String title = tagForm.getTagTitle();

        Tag tag = tagService.findOrCreateNew(title);

        Post post = postRepository.findById(postId).orElseThrow();

        postService.addTag(post, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/post/{postId}/tags/remove")
    public ResponseEntity postRemoveTag(@CurrentUser Member member, @PathVariable Long postId, @RequestBody TagForm tagForm) {

        String title = tagForm.getTagTitle();

        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        Post post = postRepository.findById(postId).orElseThrow();

        postService.removeTag(post, tag);
        return ResponseEntity.ok().build();

    }
}
