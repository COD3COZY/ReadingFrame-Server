package com.codecozy.server.dto.response;

public record SearchDto(
        String isbn,
        String cover,
        String title,
        String author,
        String publisher,
        String publicationDate
) {}
