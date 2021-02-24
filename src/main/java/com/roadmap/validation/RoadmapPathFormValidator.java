package com.roadmap.validation;

import com.roadmap.dto.roadmap.form.RoadmapPathForm;
import com.roadmap.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class RoadmapPathFormValidator implements Validator {

    private final RoadmapRepository roadmapRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return RoadmapPathForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        RoadmapPathForm roadmapPathForm = (RoadmapPathForm) target;

        if(roadmapRepository.existsByPath(roadmapPathForm.getNewPath())){
            errors.rejectValue("newPath","invalid.path",new Object[]{roadmapPathForm.getNewPath()},"이미 사용중인 경로입니다.");;
        }

    }
}
