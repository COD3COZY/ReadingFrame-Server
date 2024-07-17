package com.codecozy.server.dto.request;

public record SignUpAppleRequest(
        String id_token,
        String nickname,
        String profileImageCode
) {}
