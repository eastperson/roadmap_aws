package com.roadmap.service;

import com.roadmap.dto.roadmap.form.NodeModalForm;
import com.roadmap.dto.roadmap.form.RoadmapDescriptionForm;
import com.roadmap.dto.roadmap.form.RoadmapForm;
import com.roadmap.dto.roadmap.form.RoadmapPathForm;
import com.roadmap.dto.roadmap.form.RoadmapTitleForm;
import com.roadmap.model.Member;
import com.roadmap.model.Roadmap;
import com.roadmap.model.Stage;
import com.roadmap.model.Tag;
import com.roadmap.repository.MemberRepository;
import com.roadmap.repository.RoadmapRepository;
import com.roadmap.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;
    private final StageRepository stageRepository;

    public Roadmap registerForm(Member member, RoadmapForm roadmapForm) {

        Roadmap roadmap = modelMapper.map(roadmapForm, Roadmap.class);
        Member updateMember = memberRepository.findWithRoadmapByNickname(member.getNickname());
        roadmap.setOwner(member);
        roadmap.getMembers().add(member);
        updateMember.getRoadmaps().add(roadmap);
        memberRepository.save(updateMember);
        return roadmapRepository.save(roadmap);

    }

    public void removeMember(Roadmap roadmap, Member member) {
        roadmap.removeMemeber(member);
    }

    public void addMember(Roadmap roadmap, Member member) {
        roadmap.addMember(member);
    }

    public Roadmap getRoadmapToUpdate(Member member, String path) {
        Roadmap roadmap = this.getRoadmap(path);
        checkIfOwner(member,roadmap);
        return roadmap;
    }

    public Roadmap getRoadmap(String path) {
        Roadmap roadmap = roadmapRepository.findByPath(path);
        if(roadmap == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        return roadmap;
    }

    private void checkIfOwner(Member member, Roadmap roadmap) {
        if(!roadmap.getOwner().equals(member)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    public void updateRoadmapDescription(Roadmap roadmap, RoadmapDescriptionForm roadmapDescriptionForm) {
        modelMapper.map(roadmapDescriptionForm,roadmap);
    }

    public Stage addNewStage(Roadmap roadmap, Stage stage) {
        stage.setRoadmap(roadmap);
        int size = roadmap.getStageList().size();
        stage.setOrd(size + 1);
        if(size == 0) stage.setHead(true);
        roadmap.initTail();
        stage.setTail(true);
        Stage newStage = stageRepository.save(stage);

        roadmap.getStageList().add(newStage);
        return newStage;
    }

    public void removeStage(Roadmap roadmap, Long id) {
        Optional<Stage> result = stageRepository.findById(id);
        if(result.isPresent()){
            Stage stage = result.get();
            roadmap.getStageList().remove(stage);
            roadmap.getStageList().stream().forEach(s -> {
                if(s.getOrd() > stage.getOrd()) s.setOrd(s.getOrd()-1);
            });
            int size = roadmap.getStageList().size();
            if(size > 1) roadmap.getStageList().get(size-1).setTail(true);
            stageRepository.delete(stage);
        }
    }

    public Stage modifyStage(NodeModalForm nodeForm) {
        Stage stage = stageRepository.findById(nodeForm.getId()).orElseThrow();
        stage.setTitle(nodeForm.getTitle());
        stage.setComplete(nodeForm.isComplete());
        return stageRepository.save(stage);
    }

    public void updateRoadmapImage(Roadmap roadmap, String image) {
        roadmap.setImage(image);
    }

    public void enableRoadmapBanner(Roadmap roadmap) {
        roadmap.setUseBanner(true);
    }

    public void disableRoadmapBanner(Roadmap roadmap) {
        roadmap.setUseBanner(false);
    }

    public void addTag(Roadmap roadmap, Tag tag) {
        roadmap.getTags().add(tag);
    }

    public void removeTag(Roadmap roadmap, Tag tag) {
        roadmap.getTags().remove(tag);
    }

    public void publish(Roadmap roadmap) {
        roadmap.publish();
    }

    public void close(Roadmap roadmap) {
        roadmap.close();
    }

    public void startRecruit(Roadmap roadmap) {
        roadmap.startRecruit();
    }

    public void stopRecruit(Roadmap roadmap) {
        roadmap.stopRecruit();
    }

    public void updateRoadmapPath(Roadmap roadmap, RoadmapPathForm roadmapPathForm) {
        roadmap.setPath(roadmapPathForm.getNewPath());
    }

    public void updateRoadmapTitle(Roadmap roadmap, RoadmapTitleForm roadmapTitleForm) {
        roadmap.setTitle(roadmapTitleForm.getNewTitle());
    }

    public void remove(Roadmap roadmap) {
        if(roadmap.isRemovable()){
            roadmapRepository.delete(roadmap);
        } else{
            throw new IllegalArgumentException("로드맵을 삭제할 수 없습니다. 비공개로 전환한 뒤 삭제해 주세요.");
        }
    }

    public Roadmap getRoadmapToUpdateStatus(Member member, String path) {
        Roadmap roadmap = roadmapRepository.findRoadmapWithOwnerByPath(path);
        checkIfExistingRoadmap(path,roadmap);
        checkIfOwner(member,roadmap);
        return roadmap;
    }

    private void checkIfExistingRoadmap(String path, Roadmap roadmap) {
        if(roadmap == null){
            throw new IllegalArgumentException(path +"에 해당하는 로드맵이 없습니다.");
        }
    }
}
