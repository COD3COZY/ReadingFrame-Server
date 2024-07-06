package com.codecozy.server.dto.response;

public record GetAllMarkerResponse(
        long locationId,
        double latitude,
        double longitude,
        boolean locationType
) {}
