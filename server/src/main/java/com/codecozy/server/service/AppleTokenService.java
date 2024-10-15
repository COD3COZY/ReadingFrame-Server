package com.codecozy.server.service;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class AppleTokenService {
    // 공개 키 URL
    private static final String APPLE_PUBLIC_KEYS_URL = "https://appleid.apple.com/auth/keys";

    // idToken 유효성 확인
    public boolean isValid(String idToken) {
        try {
            // idToken 파싱 (parseException 발생 가능 부분)
            SignedJWT signedJWT = SignedJWT.parse(idToken);

            // 검증을 위한 JWSVerifier 생성
            JWSVerifier verifier = createJwsVerifier(signedJWT);

            // 서명 유효성 확인
            if (!signedJWT.verify(verifier)) {
                return false;
            }

            // idToken Claim 검증
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            String issuer = claims.getIssuer();
            String audience = claims.getAudience().get(0);

            // issuer 확인
            if (!"https://appleid.apple.com".equals(issuer)) {
                return false;
            }

            // 애플 개발자 콘솔에서 설정한 client_id와 audience가 일치하는지 확인
            if (!"com.CodeCozy.ReadingFrame".equals(audience)) {
                return false;
            }

            // 토큰 만료 시간 확인
            Date expirationTime = claims.getExpirationTime();
            return new Date().before(expirationTime);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // JWSVerifier 생성
    private JWSVerifier createJwsVerifier(SignedJWT signedJWT) throws Exception {
        // JWSVerifier 생성 (kid를 사용해 애플의 공개 키 가져오기)
        String kid = signedJWT.getHeader().getKeyID();
        RSAKey publicKey = getApplePublicKey(kid);
        return new RSASSAVerifier(publicKey);
    }

    // 공개 키 가져오기
    private RSAKey getApplePublicKey(String kid) throws Exception {
        // 애플 JWK를 통해 공개키 가져오기
        URL url = new URL(APPLE_PUBLIC_KEYS_URL);
        InputStream inputStream = url.openStream();
        String jsonString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        // JSON 파싱
        JSONObject json = new JSONObject(jsonString);
        JSONArray keys = json.getJSONArray("keys");

        // kid에 맞는 키 찾기
        for (int i = 0; i < keys.length(); i++) {
            JSONObject key = keys.getJSONObject(i);
            if (key.getString("kid").equals(kid)) {
                // 키를 파싱해 RSAKey로 변환
                return RSAKey.parse(key.toMap());
            }
        }
        
        // 공개 키에서 해당 kid를 찾지 못한 경우
        throw new Exception("Apple public key not found for kid: " + kid);
    }
}
