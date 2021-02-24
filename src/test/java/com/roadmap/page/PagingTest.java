package com.roadmap.page;

import com.roadmap.dto.member.MemberDTO;
import com.roadmap.dto.page.PageRequestDTO;
import com.roadmap.dto.page.PageResultDTO;
import com.roadmap.service.MemberService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@Log4j2 @TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PagingTest  {

    @Autowired PasswordEncoder passwordEncoder;

    @Autowired MemberService memberService;

//    @BeforeAll
//    void createMemberList(){
//        for(int i = 0; i < 200; i++) {
//            SignUpForm signUpForm = new SignUpForm();
//            signUpForm.setNickname("nickname" + i);
//            signUpForm.setEmail(i+"@email.com");
//            signUpForm.setPassword(passwordEncoder.encode("11111111"));
//            memberService.saveNewMember(signUpForm);
//        }
//    }

    @Test
    @DisplayName("페이징 처리 테스트 - 멤버")
    void pageigng_member(){

        PageResultDTO pageResultDTO = memberService.getList(PageRequestDTO.builder().size(10).page(1).build());
        log.info(pageResultDTO);
        assertTrue(pageResultDTO.getSize() == 10);
        assertTrue(pageResultDTO.getStart() == 1);
        assertFalse(pageResultDTO.isPrev());

    }

    @Test
    @DisplayName("검색 처리 테스트 - 멤버")
    void pageigng_search_member(){

        PageResultDTO pageResultDTO = memberService.getList(PageRequestDTO.builder()
                .size(10).page(1).type("e").keyword("3").build());
        log.info(pageResultDTO);
        assertTrue(pageResultDTO.getStart() == 1);
        assertFalse(pageResultDTO.isPrev());
        pageResultDTO.getDtoList().stream().forEach(dto -> {
            MemberDTO member = (MemberDTO) dto;
            assertTrue(member.getEmail().contains("3") || member.getNickname().contains("3") || member.getOccupation().contains("3"));
        });

    }


}
