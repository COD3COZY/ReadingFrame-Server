package com.codecozy.server.dto.request;

public class BookDTO {
    private String isbn;
    private String cover;
    private String title;
    private String author;
    private String category;
    private int totalPage;

    public String getIsbn() { return isbn; }
}
