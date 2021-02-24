package com.roadmap.dto.roadmap.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RoadmapForm {

    @NotBlank @Length(min = 2, max = 25)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{2,25}$")
    private String path;

    @NotBlank @Length(max = 50)
    private String title;

    @NotBlank @Length(max = 100)
    private String shortDescription;

    @NotBlank
    private String fullDescription;

    private boolean useBanner;

}
