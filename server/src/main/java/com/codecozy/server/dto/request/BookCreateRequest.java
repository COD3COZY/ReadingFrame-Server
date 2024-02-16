package com.codecozy.server.dto.request;

import lombok.Data;

import java.util.List;

public record BookCreateRequest (
    String isbn,
    String cover,
    String title,
    String author,
    String category,
    int totalPage
) {}
