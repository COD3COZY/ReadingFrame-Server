package com.codecozy.server.dto.request;

public record SignUpAppleRequest(
        String userIdentifier,
        String idToken,
        String nickname,
        String profileImageCode
) {}
