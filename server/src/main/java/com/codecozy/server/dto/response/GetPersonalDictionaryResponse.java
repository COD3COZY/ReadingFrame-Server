package com.codecozy.server.dto.response;

public record GetPersonalDictionaryResponse(
   String emoji,
   String name,
   String preview,
   String description
) {}
