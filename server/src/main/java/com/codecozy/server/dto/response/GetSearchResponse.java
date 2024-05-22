package com.codecozy.server.dto.response;

import java.util.List;

// 검색 API에서 사용
public record GetSearchResponse(
        int totalCount,
        List<SearchDto> searchList
) {}
