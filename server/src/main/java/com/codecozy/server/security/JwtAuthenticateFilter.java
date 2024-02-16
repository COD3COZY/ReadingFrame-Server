package com.codecozy.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticateFilter extends OncePerRequestFilter {
    @Autowired
    private TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 헤더에서 토큰 받아오기
        String token = tokenProvider.resolveToken(request);

        // 토큰이 유효하다면
        if (token != null && !tokenProvider.isTokenExpired(token)) {
            // 토큰으로부터 유저 정보를 받아
            Authentication authentication = tokenProvider.getAuthentication(token);
            // 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 다음 Filter 실행
        filterChain.doFilter(request, response);
    }
}
