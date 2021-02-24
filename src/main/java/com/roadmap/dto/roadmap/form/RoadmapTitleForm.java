package com.roadmap.dto.roadmap.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class RoadmapTitleForm {

    @NotBlank
    @Length(max = 50)
    private String newTitle;
}
