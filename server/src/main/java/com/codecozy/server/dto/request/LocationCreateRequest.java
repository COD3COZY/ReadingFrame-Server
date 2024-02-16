package com.codecozy.server.dto.request;

public record LocationCreateRequest (
   String placeName,
   String address,
   String latitude,
   String longitude
) {}
