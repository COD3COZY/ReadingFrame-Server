package com.codecozy.server.context;

public enum ResponseMessages {

    SUCCESS("성공"),

    // 토큰
    EXPIRED_TOKEN("토큰이 만료되었습니다"),
    NOT_FOUND_TOKEN("토큰이 존재하지 않습니다."),
    INVALID_TOKEN_VALUE("토큰의 값이 유효하지 않습니다."),
    INVALID_TOKEN_SIGNATURE("토큰의 서명이 올바르지 않습니다."),
    INVALID_USER("(토큰 오류) 해당 유저가 존재하지 않습니다."),

    // 사용자
    OK_NICKNAME("사용 가능한 닉네임입니다."),
    CONFLICT_NICKNAME("사용 불가능한 닉네임입니다."),

    NOT_FOUND_USER("해당 사용자가 존재하지 않습니다."),
    CONFLICT_USER("이미 가입한 회원입니다."),
    INVALID_ID_TOKEN("ID 토큰이 유효하지 않습니다."),

    // 독서노트
    NOT_FOUND_BOOK_RECORD("해당 독서노트가 없습니다."),
    CONFLICT_BOOK_RECORD("이미 등록한 독서노트입니다."),
    CANNOT_REGISTER_BOOK_AS_WANT_TO_READ("독서 노트에 등록한 도서는 읽고 싶은 도서로 등록할 수 없습니다."),

    // 위치
    CONFLICT_MAIN_LOCATION("대표 위치가 이미 있습니다."),
    NOT_FOUND_MAIN_LOCATION("해당 독서노트의 대표 위치가 없습니다."),

    NOT_FOUND_LOCATION("조회할 위치가 없습니다."),
    NOT_LATEST_LOCATION("최근 등록된 위치가 아닙니다."),
    UNREGISTERED_LOCATION("등록되지 않은 위치입니다."),

    NOT_FOUND_MARKER("조회할 마크가 없습니다."),
    NOT_FOUND_MARKER_TO_DETAIL("세부 조회할 마크가 없습니다."),

    // 리뷰
    NOT_FOUND_REGISTERED_COMMENT("등록된 한줄평이 없습니다."),
    CONFLICT_COMMENT("이미 등록한 한줄평입니다."),
    NOT_FOUND_COMMENT("해당 한줄평이 없습니다."),

    NOT_FOUND_COMMENT_REACTION("해당 한줄평에 남긴 반응이 없습니다."),
    CONFLICT_REPORT("이미 신고한 리뷰입니다."),

    CONFLICT_SELECT_REVIEW("이미 등록한 선택 리뷰입니다."),

    // 책갈피
    CONFLICT_BOOKMARK("이미 등록한 책갈피입니다."),
    UNREGISTERED_BOOKMARK("등록하지 않은 책갈피입니다."),
    NOT_FOUND_BOOKMARK("해당 책갈피가 없습니다."),

    // 메모
    CONFLICT_MEMO("이미 등록한 메모입니다."),
    UNREGISTERED_MEMO("등록하지 않은 메모입니다."),
    NOT_FOUND_MEMO("해당 메모가 없습니다."),

    // 인물사전
    CONFLICT_CHARACTER("이미 등록한 인물입니다."),
    UNREGISTERED_CHARACTER("등록하지 않은 인물입니다."),
    NOT_FOUND_CHARACTER("해당 인물이 없습니다."),

    // 기타
    MISSING_UUID("uuid 값이 없습니다.");

    private final String message;

    ResponseMessages(String message) {
        this.message = message;
    }

    public String get() {
        return message;
    }
}
