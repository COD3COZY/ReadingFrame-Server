package com.codecozy.server.dto.response;

public record RecentLocationResponse(
        long locationId,
        String placeName,
        String address,
        double latitude,
        double longitude
) {}
