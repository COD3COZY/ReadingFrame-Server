package com.codecozy.server.context;

// 뱃지 획득 조건 검사를 위한 action type
public enum BadgeActionType {
    // 서재에 책을 등록할 때 (읽는 중 & 다 읽음)
    CREATE_BOOK,

    // 독서 진행률을 변경할 때 or 다 읽음 표시를 할 때
    UPDATE_READING,

    // 책갈피, 인물사전, 메모를 등록할 때
    CREATE_RECORD,

    // 키워드 리뷰, 선택 리뷰, 한줄평 리뷰를 등록할 때
    CREATE_REVIEW
}
