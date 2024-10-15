package com.codecozy.server.dto.request;

public record SignInAppleRequest(
        String userIdentifier,
        String idToken
) {}
