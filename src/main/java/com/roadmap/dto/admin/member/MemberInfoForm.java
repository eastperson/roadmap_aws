package com.roadmap.dto.admin.member;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class MemberInfoForm {

    @Length(max = 35)
    private String bio;

    @Length(max = 50)
    private String url;

    @Length(max = 50)
    private String occupation;

}
