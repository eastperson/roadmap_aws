package com.roadmap.service;

import com.roadmap.model.Event;
import com.roadmap.model.Member;
import com.roadmap.model.Roadmap;
import com.roadmap.repository.EnrollmentRepository;
import com.roadmap.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentRepository enrollmentRepository;


    public Event createEvent(Roadmap roadmap, Event event, Member member) {
        event.setCreatedBy(member);
        event.setRoadmap(roadmap);
        event.setCreatedDateTime(LocalDateTime.now());
        return eventRepository.save(event);
    }
}
