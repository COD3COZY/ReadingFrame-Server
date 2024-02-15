package com.codecozy.server.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class BookCreateRequest {
    private String isbn;
    private int readingStatus;
    private int bookType;
    private List<String> mainLocation;
    private boolean isMine;
    private String startDate;
    private String recentDate;
    private List<String> bookInformation;
}
