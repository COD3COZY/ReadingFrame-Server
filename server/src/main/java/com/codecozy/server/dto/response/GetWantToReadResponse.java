package com.codecozy.server.dto.response;

public record GetWantToReadResponse(
        String isbn,
        String cover,
        String title,
        String author,
        int category
) {}
