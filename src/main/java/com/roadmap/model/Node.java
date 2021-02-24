package com.roadmap.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;

@Entity @ToString(exclude = {"post","noteList","stage","parent"})
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Node extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Stage stage;

    @OneToOne(cascade = CascadeType.ALL)
    private Node parent;

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
