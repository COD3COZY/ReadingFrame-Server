package com.codecozy.server.dto.response;

public record PersonalDictionaryResponse(
   int emoji,
   String name,
   String preview,
   String description
) {}
