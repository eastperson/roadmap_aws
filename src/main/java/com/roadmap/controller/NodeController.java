package com.roadmap.controller;

import com.roadmap.model.Node;
import com.roadmap.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/roadmap/{path}")
@Log4j2
public class NodeController {

    private final NodeRepository nodeRepository;

    @GetMapping("/node/{id}")
    public String viewNode(@PathVariable Long id, Model model){

        Node node = nodeRepository.findById(id).orElseThrow();

        model.addAttribute(node);

        return "/node/view";
    }
}