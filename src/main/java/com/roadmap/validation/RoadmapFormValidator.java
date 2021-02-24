package com.roadmap.validation;

import com.roadmap.dto.roadmap.form.RoadmapForm;
import com.roadmap.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class RoadmapFormValidator implements Validator {

    private final RoadmapRepository roadmapRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return RoadmapForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        RoadmapForm roadmapForm = (RoadmapForm) target;

        if(roadmapRepository.existsByPath(roadmapForm.getPath())){
            errors.rejectValue("path","invalid.path",new Object[]{roadmapForm.getPath()},"이미 사용중인 경로입니다.");;
        }

    }
}
