package com.codecozy.server.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 검색 API에서 사용
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetSearchResponse {
    private int totalCount;
    List<SearchDto> searchList;

    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
}
