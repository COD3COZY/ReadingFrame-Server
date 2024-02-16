package com.codecozy.server.dto.request;

public record BookCreateRequest (
    String isbn,
    String cover,
    String title,
    String author,
    String category,
    int totalPage
) {}
