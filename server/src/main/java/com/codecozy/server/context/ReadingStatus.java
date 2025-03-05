package com.codecozy.server.context;

// 독서상태 값 모음
public class ReadingStatus {
    public static final int UNREGISTERED = -1;    // 미등록
    public static final int WANT_TO_READ = 0;     // 읽고 싶은
    public static final int READING = 1;          // 읽는 중
    public static final int FINISH_READ = 2;      // 다 읽음
}
