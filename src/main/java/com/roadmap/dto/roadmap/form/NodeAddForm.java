package com.roadmap.dto.roadmap.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class NodeAddForm {

    private Long id;

    private Long parentId;

    @NotBlank
    private String parentType;

    @NotBlank @Length(max = 25)
    private String title;

    @NotBlank
    private String nodeType;



}
