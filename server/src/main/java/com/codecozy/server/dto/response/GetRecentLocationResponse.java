package com.codecozy.server.dto.response;

public record GetRecentLocationResponse(
        long locationId,
        String placeName,
        String address,
        double latitude,
        double longitude
) {}
