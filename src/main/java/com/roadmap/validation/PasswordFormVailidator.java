package com.roadmap.validation;

import com.roadmap.dto.member.form.PasswordForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PasswordFormVailidator implements Validator {


    @Override
    public boolean supports(Class<?> clazz) {
        return PasswordForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) target;
        if(!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())){
            errors.rejectValue("newPasswordConfirm","invalid.passwordConfirm",new Object[]{passwordForm.getNewPassword()},"비밀번호가 일치하지 않습니다.");
        }

    }
}
