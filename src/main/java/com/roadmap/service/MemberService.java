package com.roadmap.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.roadmap.config.AppProperties;
import com.roadmap.dto.admin.member.MemberInfoForm;
import com.roadmap.dto.email.EmailMessage;
import com.roadmap.dto.member.MemberDTO;
import com.roadmap.dto.member.form.LocationForm;
import com.roadmap.dto.member.form.NicknameForm;
import com.roadmap.dto.member.form.NotificationForm;
import com.roadmap.dto.member.form.ProfileForm;
import com.roadmap.dto.member.form.SignUpForm;
import com.roadmap.dto.page.PageRequestDTO;
import com.roadmap.dto.page.PageResultDTO;
import com.roadmap.model.Location;
import com.roadmap.model.Member;
import com.roadmap.model.MemberRole;
import com.roadmap.model.QMember;
import com.roadmap.model.Tag;
import com.roadmap.model.UserMember;
import com.roadmap.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;
    private final AppProperties appProperties;


    public void login(Member member) {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    new UserMember(member),
                    member.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(token);

    }

    public Member saveNewMember(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Member member = modelMapper.map(signUpForm,Member.class);
        member.setJoinedAt(LocalDateTime.now());
        login(member);
        sendSignUpConfirmEmail(member);
        member.addMemberRole(MemberRole.USER);
        return memberRepository.save(member);
    }

    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(emailOrNickname);
        if(member == null) {
            member = memberRepository.findByNickname(emailOrNickname);
        }
        if(member == null){
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserMember(member);
    }

    public void sendSignUpConfirmEmail(Member member) {
        member.generateEmailCheckToken();
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + member.getEmailCheckToken() +
                "&email=" + member.getEmail());
        context.setVariable("nickname", member.getNickname());
        context.setVariable("linkName", "로드맵 이메일 인증하기");
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("host",appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(member.getEmail())
                .subject("로드맵, 로그인 링크")
                .message(message)
                .build();
        //TODO 나중에 추가 emailService.sendEmail(emailMessage);
        log.info("email send signup confirm Email link : "+context.getVariable("link"));
    }

    public void completeSignUp(Member member) {
        member.completeSignUp();
        login(member);
    }

    public void updateProfile(Member member, ProfileForm profileForm) {
        modelMapper.map(profileForm,member);
        memberRepository.save(member);
    }

    public void updatePassword(Member member, String password) {
        member.setPassword(passwordEncoder.encode(password));
        memberRepository.save(member);
    }

    public void updateNotification(Member member, NotificationForm notificationForm) {
        modelMapper.map(notificationForm,member);
        memberRepository.save(member);
    }

    public Set<Tag> getTags(Member member) {
        Optional<Member> byId = memberRepository.findById(member.getId());
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Member member, Tag tag){
        Optional<Member> byId = memberRepository.findById(member.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }
    public void addTag(Member member, Tag tag){
        Optional<Member> byId = memberRepository.findById(member.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
    }

    public void updateLocation(Member member, LocationForm locationForm){
        Member withLoc = memberRepository.findWithLocByNickname(member.getNickname());
        withLoc.setLocation(modelMapper.map(locationForm,Location.class));
        memberRepository.save(withLoc);
    }

    public void sendLoginLink(Member member) {
        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + member.getEmailCheckToken() +
                "&email=" + member.getEmail());
        context.setVariable("nickname", member.getNickname());
        context.setVariable("linkName","이메일 로그인하기");
        context.setVariable("message","로드맵 로그인 하려면 링크를 클릭하세요.");
        context.setVariable("host",appProperties.getHost());

        String message = templateEngine.process("mail/simple-link",context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(member.getEmail())
                .subject("로드맵, 로그인 링크")
                .message(message)
                .build();

        //TODO emailService.sendEmail(emailMessage);
        log.info("link : "+context.getVariable("link"));
    }

    public void updateNickname(Member member,NicknameForm nicknameForm) {
        modelMapper.map(nicknameForm,member);
        memberRepository.save(member);
    }

    public PageResultDTO<MemberDTO,Member> getList(PageRequestDTO requestDTO) {
        Pageable pageable = requestDTO.getPageable(Sort.by("id").descending());

        BooleanBuilder booleanBuilder = getSearch(requestDTO);

        Page<Member> result = memberRepository.findAll(booleanBuilder,pageable);

        Function<Member, MemberDTO> fn = (entity->entity.entityToDto(modelMapper));
        return new PageResultDTO<>(result,fn);
    }

    private BooleanBuilder getSearch(PageRequestDTO requestDTO) {
        String type = requestDTO.getType();
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QMember qMember = QMember.member;
        String keyword = requestDTO.getKeyword();
        BooleanExpression expression = qMember.id.gt(0L);

        booleanBuilder.and(expression);
//        if(type == null || type.trim().length() == 0) return booleanBuilder;

        if(keyword == null || keyword.trim().length() == 0) return booleanBuilder;

        BooleanBuilder conditionBuilder = new BooleanBuilder();

        conditionBuilder.or(qMember.email.contains(keyword));

        conditionBuilder.or(qMember.nickname.contains(keyword));

        conditionBuilder.or(qMember.occupation.contains(keyword));

        booleanBuilder.and(conditionBuilder);

        return booleanBuilder;
    }

    public void updateInfo(MemberInfoForm memberInfoForm,Member member) {
        modelMapper.map(memberInfoForm,member);
        Member upMember = memberRepository.save(member);

        log.info("member update : "+upMember);
    }

    public PageResultDTO<MemberDTO,Member> getAdminList(PageRequestDTO requestDTO) {
        Pageable pageable = requestDTO.getPageable(Sort.by("id").descending());

        BooleanBuilder booleanBuilder = getSearch(requestDTO);

        QMember qMember = QMember.member;

        booleanBuilder.and(qMember.roleSet.contains(MemberRole.ADMIN));

        Page<Member> result = memberRepository.findAll(booleanBuilder,pageable);

        Function<Member, MemberDTO> fn = (entity->entity.entityToDto(modelMapper));
        return new PageResultDTO<>(result,fn);
    }


}
