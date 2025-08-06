package com.codecozy.server.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 도서 정보 초기 조회 API에서 사용
// 단일 도서의 정보 DTO
@Getter
@AllArgsConstructor
public class SearchOneBookResponse {
    private String cover;
    private String title;
    private String author;
    private int categoryName;
    private int readingStatus;
    private String publisher;
    private String publicationDate;
    private int totalPage;
    private String description;
    private int commentCount;
    private List<Integer> selectReviewList;
    private List<String> commentList;

    public void setReadingStatus(int readingStatus) {
        this.readingStatus = readingStatus;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public void setSelectReviewList(List<Integer> selectReviewList) {
        this.selectReviewList = selectReviewList;
    }

    public void setCommentList(List<String> commentList) {
        this.commentList = commentList;
    }
}