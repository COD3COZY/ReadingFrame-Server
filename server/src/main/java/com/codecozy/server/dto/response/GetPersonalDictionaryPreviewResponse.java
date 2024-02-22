package com.codecozy.server.dto.response;

public record GetPersonalDictionaryPreviewResponse(
        int emoji,
        String name,
        String preview
) {}
