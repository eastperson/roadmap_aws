package com.roadmap.config;

import com.roadmap.filter.ApiCheckFilter;
import com.roadmap.filter.ApiLoginFilter;
import com.roadmap.handler.ApiLoginFailHandler;
import com.roadmap.handler.LoginSuccessHandler;
import com.roadmap.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j2
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    private final UserDetailsService accountService;
    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/","/login","/h2-console/**","/sign-up","/popup/jusoPopup","/sign-up","/email-login","/roadmap/api/**").permitAll()
                .mvcMatchers(HttpMethod.GET, "/profile/**","/post/**","/roadmap/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated();
        http.csrf().ignoringAntMatchers("/popup/jusoPopup","/settings/location","/roadmap/api/**");
        http.formLogin().loginPage("/login").permitAll();
        //http.logout().deleteCookies("remember-me","JSESSION_ID").logoutSuccessUrl("/");
        http.logout().deleteCookies("JSESSION_ID").logoutSuccessUrl("/");
        http.rememberMe()
                .tokenValiditySeconds(60*60*7)
                .userDetailsService(accountService)
                .tokenRepository(tokenRepository());
        http.oauth2Login().loginPage("/login").successHandler(successHandler());


        http.addFilterBefore(apiCheckFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(apiLoginFilter(),UsernamePasswordAuthenticationFilter.class);

    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        log.info("passwordEncoder : " + passwordEncoder);
        return new LoginSuccessHandler(passwordEncoder);
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    @Bean
    public ApiLoginFilter apiLoginFilter() throws Exception{
        ApiLoginFilter apiLoginFilter = new ApiLoginFilter("/api/login",jwtUtil());
        apiLoginFilter.setAuthenticationManager(authenticationManager());
        apiLoginFilter.setAuthenticationFailureHandler(new ApiLoginFailHandler());
        return apiLoginFilter;

    }

    @Bean
    public JWTUtil jwtUtil(){
        return new JWTUtil();
    }

    @Bean
    public ApiCheckFilter apiCheckFilter() {
        return new ApiCheckFilter("/roadmap/api/**/*",jwtUtil());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/node_modules/**","/**/*.js","/**/*.css","/images/**","/static/js/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}