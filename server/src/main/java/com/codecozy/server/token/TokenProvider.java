package com.codecozy.server.token;

import com.codecozy.server.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider {
    private final String secretKey;
    private final long expirationDays;
    private final String issuer;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    public TokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.expiration-days}") long expirationDays,
            @Value("${jwt.issuer}") String issuer
    ) {
        this.secretKey = secretKey;
        this.expirationDays = expirationDays;
        this.issuer = issuer;
    }

    public String createAccessToken(Long memberId) {
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, secretKey.getBytes())
                .claim("memberId", memberId)
                .setIssuer(issuer)
                .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
                .setExpiration(Date.from(Instant.now().plus(expirationDays, ChronoUnit.DAYS)))
                .compact();
    }

    // 토큰의 Claim 디코딩
    public Claims getAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    // 인증 정보 조회
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getMemberIdFromToken(token).toString());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // Claim 중 memberId 값 빼오기
    public Long getMemberIdFromToken(String token) {
        String memberIdStr = getAllClaims(token).get("memberId").toString();
        return Long.valueOf(memberIdStr);
    }

    // 토큰 만료기한 가져오기
    public Date getExpirationDate(String token) {
        return getAllClaims(token).getExpiration();
    }

    // 토큰이 만료되었는지 검증
    // true: 만료됨
    public boolean isTokenExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }

    // Request의 Header에서 토큰 값 가져오기
    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("xAuthToken");
    }
}
