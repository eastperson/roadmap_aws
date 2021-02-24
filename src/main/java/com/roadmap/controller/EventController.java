package com.roadmap.controller;

import com.roadmap.dto.member.CurrentUser;
import com.roadmap.dto.roadmap.form.EventForm;
import com.roadmap.model.Event;
import com.roadmap.model.Member;
import com.roadmap.model.Roadmap;
import com.roadmap.repository.EnrollmentRepository;
import com.roadmap.repository.EventRepository;
import com.roadmap.repository.RoadmapRepository;
import com.roadmap.service.EventService;
import com.roadmap.service.RoadmapService;
import com.roadmap.validation.EventValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/roadmap/{path}")
@Log4j2
public class EventController {

    private final RoadmapService roadmapService;
    private final RoadmapRepository roadmapRepository;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EventValidator eventValidator;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder) {webDataBinder.addValidators(eventValidator);}

    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Member member, @PathVariable String path, Model model) {
        Roadmap roadmap = roadmapService.getRoadmapToUpdateStatus(member,path);
        model.addAttribute(roadmap);
        model.addAttribute(roadmap);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventForm_input(@CurrentUser Member member, @PathVariable String path, @Valid EventForm eventForm, Errors errors, Model model){
        Roadmap roadmap = roadmapService.getRoadmap(path);
        if(errors.hasErrors()){
            model.addAttribute(member);
            model.addAttribute(roadmap);
            return "event/form";
        }

        Event event = eventService.createEvent(roadmap,modelMapper.map(eventForm,Event.class),member);
        return "redirect:/roadmap/" + roadmap.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentUser Member member, @PathVariable String path, @PathVariable Long id, Model model){
        model.addAttribute(member);
        model.addAttribute(eventRepository.findById(id).orElseThrow());
        model.addAttribute(roadmapRepository.findRoadmapWithOwnerByPath(path));
        return "event/view";
    }

    @GetMapping("/events")
    public String viewRoadmapEvents(@CurrentUser Member member, @PathVariable String path, Model model) {
        Roadmap roadmap = roadmapService.getRoadmap(path);
        model.addAttribute(member);
        model.addAttribute(roadmap);

        List<Event> events = eventRepository.findByRoadmapOrderByStartDateTime(roadmap);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        events.forEach(e -> {
            if(e.getEndDateTime().isBefore(LocalDateTime.now())){
                oldEvents.add(e);
            } else {
                newEvents.add(e);
            }
        });

        model.addAttribute("newEvents",newEvents);
        model.addAttribute("oldEvents",oldEvents);
        return "roadmap/events";
    }

}
