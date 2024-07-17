package com.codecozy.server.dto.response;

import java.util.List;

public record GetAllLocationResponse(
        List<LocationInfoDto> locationInfoDto,
        boolean isMore
) {}