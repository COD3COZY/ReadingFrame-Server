package com.codecozy.server.dto.response;

public record WantToReadResponse(
        String isbn,
        String cover,
        String title,
        String author,
        int category
) {}
