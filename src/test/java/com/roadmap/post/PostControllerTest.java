package com.roadmap.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadmap.dto.roadmap.form.NodeAddForm;
import com.roadmap.dto.roadmap.form.PostForm;
import com.roadmap.dto.roadmap.form.RoadmapForm;
import com.roadmap.dto.roadmap.form.StageForm;
import com.roadmap.member.WithMember;
import com.roadmap.model.Member;
import com.roadmap.model.Node;
import com.roadmap.model.Post;
import com.roadmap.model.Roadmap;
import com.roadmap.model.Stage;
import com.roadmap.repository.MemberRepository;
import com.roadmap.repository.NodeRepository;
import com.roadmap.repository.PostRepository;
import com.roadmap.repository.RoadmapRepository;
import com.roadmap.service.NodeService;
import com.roadmap.service.PostService;
import com.roadmap.service.RoadmapService;
import com.roadmap.service.TagService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Log4j2
public class PostControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private RoadmapRepository roadmapRepository;
    @Autowired private RoadmapService roadmapService;
    @Autowired private ModelMapper modelMapper;
    @Autowired private NodeService nodeService;
    @Autowired private NodeRepository nodeRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PostRepository postRepository;
    @Autowired private PostService postService;
    @Autowired private TagService tagService;
    private Long nodeId = 0L;

    @BeforeEach
    public void before(){
        Member member = memberRepository.findByNickname("epepep");
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setTitle("title");
        roadmapForm.setShortDescription("short description");
        roadmapForm.setFullDescription("full description");
        roadmapForm.setPath("/path");
        Roadmap roadmap = roadmapService.registerForm(member,roadmapForm);
        StageForm stageForm = new StageForm();
        stageForm.setTitle("1단계");
        Stage stage = roadmapService.addNewStage(roadmap,modelMapper.map(stageForm, Stage.class));
        NodeAddForm nodeForm = new NodeAddForm();
        nodeForm.setNodeType("post");
        nodeForm.setTitle("노드 제목");
        nodeForm.setParentType("stage");
        nodeForm.setParentId(stage.getId());
        nodeId = nodeService.addNewNode(stage,nodeForm);
    }

    @Test
    @DisplayName("포스트 생성")
    @WithMember("epepep")
    void newPostFormView() throws Exception {
        Node node = nodeRepository.findById(nodeId).orElseThrow();

        MvcResult result = mockMvc.perform(get("/new-post/" + node.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("nodeId"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(view().name("roadmap/post/form"))
                .andExpect(authenticated()).andReturn();

        Post post = postRepository.findByNodeId(node.getId());
        assertNotNull(post);
    }

    @Test
    @DisplayName("포스트 생성 제출 - 입력값 정상")
    @WithMember("epepep")
    void newPostFormSubmit_correct() throws Exception {
        Node node = nodeRepository.findById(nodeId).orElseThrow();

        mockMvc.perform(get("/new-post/" + node.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("nodeId"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(view().name("roadmap/post/form"))
                .andExpect(authenticated());

        MvcResult result = mockMvc.perform(post("/new-post/" + node.getId())
                .param("title","제목")
                .param("description","설명")
                .param("content","내용")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated()).andReturn();

        Post post = postRepository.findByNodeId(node.getId());
        assertNotNull(post);
        assertTrue(post.getTitle().equals("제목"));
        assertTrue(post.getDescription().equals("설명"));
        assertTrue(post.getWriter().getNickname().equals("epepep"));
        assertTrue(post.getContent().equals("내용"));
        assertTrue(post.getNode().getId().equals(nodeId));
    }

    @Test
    @DisplayName("포스트 생성 제출 - 입력값 에러")
    @WithMember("epepep")
    void newPostFormSubmit_error() throws Exception {
        Node node = nodeRepository.findById(nodeId).orElseThrow();

        mockMvc.perform(get("/new-post/" + node.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("nodeId"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(view().name("roadmap/post/form"))
                .andExpect(authenticated());

        MvcResult result = mockMvc.perform(post("/new-post/" + node.getId())
                .param("title","")
                .param("description","설명")
                .param("content","내용")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(view().name("roadmap/post/form")).andReturn();

        Post post = postRepository.findByNodeId(node.getId());
        assertNotNull(post);
        assertNull(post.getTitle());
        assertNull(post.getDescription());
        assertNull(post.getContent());
        assertNull(post.getWriter());
        assertTrue(post.getNode().getId().equals(nodeId));
    }

    @Test
    @DisplayName("포스트 확인")
    @WithMember("epepep")
    void newPostView() throws Exception {
        Member member = memberRepository.findByNickname("epepep");

        PostForm postForm = new PostForm();
        postForm.setTitle("포스트 제목");
        postForm.setContent("포스트 내용");
        postForm.setDescription("포스트 설명");
        Post post = postService.createNewPost(nodeId);
        postService.update(member,post,postForm);

        Node node = nodeRepository.findById(nodeId).orElseThrow();
        log.info("node : " + node);
        Post newPost = postRepository.findByNodeId(node.getId());
        log.info("post : " + post.getId());


        MvcResult result = mockMvc.perform(get("/post/" + newPost.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("post"))
                .andExpect(view().name("roadmap/post/view"))
                .andExpect(authenticated()).andReturn();

        log.info("model : "+result.getModelAndView().getModel());
    }

    @Test
    @DisplayName("포스트 태그 추가_입력값 정상")
    @WithMember("epepep")
    void newPostAddTag_correct() throws Exception {
        Member member = memberRepository.findByNickname("epepep");

        Post post = createPost(member);

        Node node = nodeRepository.findById(nodeId).orElseThrow();

        mockMvc.perform(get("/new-post/" + node.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("nodeId"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(view().name("roadmap/post/form"))
                .andExpect(authenticated());

        MvcResult result = mockMvc.perform(post("/new-post/" + node.getId() + "/tags/add")
                .param("tagTitle","태그 타이틀")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(authenticated()).andReturn();

        Post newPost = postRepository.findByNodeId(node.getId());
        assertNotNull(newPost.getTags());

    }

    private Post createPost(Member member) {
        PostForm postForm = new PostForm();
        postForm.setTitle("포스트 제목");
        postForm.setContent("포스트 내용");
        postForm.setDescription("포스트 설명");
        Post post = postService.createNewPost(nodeId);
        return postService.update(member,post,postForm);
    }

}
