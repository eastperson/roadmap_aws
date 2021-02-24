package com.roadmap.member;

import com.roadmap.model.Member;
import com.roadmap.repository.MemberRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Log4j2
@Transactional
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void memberWithRoadmapsByNickname(){

       Member member = memberRepository.findWithRoadmapByNickname("kjuioq@nate.com");
       if(member != null){
           assertTrue(member.getNickname().equals("kjuioq@nate.com"));
           assertNotNull(member.getRoadmaps());
           log.info(member);
           log.info(member.getRoadmaps());
       }
    }
}
