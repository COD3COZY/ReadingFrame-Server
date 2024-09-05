package com.codecozy.server.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private TokenProvider tokenProvider;
    private final String[] allowedUrls = {"/", "/nickname/**", "/sign-up/**", "/sign-in/**"};

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                           .requestMatchers(allowedUrls)    // 일부 url 허용
                           .requestMatchers(PathRequest.toH2Console()); // h2 콘솔 사용을 위함
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)  // 쿠키 사용 안 함
                .authorizeHttpRequests(requests ->
                        requests.anyRequest().authenticated()   // 그 외 모든 요청은 인증 필요
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )    // 세션을 사용하지 않으므로 STATELESS 설정
                .addFilterBefore(new JwtAuthenticateFilter(tokenProvider),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
