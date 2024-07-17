package com.codecozy.server.dto.response;

public record AllMarkerResponse(
        long locationId,
        double latitude,
        double longitude,
        boolean locationType
) {}
