package com.roadmap.roadmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadmap.dto.roadmap.form.RoadmapForm;
import com.roadmap.member.WithMember;
import com.roadmap.model.Member;
import com.roadmap.model.Node;
import com.roadmap.model.Roadmap;
import com.roadmap.model.Stage;
import com.roadmap.repository.MemberRepository;
import com.roadmap.repository.NodeRepository;
import com.roadmap.repository.RoadmapRepository;
import com.roadmap.repository.StageRepository;
import com.roadmap.service.RoadmapService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Log4j2
@Transactional
public class RoadmapRepositoryTest {

    @Autowired private RoadmapRepository roadmapRepository;
    @Autowired private RoadmapService roadmapService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private StageRepository stageRepository;
    @Autowired private NodeRepository nodeRepository;
    @Autowired private ObjectMapper objectMapper;

    @DisplayName("로드맵 findByPath")
    @Test
    @WithMember("epepep")
    void findByPath(){
        RoadmapForm roadmapForm = RoadmapForm.builder()
                .title("제목")
                .path("path")
                .shortDescription("짧은 소개")
                .fullDescription("긴 소개")
                .build();

        Member member = memberRepository.findByNickname("epepep");

        roadmapService.registerForm(member,roadmapForm);

        Roadmap roadmap = roadmapRepository.findByPath("path");

        log.info("roadmap : "+roadmap);

    }


    @DisplayName("로드맵 with stageList,nodeList,members")
    @Test
    @WithMember("epepep")
    void findWithAllByPath(){
        RoadmapForm roadmapForm = RoadmapForm.builder()
                .title("제목")
                .path("path")
                .shortDescription("짧은 소개")
                .fullDescription("긴 소개")
                .build();

        Member member = memberRepository.findByNickname("epepep");

        Roadmap newRoadmap = roadmapService.registerForm(member,roadmapForm);
        Stage stage = new Stage();
        stage.setHead(true);
        stage.setTitle("1단계");

        Node node = new Node();
        node.setTitle("title");
        node.setText("content");

        Node node2 = new Node();
        node.setTitle("title2");
        node.setText("content2");

        node.getChilds().add(node2);
        node2.setParent(node);

        newRoadmap.getStageList().add(stage);
        newRoadmap.getStageList().get(0).getNodeList().add(node);
        //newRoadmap.getStageList().get(0).getNodeList().add(node2);
        roadmapRepository.save(newRoadmap);
        nodeRepository.save(node);
        nodeRepository.save(node2);
        stageRepository.save(stage);

        Roadmap roadmap = roadmapRepository.findWithAllByPath("path");
        assertNotNull(roadmap.getOwner());
        assertNotNull(roadmap.getStageList());
        assertNotNull(roadmap.getOwner());
        assertNotNull(roadmap.getStageList().get(0).getNodeList());
        //assertNotNull(roadmap.getLikeMembers());

        log.info("roadmap : "+roadmap);
        log.info("roadmap stage list : " + roadmap.getStageList());
        log.info("roadmap stage node list : " + roadmap.getStageList().get(0).getNodeList());
    }

//    @Test
//    void findNodeWithById(){
//        Node node = nodeRepository.findById(130L).orElseThrow();
//
//        log.info(node.getStage());
//    }
/*
    @Test
    void test() throws JsonProcessingException {
        Roadmap roadmap = roadmapRepository.findWithAllByPath("test2");
        log.info("roadmap : "+roadmap);
        log.info("list : "+roadmap.getStageList());
        log.info("json : "+objectMapper.writeValueAsString(roadmap.getStageList()));
        log.info(roadmap.getStageList().get(0).getRoadmap());
    }
*/
}
