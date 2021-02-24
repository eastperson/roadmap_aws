package com.roadmap.validation;

import com.roadmap.dto.member.form.NicknameForm;
import com.roadmap.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
@Log4j2
public class NicknameFormValidator implements Validator {

    private final MemberRepository memberRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return NicknameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) target;

        if (memberRepository.existsByNickname(nicknameForm.getNickname())) {
            errors.rejectValue("nickname","invalid.nickname",new Object[]{nicknameForm.getNickname()},"이미 사용중인 닉네임입니다.");
        }
    }
}
