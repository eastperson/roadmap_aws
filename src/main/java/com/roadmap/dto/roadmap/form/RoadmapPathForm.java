package com.roadmap.dto.roadmap.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class RoadmapPathForm {

    @NotBlank @Length(min = 2, max = 25)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{2,25}$")
    private String newPath;

}
