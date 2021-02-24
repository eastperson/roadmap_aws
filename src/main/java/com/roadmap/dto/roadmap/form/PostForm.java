package com.roadmap.dto.roadmap.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostForm {

    @NotBlank
    @Length(max = 50)
    private String title;

    @NotBlank @Length(max = 100)
    private String description;

    @NotBlank
    private String content;

}
