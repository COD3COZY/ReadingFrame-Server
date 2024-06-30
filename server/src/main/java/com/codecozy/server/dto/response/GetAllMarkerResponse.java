package com.codecozy.server.dto.response;

public record GetAllMarkerResponse(
        double latitude,
        double longitude,
        boolean locationType
) {}
