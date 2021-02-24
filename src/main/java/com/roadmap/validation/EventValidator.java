package com.roadmap.validation;

import com.roadmap.dto.roadmap.form.EventForm;
import com.roadmap.model.Event;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return EventForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm) target;

        if(isNotValidEndEnrollmentDateTime(eventForm)){
            errors.rejectValue("endEnrollmentDateTime","wrong.datetime","모집 접수 종료 일시를 정확히 입력하세요.");
        }

        if(isNotValidEndDateTime(eventForm)){
            errors.rejectValue("endEnrollmentDateTime","wrong.datetime","모임 접수 종료 일시를 정확히 입력하세요.");
        }

        if(isNotValidStartDateTime(eventForm)){
            errors.rejectValue("startDateTime","wrong,datetime","모임 시작 일시를 정확히 입력하세요.");
        }

    }
    private boolean isNotValidStartDateTime(EventForm eventForm) {
        // 이벤트 폼의 시작시간은 종료시각보다 등록 종료시간보다 이전이면 안된다.
        return eventForm.getStartDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }

    private boolean isNotValidEndEnrollmentDateTime(EventForm eventForm) {
        // 이벤트 폼의 등록 종료시간은 지금보다 이전이면 안된다.
        return eventForm.getEndEnrollmentDateTime().isBefore(LocalDateTime.now());
    }

    private boolean isNotValidEndDateTime(EventForm eventForm) {
        // 이벤트 폼의 끝나는 시간은 이벤트 폼의 시작시간보다 이전이면 안된다.
        // 이벤트 폼의 끝나느 시간은 등록마감시간보다 이전이면 안된다.
        return eventForm.getEndDateTime().isBefore(eventForm.getStartDateTime()) || eventForm.getEndDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }

    public void validateUpdateForm(EventForm eventForm, Event event, Errors errors) {
        // 이벤트 폼의 제한 등록인원은 이벤트의 등록인원보다 작으면 안된다.
        if(eventForm.getLimitOfEnrollments() < event.getNumberOfAcceptedEnrollments()){
            errors.rejectValue("limitOfEnrollments","wrong.value","확인된 참가 신청보다 모집 인원 수가 커야 합니다.");
        }
    }
}
