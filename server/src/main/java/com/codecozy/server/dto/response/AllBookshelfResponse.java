package com.codecozy.server.dto.response;

import java.util.List;

public record AllBookshelfResponse(
        int categoryCode,
        int categoryCount,
        List<Integer> totalPage
) {}
