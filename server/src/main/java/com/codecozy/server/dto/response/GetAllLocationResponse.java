package com.codecozy.server.dto.response;

import java.util.List;

public record GetAllLocationResponse(
  String date,
  boolean locationType,
  String title,
  int readPage,
  List<String> location
) {}