package com.roadmap.service;

import com.roadmap.dto.roadmap.form.NodeAddForm;
import com.roadmap.dto.roadmap.form.NodeModalForm;
import com.roadmap.model.Node;
import com.roadmap.model.NodeType;
import com.roadmap.model.Stage;
import com.roadmap.repository.NodeRepository;
import com.roadmap.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@Log4j2
@RequiredArgsConstructor
public class NodeService {

    private final NodeRepository nodeRepository;
    private final StageRepository stageRepository;
    private final ModelMapper modelMaper;


    public Long addNewNode(Stage stage, NodeAddForm nodeForm){
        nodeForm.setId(null);
        Node newNode = modelMaper.map(nodeForm,Node.class);
        newNode.setStage(stage);
        Node node = nodeRepository.save(newNode);
        return node.getId();
    }

    public Long addNewNode(Node parent, NodeAddForm nodeForm){
        log.info("------------------------add new node");
        log.info("------------------------parent : " + parent);
        log.info("------------------------nodeForm : " + nodeForm);
        nodeForm.setId(null);
        Node newNode = modelMaper.map(nodeForm,Node.class);
        log.info("new node : " + newNode);
        Node node = nodeRepository.save(newNode);
        log.info("node : " + node);
        newNode.setParent(parent);
        return node.getId();
    }

    public void removeNode(Node node) {

        log.info("------------------------------remove node");

        log.info("node stage  : " + node.getStage());
        log.info("node parent  : " + node.getParent());


        if(node.getStage() != null){
            log.info("------------------------------remove stage node");
            Stage stage = node.getStage();
            log.info("------------------------------before stage : " + stage);
            stage.getNodeList().remove(node);
            node.setStage(null);
            log.info("------------------------------after stage " + stage);
        }

        if(node.getParent() != null) {
            log.info("---------------------------------remove node node");
            log.info("---------------------------------parent node : " + node.getParent());
            log.info("---------------------------------parent node childs : " + node.getParent().getChilds());
            node.getParent().getChilds().remove(node);
            log.info("---------------------------------parent node childs : " + node.getParent().getChilds());
            node.setParent(null);
        }

        nodeRepository.delete(node);
    }

    public Node modifyTypeNode(Node node,String type) {
        String nodeType = null;
        if(type.equalsIgnoreCase("text")) nodeType = NodeType.TEXT.toString();
        if(type.equalsIgnoreCase("post"))  nodeType = NodeType.POST.toString();
        if(type.equalsIgnoreCase("video"))  nodeType = NodeType.LINK.toString();
        node.setNodeType(nodeType);
        return nodeRepository.save(node);
    }

    public Node modifyNode(NodeModalForm nodeForm) {
        Node node = nodeRepository.findById(nodeForm.getId()).orElseThrow();
        node.setTitle(nodeForm.getTitle());
        node.setText(nodeForm.getText());
        node.setComplete(nodeForm.isComplete());
        node.setRead(nodeForm.isRead());
        node.setShortDescription(nodeForm.getShortDescription());
        node.setUrl(nodeForm.getUrl());
        return nodeRepository.save(node);
    }
}
