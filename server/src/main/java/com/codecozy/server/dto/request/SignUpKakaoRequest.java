package com.codecozy.server.dto.request;

public record SignUpKakaoRequest(
        String nickname,
        String profileImageCode
) {}
