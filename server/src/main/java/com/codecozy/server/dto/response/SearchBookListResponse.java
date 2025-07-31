package com.codecozy.server.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 검색 API에서 사용
@Getter
@AllArgsConstructor
public class SearchBookListResponse {
    private int totalCount;
    List<SearchBookDto> searchList;

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
