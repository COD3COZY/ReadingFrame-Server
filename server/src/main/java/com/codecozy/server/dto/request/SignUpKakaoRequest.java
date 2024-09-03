package com.codecozy.server.dto.request;

public record SignUpKakaoRequest(
        String email,
        String nickname,
        String profileImageCode
) {}
