package com.roadmap.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadmap.config.AppProperties;
import com.roadmap.dto.roadmap.NodeDTO;
import com.roadmap.dto.roadmap.StageDTO;
import com.roadmap.dto.roadmap.form.NodeAddForm;
import com.roadmap.dto.roadmap.form.NodeModalForm;
import com.roadmap.dto.roadmap.form.StageForm;
import com.roadmap.model.Node;
import com.roadmap.model.Roadmap;
import com.roadmap.model.Stage;
import com.roadmap.repository.NodeRepository;
import com.roadmap.repository.RoadmapRepository;
import com.roadmap.repository.StageRepository;
import com.roadmap.service.NodeService;
import com.roadmap.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Log4j2
@RequestMapping("/roadmap/api/{path}")
@RequiredArgsConstructor
public class RoadmapRestController {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapService roadmapService;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;
    private final StageRepository stageRepository;
    private final NodeRepository nodeRepository;
    private final NodeService nodeService;


    @PostMapping("/stage/new")
    public ResponseEntity<StageDTO> registerStage(@RequestBody @Valid StageForm stageForm, Errors errors, @PathVariable String path) throws JsonProcessingException {
        if(errors.hasErrors()) {
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
        }

        Roadmap roadmap = roadmapRepository.findWithAllByPath(path);

        Stage newStage = roadmapService.addNewStage(roadmap,modelMapper.map(stageForm,Stage.class));

        return new ResponseEntity<>(modelMapper.map(newStage, StageDTO.class), HttpStatus.OK);
    }

    @DeleteMapping("/stage/remove")
    public ResponseEntity<String> removeStage(@PathVariable String path, Long id) {

        Roadmap roadmap = roadmapRepository.findByPath(path);

        if(id == null) return new ResponseEntity<>("not found id", HttpStatus.BAD_REQUEST);;
        if(roadmap == null) return new ResponseEntity<>("not found stage", HttpStatus.BAD_REQUEST);

        roadmapService.removeStage(roadmap,id);

        return new ResponseEntity<>("remove stage successful", HttpStatus.OK);
    }

    @GetMapping("/stage/get/{ord}")
    public ResponseEntity<StageDTO> getStage(@PathVariable String path,@PathVariable int ord) {

        log.info("------------------get stage---------------------");

        Roadmap roadmap = roadmapRepository.findByPath(path);
        Stage getStage = roadmap.getStageList().stream().filter(stage -> stage.getOrd() == ord).collect(Collectors.toList()).get(0);

        return new ResponseEntity<>(modelMapper.map(getStage,StageDTO.class), HttpStatus.OK);
    }

    @GetMapping("/stage/getList")
    public ResponseEntity<List<Stage>> getStageList(@PathVariable String path) {

        log.info("------------------get stage list---------------------");

        Roadmap roadmap = roadmapRepository.findWithAllByPath(path);

        roadmap.getStageList().stream().sorted((s1,s2) -> (int) (s2.getOrd() - s1.getOrd()))
                .collect(Collectors.toList())
                .forEach(stage ->{
                    stage.setRoadmap(null);
                    stage.getNodeList().stream().forEach(node -> {
                        node.setStage(null);
                        node.setParent(null);
                        RoadmapController.recursionNode(node);
                    });
                });

        return new ResponseEntity<>(roadmap.getStageList(), HttpStatus.OK);
    }

    @PostMapping("/node/new")
    @Transactional
    public ResponseEntity<NodeDTO> registerNode(@RequestBody String nodeFormStr, Errors errors) throws JsonProcessingException {

        log.info("------------------register new node---------------------");
        log.info(nodeFormStr);

        NodeAddForm nodeForm = objectMapper.readValue(nodeFormStr,NodeAddForm.class);

        log.info(nodeForm);

        Long id = nodeForm.getId();

        if(errors.hasErrors()) {
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
        }

        if(id == null)
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);

        log.info("-----------------------success---------------------------");

        Node node = null;
        Node newNode = null;
        NodeDTO nodeDTO = new NodeDTO();
        Long newId = null;

        if(nodeForm.getParentType().equals("stage")){
            Stage parent = stageRepository.findById(id).orElseThrow();
            newId = nodeService.addNewNode(parent,nodeForm);
            nodeDTO.setStageId(parent.getId());
            node = nodeRepository.findById(newId).orElseThrow();
            parent.getNodeList().add(node);
            stageRepository.save(parent);
        } else {
            Node parent = nodeRepository.findById(id).orElseThrow();
            log.info("parent : " + parent);
            newId = nodeService.addNewNode(parent,nodeForm);
            nodeDTO.setParentId(parent.getId());
            node = nodeRepository.findById(newId).orElseThrow();
            parent.getChilds().add(node);
            nodeRepository.save(parent);
        }

        nodeDTO.setId(node.getId());
        nodeDTO.setNodeType(node.getNodeType());
        nodeDTO.setTitle(node.getTitle());

        return new ResponseEntity<>(nodeDTO, HttpStatus.OK);
    }

    @DeleteMapping("/node/remove")
    public ResponseEntity<String> removeNode(@RequestBody String str) throws JsonProcessingException {

        log.info("----------------------------------------remove node-------------------------");
        Map<String,String> map = objectMapper.readValue(str, HashMap.class);
        Long id = Long.valueOf(map.get("id"));
        String parentType = map.get("parentType");

        log.info("id : " + id);
        log.info("parentType : " + parentType);

        if(id == null)
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);

        if(parentType.equalsIgnoreCase("node")){
            Optional<Node> result = nodeRepository.findById(id);

            if(!result.isPresent())
                return new ResponseEntity<>("존재하지 않는 node입니다.",HttpStatus.BAD_REQUEST);

            Node node = result.get();

            log.info("node : " + node);

            if(node.getChilds().size() > 0){
                return new ResponseEntity<>("node에 자식 노드가 있습니다.",HttpStatus.BAD_REQUEST);
            }

            nodeService.removeNode(node);
        } else {
            Optional<Stage> result = stageRepository.findById(id);

            if(!result.isPresent())
                return new ResponseEntity<>("존재하지 않는 stage 입니다.",HttpStatus.BAD_REQUEST);

            Stage stage = result.get();

            log.info("node : " + stage);

            if(stage.getNodeList().size() > 0) {
                return new ResponseEntity<>("stage에 자식 노드들이 있습니다.",HttpStatus.BAD_REQUEST);
            }

            roadmapService.removeStage(stage.getRoadmap(),stage.getId());
        }


        return new ResponseEntity<>("remove node successful", HttpStatus.OK);
    }

    @PutMapping("/node/modify/type")
    public ResponseEntity<NodeDTO> modifyTypeNode(Long id,String type){

        if(id == null || type == null)
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
        if(!type.equalsIgnoreCase("post") && !type.equalsIgnoreCase("text")
                && !type.equalsIgnoreCase("video"))
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);


        Node node = nodeRepository.findById(id).orElseThrow();

        Node updateNode = nodeService.modifyTypeNode(node,type);

        return new ResponseEntity<>(modelMapper.map(updateNode,NodeDTO.class), HttpStatus.OK);
    }

    @PutMapping({"/node/modify","/stage/modify"})
    public ResponseEntity<NodeDTO> modifyNodeAndStage(@RequestBody String nodeFormStr) throws JsonProcessingException {
        log.info("------------------modify node---------------------");

        log.info("node form str : "+nodeFormStr);

        NodeModalForm nodeForm = objectMapper.readValue(nodeFormStr, NodeModalForm.class);

        log.info(nodeForm);

        Long id = nodeForm.getId();

        if(id == null)
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);

        log.info("-----------------------success---------------------------");

        Node node = null;
        Node newNode = null;
        NodeDTO nodeDTO = new NodeDTO();
        Long newId = null;

        if(nodeForm.getParentType().equals("stage")){
            Stage stage = roadmapService.modifyStage(nodeForm);

            nodeDTO.setId(stage.getId());
            nodeDTO.setTitle(stage.getTitle());
            nodeDTO.setComplete(stage.isComplete());
        } else {
            node = nodeService.modifyNode(nodeForm);

            modelMapper.map(node,nodeDTO);
        }

        return new ResponseEntity<>(nodeDTO, HttpStatus.OK);
    }
}
