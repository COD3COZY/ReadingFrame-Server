package com.codecozy.server.dto.request;

public record PersonalDictionaryRequest(
    String emoji,
    String name,
    String preview,
    String description

) {}
