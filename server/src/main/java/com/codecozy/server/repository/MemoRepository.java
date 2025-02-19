package com.codecozy.server.repository;

import com.codecozy.server.composite_key.MemoKey;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, MemoKey> {
    // 특정 독서노트 내에서 uuid로 메모 찾기
    Memo findByBookRecordAndUuid(BookRecord bookRecord, String uuid);

    // 특정 독서노트 내에 있는 모든 메모 찾기
    List<Memo> findAllByBookRecord(BookRecord bookRecord);

    // 특정 독서노트 내에 작성된 최근 3개의 메모 가져오기
    List<Memo> findTop3ByBookRecordOrderByDateDesc(BookRecord bookRecord);

    // (뱃지 검사용) 특정 유저의 메모 작성 여부 가져오기
    boolean existsByBookRecordMember(Member member);
}
