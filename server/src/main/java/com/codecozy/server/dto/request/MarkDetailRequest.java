package com.codecozy.server.dto.request;

import com.codecozy.server.dto.response.LocationInfoDto;

import java.util.List;

public record MarkDetailRequest(
   long locationId,
   int orderNumber
) {}
