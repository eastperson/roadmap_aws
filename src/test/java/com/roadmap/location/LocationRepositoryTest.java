package com.roadmap.location;

import com.roadmap.member.WithMember;
import com.roadmap.model.Location;
import com.roadmap.model.Member;
import com.roadmap.repository.LocationRepository;
import com.roadmap.repository.MemberRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Log4j2
public class LocationRepositoryTest {

    @Autowired LocationRepository locationRepository;
    @Autowired MemberRepository memberRepository;

    @DisplayName("Member-Location 1:1 단방향 조인 테스트")
    @WithMember("epepep")
    @Test
    @Transactional
    void locationJoin(){
        Location location = Location.builder()
                .addr("주소 입력 테스트1")
                .lat(3.0003)
                .lng(5.0005)
                .build();
        Member member = memberRepository.findByNickname("epepep");
        assertNull(member.getLocation());

        Location result = locationRepository.save(location);
        member.setLocation(result);
        assertNotNull(result);
        log.info("member : " + member);
        log.info("location : " + location);

        Location findById = locationRepository.findById(result.getId()).orElseThrow();
        assertNotNull(findById);
        log.info("findById location : " + findById);
        Member memberWithLoc = memberRepository.findWithLocByNickname("epepep");
        assertNotNull(memberWithLoc);
        assertNotNull(memberWithLoc.getLocation());
    }

    @DisplayName("Member-Location 1:1 단방향 조인 테스트 - location이 없을 경우")
    @WithMember("epepep")
    @Test
    @Transactional
    void locationJoin_outer(){
        Member member = memberRepository.findWithLocByNickname("epepep");
        assertNotNull(member);
        assertNull(member.getLocation());
    }


}
