package com.roadmap.dto.roadmap.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class StageForm {

    @Length(max = 25)
    @NotBlank
    private String title;

}
