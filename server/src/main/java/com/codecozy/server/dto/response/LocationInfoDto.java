package com.codecozy.server.dto.response;

public record LocationInfoDto(
        String date,
        boolean locationType,
        String title,
        int readPage,
        long locationId,
        String placeName
) {}
