package com.roadmap.handler;

import com.roadmap.dto.member.AuthMemberDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Log4j2
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private PasswordEncoder passwordEncoder;

    public LoginSuccessHandler(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        AuthMemberDTO authMember = (AuthMemberDTO) authentication.getPrincipal();

        boolean fromSocial = authMember.isFromSocial();

        log.info("auth member : "+authMember);
        log.info("password encoder in login success handler : " + this.passwordEncoder);

        boolean passwordResult = this.passwordEncoder.matches("1111","{noop}"+authMember.getPassword());
        Set<String> roleNames = new HashSet<>();
        authMember.getAuthorities().forEach(grantedAuthority -> {
                roleNames.add(grantedAuthority.getAuthority());
        });

        log.info("role names : " + roleNames);

        String redirectUrl = "/";

        if (roleNames.contains("ROLE_ADMIN")){
            redirectUrl = "/admin";
        }

        if(fromSocial && passwordResult) {
            redirectUrl = "/settings/password";
        }

        redirectStrategy.sendRedirect(request,response,redirectUrl);
    }
}
