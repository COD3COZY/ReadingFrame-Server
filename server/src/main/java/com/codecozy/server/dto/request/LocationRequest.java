package com.codecozy.server.dto.request;

public record LocationRequest(
   String placeName,
   String address,
   String latitude,
   String longitude
) {}
