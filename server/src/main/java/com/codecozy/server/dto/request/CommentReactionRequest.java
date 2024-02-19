package com.codecozy.server.dto.request;

public record CommentReactionRequest(
    String name,
    int commentReaction
) {}
