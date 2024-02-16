package com.codecozy.server.dto.request;

public record BookCreateRequest (
    String cover,
    String title,
    String author,
    String category,
    String totalPage
) {}
