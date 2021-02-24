package com.roadmap.dto.roadmap;

import com.roadmap.model.Node;
import com.roadmap.model.NodeType;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Data
public class NodeDTO {

    private Long id;

    private Long stageId;

    private Long parentId;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Node> childs = new ArrayList<>();

    private String nodeType = NodeType.TEXT.toString();

    private String title;

    private String shortDescription;

    private String url;

    private boolean complete;

    private boolean read;

    private String text;
}
