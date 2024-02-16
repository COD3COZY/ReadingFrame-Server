package com.codecozy.server.dto.request;

public record ReactionCommentRequest(
    long commentListIndex,
    long commentReaction
) {}
