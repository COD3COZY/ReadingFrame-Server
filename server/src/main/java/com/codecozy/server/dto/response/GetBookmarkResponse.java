package com.codecozy.server.dto.response;

import com.codecozy.server.entity.LocationList;

import java.util.List;

public record GetBookmarkResponse(
        String date,
        int markPage,
        List<String> location,
        String uuid
) {}
