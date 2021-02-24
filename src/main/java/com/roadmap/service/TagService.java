package com.roadmap.service;

import com.roadmap.model.Tag;
import com.roadmap.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final TagRepository tagRepository;
    private final ModelMapper modelMapper;

    public Tag findOrCreateNew(String title) {

        Tag tag = tagRepository.findByTitle(title);

        if(tag == null){
            return tagRepository.save(Tag.builder().title(title).build());
        }
        return tag;
    }
}
