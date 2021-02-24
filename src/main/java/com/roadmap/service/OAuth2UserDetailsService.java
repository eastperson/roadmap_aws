package com.roadmap.service;

import com.roadmap.dto.member.AuthMemberDTO;
import com.roadmap.model.Member;
import com.roadmap.model.MemberRole;
import com.roadmap.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class OAuth2UserDetailsService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("---------------------------------------");
        log.info("userRequest : " + userRequest); // org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest 객체

        String clientName = userRequest.getClientRegistration().getClientName();

        log.info("clientName : " + clientName);
        log.info(userRequest.getAdditionalParameters());

        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("=======================================");
        oAuth2User.getAttributes().forEach((k,v) -> {
            log.info(k + ":" + v);
        });

        String email = null;
        String nickname = null;
        String url = null;

        if(clientName.equals("Google")){
            email = oAuth2User.getAttribute("email");
        }

        // naver는 attribute가 response에 담겨있다.
        if(clientName.equals("Naver")){
            Map<String,Object> response = (Map<String,Object>) oAuth2User.getAttributes().get("response");
            email = (String) response.get("email");
        }

        if(clientName.equals("kakao")) {
            Map<String,Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
            log.info("kakao_account : " + response);
            email = (String) response.get("email");
        }

        if(clientName.equals("GitHub")){
            nickname = (String) oAuth2User.getAttributes().get("login");
            url = (String) oAuth2User.getAttributes().get("html_url");
        }

        log.info("EMAIL : " + email);
        Member member;
        if(!clientName.equals("GitHub"))  member = saveMemberByEmail(email);
        else member = saveMemberByNickname(nickname);

        AuthMemberDTO authMember = new AuthMemberDTO(member.getNickname(),member.getPassword(),true,member.getRoleSet().stream().map(
                role -> new SimpleGrantedAuthority("ROLE_" + role.name())
        ).collect(Collectors.toList()), oAuth2User.getAttributes());

        authMember.setMember(member);

        return authMember;

    }

    private Member saveMemberByEmail(String email) {

        // 기존에 동일한 이메일로 가입한 회원이 있는 경우에는 그대로 조회만
        Member member = memberRepository.findWithRoleByEmail(email);

        if(member != null){
            return member;
        }

        // 없다면 회원 추가 패스워드는 1111 닉네임은 메일주소
        Member newMember = new Member();
        newMember.setEmail(email);
        newMember.setNickname(email);
        newMember.setPassword("1111");
        newMember.setFromSocial(true);
        newMember.addMemberRole(MemberRole.USER);
        newMember.setJoinedAt(LocalDateTime.now());
        memberService.sendSignUpConfirmEmail(newMember);

        return memberRepository.save(newMember);
    }

    private Member saveMemberByNickname(String nickname) {

        // 기존에 동일한 이메일로 가입한 회원이 있는 경우에는 그대로 조회만
        Member member = memberRepository.findWithRoleByNickname(nickname);

        if(member != null){
            return member;
        }

        // 없다면 회원 추가 패스워드는 1111 닉네임은 메일주소
        Member newMember = new Member();
        newMember.setEmail(nickname+"@email.com");
        newMember.setNickname(nickname);
        newMember.setPassword("1111");
        newMember.setFromSocial(true);
        newMember.addMemberRole(MemberRole.USER);
        newMember.setJoinedAt(LocalDateTime.now());

        return memberRepository.save(newMember);
    }
}
