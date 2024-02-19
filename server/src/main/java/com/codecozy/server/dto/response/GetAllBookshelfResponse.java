package com.codecozy.server.dto.response;

import java.util.List;

public record GetAllBookshelfResponse(
        int categoryCode,
        int categoryCount,
        List<Integer> totalPage
) {}
