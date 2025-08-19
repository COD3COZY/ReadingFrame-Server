package com.codecozy.server.dto.response;

// 책갈피 전체 조회 API - 위치 정보 응답을 보낼 때 사용
public record BookmarkLocationInfoDto(
        String placeName,
        String address,
        String latitude,
        String longitude
) { }
