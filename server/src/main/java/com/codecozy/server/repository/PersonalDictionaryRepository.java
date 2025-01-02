package com.codecozy.server.repository;

import com.codecozy.server.composite_key.PersonalDictionaryKey;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.PersonalDictionary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonalDictionaryRepository extends JpaRepository<PersonalDictionary, PersonalDictionaryKey> {
    // 특정 독서노트 내에서 이름으로 인물사전 찾기
    PersonalDictionary findByBookRecordAndName(BookRecord bookRecord, String name);

    // 특정 독서노트 내에 있는 모든 인물사전 가져오기
    List<PersonalDictionary> findAllByBookRecord(BookRecord bookRecord);

    // 특정 독서노트 내의 이름순 3개 인물사전 가져오기
    List<PersonalDictionary> findTop3ByBookRecordOrderByNameAsc(BookRecord bookRecord);
}
