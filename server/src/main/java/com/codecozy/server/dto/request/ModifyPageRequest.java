package com.codecozy.server.dto.request;

// 읽은 페이지 변경에 사용
public record ModifyPageRequest(
   int page,
   boolean type
) {}
