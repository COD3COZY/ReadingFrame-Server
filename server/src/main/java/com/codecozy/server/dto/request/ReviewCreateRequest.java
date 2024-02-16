package com.codecozy.server.dto.request;

import java.util.List;

public record ReviewCreateRequest (
        String keyword,
        List<Integer> select,
        String comment

) {}
