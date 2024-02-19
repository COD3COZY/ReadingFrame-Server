package com.codecozy.server.dto.request;

public record ReactionCommentRequest(
    String name,
    long commentReaction
) {}
