package com.roadmap.roadmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadmap.config.AppProperties;
import com.roadmap.dto.roadmap.form.NodeAddForm;
import com.roadmap.dto.roadmap.form.RoadmapForm;
import com.roadmap.dto.roadmap.form.StageForm;
import com.roadmap.member.WithMember;
import com.roadmap.model.Member;
import com.roadmap.model.Node;
import com.roadmap.model.Roadmap;
import com.roadmap.model.Stage;
import com.roadmap.repository.MemberRepository;
import com.roadmap.repository.NodeRepository;
import com.roadmap.repository.RoadmapRepository;
import com.roadmap.repository.StageRepository;
import com.roadmap.service.NodeService;
import com.roadmap.service.RoadmapService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class RoadmapRestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private RoadmapRepository roadmapRepository;
    @Autowired private RoadmapService roadmapService;
    @Autowired private AppProperties appProperties;
    @Autowired private StageRepository stageRepository;
    @Autowired private ModelMapper modelMapper;
    @Autowired private NodeService nodeService;
    @Autowired private NodeRepository nodeRepository;

    private final static String NICKNAME = "epepep";

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("스테이지 만들기 - 입력값 정상")
    @WithMember(NICKNAME)
    @Test
    void newStage() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);
        Roadmap roadmap = roadmapRepository.findByPath("testpath");
        StageForm stageForm = new StageForm();
        stageForm.setTitle("제목 테스트");

        mockMvc.perform(post("/roadmap/api/{path}/stage/new",roadmap.getEncodedPath())
                .content(asJsonString(stageForm))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + appProperties.getRoadmapApiKey()))
                .andExpect(status().isOk())
                .andExpect(authenticated());

    }

    @DisplayName("스테이지 만들기 - 토큰 오류")
    @WithMember(NICKNAME)
    @Test
    void newStage_error_token() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);
        Roadmap roadmap = roadmapRepository.findByPath("testpath");
        StageForm stageForm = new StageForm();
        stageForm.setTitle("제목 테스트");

        mockMvc.perform(post("/roadmap/api/{path}/stage/new",roadmap.getEncodedPath())
                .content(asJsonString(stageForm))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                //.header("Authorization","Bearer " + appProperties.getRoadmapApiKey()))
                .andExpect(status().isForbidden())
                .andExpect(authenticated());

    }

    @DisplayName("스테이지 만들기 - 입력값 오류")
    @WithMember(NICKNAME)
    @Test
    void newStage_error_input() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);
        Roadmap roadmap = roadmapRepository.findByPath("testpath");
        StageForm stageForm = new StageForm();
        stageForm.setTitle("0000000000000000000000000000000000000003232132300000000000000000000");

        mockMvc.perform(post("/roadmap/api/{path}/stage/new",roadmap.getEncodedPath())
                .content(asJsonString(stageForm))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + appProperties.getRoadmapApiKey()))
                .andExpect(status().isBadRequest())
                .andExpect(authenticated());

    }

    @DisplayName("스테이지 제거")
    @WithMember(NICKNAME)
    @Test
    void removeStage() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);

        Roadmap roadmap = roadmapRepository.findByPath("testpath");
        StageForm stageForm = new StageForm();
        stageForm.setTitle("제목 테스트");

        Stage stage = roadmapService.addNewStage(roadmap,modelMapper.map(stageForm, Stage.class));

        mockMvc.perform(delete("/roadmap/api/{path}/stage/remove",roadmap.getEncodedPath())
                .param("id",stage.getId().toString())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + appProperties.getRoadmapApiKey()))
                .andExpect(status().isOk())
                .andExpect(authenticated());

        Optional<Stage> result = stageRepository.findById(stage.getId());
        assertFalse(result.isPresent());

    }

    @DisplayName("스테이지 리스트 확인하기")
    @WithMember(NICKNAME)
    @Test
    void getStageList() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);

        Roadmap roadmap = roadmapRepository.findByPath("testpath");
        StageForm stageForm = new StageForm();
        stageForm.setTitle("제목 테스트");

        Stage stage = roadmapService.addNewStage(roadmap,modelMapper.map(stageForm, Stage.class));

        mockMvc.perform(get("/roadmap/api/{path}/stage/getList",roadmap.getEncodedPath())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + appProperties.getRoadmapApiKey()))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andDo(print());

        Optional<Stage> result = stageRepository.findById(stage.getId());
        assertTrue(result.isPresent());

    }

    @DisplayName("스테이지 순서로 확인하기")
    @WithMember(NICKNAME)
    @Test
    void getStageByOrd() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);

        Roadmap roadmap = roadmapRepository.findByPath("testpath");
        StageForm stageForm = new StageForm();
        stageForm.setTitle("제목 테스트");

        Stage stage = roadmapService.addNewStage(roadmap,modelMapper.map(stageForm, Stage.class));

        mockMvc.perform(get("/roadmap/api/{path}/stage/get/{ord}",roadmap.getEncodedPath(),stage.getOrd())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + appProperties.getRoadmapApiKey()))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andDo(print());

        Optional<Stage> result = stageRepository.findById(stage.getId());
        assertTrue(result.isPresent());

    }

    @DisplayName("노드 만들기 - 스테이지")
    @WithMember(NICKNAME)
    @Test
    void newNode_stage_correct() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);
        Roadmap roadmap = roadmapRepository.findByPath("testpath");
        StageForm stageForm = new StageForm();
        stageForm.setTitle("제목 테스트");
        Stage stage = roadmapService.addNewStage(roadmap,modelMapper.map(stageForm,Stage.class));
        NodeAddForm nodeForm = new NodeAddForm();
        nodeForm.setId(stage.getId());
        nodeForm.setNodeType("text");
        nodeForm.setTitle("노드 제목 테스트");
        nodeForm.setParentType("stage");

        mockMvc.perform(post("/roadmap/api/{path}/node/new",roadmap.getEncodedPath())
                .param("id",stage.getId().toString())
                .content(asJsonString(nodeForm))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + appProperties.getRoadmapApiKey()))
                .andExpect(status().isOk())
                .andExpect(authenticated());

        Stage newStage = stageRepository.findById(stage.getId()).orElseThrow();
        assertTrue(newStage.getNodeList().size() > 0);

    }

    @DisplayName("노드 만들기 - 부모 노드")
    @WithMember(NICKNAME)
    @Test
    void newNode_node_correct() throws Exception {
        Member member = memberRepository.findByNickname(NICKNAME);
        RoadmapForm roadmapForm = new RoadmapForm();
        roadmapForm.setPath("testpath");
        roadmapForm.setTitle("제목 테스트");
        roadmapForm.setFullDescription("긴 소개 테스트");
        roadmapForm.setShortDescription("짧은 소개 테스트");
        roadmapService.registerForm(member,roadmapForm);
        Roadmap roadmap = roadmapRepository.findByPath("testpath");
        StageForm stageForm = new StageForm();
        stageForm.setTitle("제목 테스트");
        Stage stage = roadmapService.addNewStage(roadmap,modelMapper.map(stageForm,Stage.class));
        NodeAddForm nodeForm = new NodeAddForm();
        nodeForm.setNodeType("text");
        nodeForm.setTitle("노드 제목 테스트");
        nodeForm.setParentType("stage");
        Long newId = nodeService.addNewNode(stage,nodeForm);
        Node node = nodeRepository.findById(newId).orElseThrow();
        nodeForm.setParentType("node");
        nodeForm.setId(node.getId());


        mockMvc.perform(post("/roadmap/api/{path}/node/new",roadmap.getEncodedPath())
                .content(asJsonString(nodeForm))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + appProperties.getRoadmapApiKey()))
                .andExpect(status().isOk())
                .andExpect(authenticated());

        Node newNode = nodeRepository.findById(node.getId()).orElseThrow();
        assertTrue(newNode.getChilds().size() > 0);

    }

}
