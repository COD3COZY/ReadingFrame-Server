package com.codecozy.server.context;

// 책 유형
public class BookType {
    public static final int UNKNOWN = -1;       // 읽는중, 다읽은 책이 아닌 책들
    public static final int PAPER_BOOK = 0;     // 종이책
    public static final int E_BOOK = 1;         // 전자책
    public static final int AUDIO_BOOK = 2;     // 오디오북
}
