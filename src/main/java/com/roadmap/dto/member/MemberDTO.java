package com.roadmap.dto.member;

import com.roadmap.model.Location;
import com.roadmap.model.Member;
import com.roadmap.model.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class MemberDTO {

    private Long id;
    private String bio;
    private String email;
    private String nickname;
    private String password;
    private String emailCheckToken;
    private LocalDateTime emailCheckTokenGeneratedAt;
    private boolean emailVerified;
    private LocalDateTime joinedAt;
    private String occupation;
    private String profileImage;
    private String url;
    private boolean roadmapCreatedByEmail;
    private boolean roadmapCreatedByWeb ;
    private boolean roadmapEnrollmentResultByEmail;
    private boolean roadmapEnrollmentResultByWeb;
    private boolean roadmapUpdatedByEmail;
    private boolean roadmapUpdatedByWeb;
    private Set<Tag> tags;
    private boolean fromSocial;
    private Location location;

    public Member dtoToEntity(ModelMapper modelMapper) {
        return modelMapper.map(this,Member.class);
    }

}
