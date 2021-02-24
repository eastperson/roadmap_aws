package com.roadmap.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NamedEntityGraph(
        name = "Event.withEnrollments",
        attributeNodes = @NamedAttributeNode("enrollments")
)
@Entity @Getter @Setter
@EqualsAndHashCode(of = "id") @NoArgsConstructor
public class Event {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    private Roadmap roadmap;

    @ManyToOne
    private Member createdBy;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = true)
    private Integer limitOfEnrollments;

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    public boolean isEnrollableFor(UserMember userMember) {
        // 열려있고, 참여하지 않았고, 이미 등록하지 않았을 경우
        return isNotClosed() && !this.isAttended(userMember) && !isAlreadyEnrolled(userMember);
    }

    public boolean isDisenrollableFor(UserMember userMember) {
        // 열려있지만, 이미 등록한 경우
        return isNotClosed() && isAlreadyEnrolled(userMember);
    }

    public boolean isNotClosed(){
        // 등록날짜가 닫힌 날짜가 오늘 이후인지
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    // 참여가 가능한지
    public boolean isAttended(UserMember userMember) {
        Member member = userMember.getMember();
        for(Enrollment e : this.enrollments) {
            // 등록한 멤버이며, 참여가 가능한 상태인지
            if(e.getMember().equals(member) && e.isAttended()){
                return true;
            }
        }
        return false;
    }

    // 이미 등록했는지
    public boolean isAlreadyEnrolled(UserMember userMember) {
        Member member = userMember.getMember();
        for(Enrollment e : this.enrollments){
            // 등록한 멤버인지
            if(e.getMember().equals(member)) {
                return true;
            }
        }
        return false;
    }

    public long getNumberOfAcceptedEnrollments(){
        // 승낙한 등록이 많은지
        return this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public int numberOfRemainSpots(){
        // 등록 제한 인원에서 이미 승낙된 등록 숫자를 뺀 것.
        return this.limitOfEnrollments - (int) this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public boolean canAccept(Enrollment enrollment){
        // 승낙 가능 모임이며, 모임이 해당 이벤트에 속해있고, 참여하지 않고, 승낙하지 않은 등록은  승낙 상태로 바꿀 수 있다.
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && !enrollment.isAccepted();
    }

    public boolean canReject(Enrollment enrollment) {
        // 승낙 가능 등록이며, 등록이 등록 명단에 포함되어 있고, 참여는 하지 않았지만 수락은 한 상태는 거절할 수 있다.
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && enrollment.isAccepted();
    }

    public void addEnrollment(Enrollment enrollment) {
        // 등록을 추가한다.
        this.enrollments.add(enrollment);
        enrollment.setEvent(this);
    }

    public void removeEnrollment(Enrollment enrollment) {
        // 등록을 제거한다.
        this.enrollments.remove(enrollment);
        enrollment.setEvent(null);
    }

    public boolean isAbleToAcceptWaitingEnrollment(){
        // 선착순이고 제한인원보다 적으면 등록 대기를 받을 수 있다.
        return this.eventType == EventType.FCFS && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments();
    }

    public void acceptNextWaitingEnrollment(){
        if(this.isAbleToAcceptWaitingEnrollment()) {
            Enrollment enrollmentToAccept = this.getTheFirstWaitingEnrollment();
            if(enrollmentToAccept != null) {
                enrollmentToAccept.setAccepted(true);
            }
        }
    }

    private Enrollment getTheFirstWaitingEnrollment() {
        for(Enrollment e : this.enrollments) {
            if(!e.isAccepted()){
                return e;
            }
        }
        return null;
    }

    public void acceptWaitingList() {
        if(this.isAbleToAcceptWaitingEnrollment()){
            var waitingList = getWaitingList();
            int numberToAccept = (int) Math.min(this.limitOfEnrollments - this.getNumberOfAcceptedEnrollments(), waitingList.size());
            waitingList.subList(0,numberToAccept).forEach(e -> e.setAccepted(true));
        }
    }

    private List<Enrollment> getWaitingList() {
        return this.enrollments.stream().filter(enrollment -> !enrollment.isAccepted()).collect(Collectors.toList());
    }

    public void accept(Enrollment enrollment) {
        if(this.eventType == EventType.CONFIRMATIVE && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments()){
            enrollment.setAccepted(true);
        }
    }

    public void reject(Enrollment enrollment) {
        if(this.eventType == EventType.CONFIRMATIVE) {
            enrollment.setAccepted(false);
        }
    }

}
