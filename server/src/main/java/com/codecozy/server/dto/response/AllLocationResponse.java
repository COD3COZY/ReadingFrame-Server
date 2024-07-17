package com.codecozy.server.dto.response;

import java.util.List;

public record AllLocationResponse(
        List<LocationInfoDto> locationInfoDto,
        boolean isEnd
) {}