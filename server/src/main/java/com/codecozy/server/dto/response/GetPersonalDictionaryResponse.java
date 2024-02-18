package com.codecozy.server.dto.response;

public record GetPersonalDictionaryResponse(
   int emoji,
   String name,
   String preview,
   String description
) {}
