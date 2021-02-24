package com.roadmap.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.roadmap.dto.member.AuthMemberDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NamedEntityGraph(name = "Roadmap.withMembers", attributeNodes = {
        @NamedAttributeNode("members")
})
@NamedEntityGraph(name = "Roadmap.withAll", attributeNodes = {
        @NamedAttributeNode("members"),
        @NamedAttributeNode("stageList"),
        @NamedAttributeNode("likeMembers")
})
@NamedEntityGraph(name = "Roadmap.withOwner", attributeNodes = {
        @NamedAttributeNode("owner")
})
@Entity @ToString(exclude = {"members","tags","likeMembers","stageList","owner","image","fullDescription"})
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public class Roadmap extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false)
    private String path;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    private boolean published;
    private LocalDateTime publishedDateTime;

    private boolean closed;
    private LocalDateTime closedDateTime;

    private boolean recruiting;
    private LocalDateTime recruitingUpdatedDateTime;

    @Column(nullable = false)
    private String title;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    private String shortDescription;
    private boolean useBanner;
    private Double progress;
    private boolean complete;
    private LocalDateTime milestone;

    @OneToOne(fetch = FetchType.EAGER)
    private Member owner;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Member> members = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY,mappedBy = "likeRoadmaps")
    private Set<Member> likeMembers = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Stage> stageList = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Review> reviews;

    public boolean isJoinable(UserMember userMember){
        Member member = userMember.getMember();
        return this.isPublished() && this.isRecruiting() && !this.members.contains(member);
    }

    public boolean isJoinable(AuthMemberDTO authMemberDTO){
        Member member = authMemberDTO.getMember();
        return this.isPublished() && this.isRecruiting() && !this.members.contains(member);
    }

    public boolean isMember(UserMember userMember) {
        return this.members.contains(userMember.getMember());
    }

    public boolean isMember(AuthMemberDTO authMemberDTO) {
        return this.members.contains(authMemberDTO.getMember());
    }

    public boolean isOwner(UserMember userMember) {
        return this.owner.equals(userMember.getMember());
    }

    public boolean isOwner(AuthMemberDTO authMemberDTO) {
        return this.owner.equals(authMemberDTO.getMember());
    }

    public void addMember(Member member) {
        this.getMembers().add(member);
    }

    public String getImage(){
        return image != null ? image : "/images/logo.png";
    }

    public void publish(){
        if(!this.closed && !this.published) {
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("로드맵를 공개할 수 없는 상태입니다. 로드맵를 이미 공개했거나 종료했습니다.");
        }
    }


    public void close(){
        if(this.published && !this.closed){
            closed = true;
            this.closedDateTime = LocalDateTime.now();
        } else {
            throw  new RuntimeException("로드맵을 종료할 수 없습니다. 로드맵을 공개하지 않았거나 이미 종료된 로드맵입니다.");
        }
    }

    public void startRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 로드맵를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public void stopRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 로드맵를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingUpdatedDateTime == null || this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public String getEncodedPath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }

    public void removeMemeber(Member member) {
        this.getMembers().remove(member);
    }

    public void initTail() {
        this.getStageList().stream().forEach(stage -> stage.setTail(false));
    }

    public boolean isRemovable() {
        return !this.published; // TODO 모임을 했던 스터디는 삭제할 수 없다.
    }
}
