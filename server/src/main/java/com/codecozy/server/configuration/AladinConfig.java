package com.codecozy.server.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
// 알라딘 API 관련 설정 및 URL 메소드
public class AladinConfig {
    private final String lookupUrl;
    private final String searchUrl;
    private final String ttbKey;
    private final String cover;
    private final String output;
    private final String version;
    private final String itemIDType;

    public AladinConfig(
            @Value("${aladin.lookup-url}") String lookupUrl,
            @Value("${aladin.search-url}") String searchUrl,
            @Value("${aladin.ttbkey}") String ttbKey,
            @Value("${aladin.default-params.cover}") String cover,
            @Value("${aladin.default-params.output}") String output,
            @Value("${aladin.default-params.version}") String version,
            @Value("${aladin.item-id-type}") String itemIDType
    ) {
        this.lookupUrl = lookupUrl;
        this.searchUrl = searchUrl;
        this.ttbKey = ttbKey;
        this.cover = cover;
        this.output = output;
        this.version = version;
        this.itemIDType = itemIDType;
    }

    // (검색 기능 전용) 특정 키워드로 전체 책 검색 URL을 작성하는 메소드
    public String createSearchUrl(String searchText, int startPage) {
        StringBuilder sb = new StringBuilder();
        sb.append(searchUrl)
                .append("?ttbkey=")
                .append(ttbKey)
                .append("&Query=")
                .append(searchText)
                .append("&QueryType=Keyword")
                .append("&Start=")
                .append(startPage)
                .append("&MaxResults=50")
                .append("&Cover=")
                .append(cover)
                .append("&Output=")
                .append(output)
                .append("&Version=")
                .append(version);

        return sb.toString();
    }

    // (단일 도서 전용) 특정 도서의 정보를 가져오는 URL을 작성하는 메소드
    public String createGetOneBookUrl(String isbn) {
        StringBuilder sb = new StringBuilder();
        sb.append(lookupUrl)
                .append("?ttbkey=")
                .append(ttbKey)
                .append("&itemIdType=")
                .append(itemIDType)
                .append("&ItemId=")
                .append(isbn)
                .append("&Cover=")
                .append(cover)
                .append("&Output=")
                .append(output)
                .append("&Version=")
                .append(version);

        return sb.toString();
    }
}
