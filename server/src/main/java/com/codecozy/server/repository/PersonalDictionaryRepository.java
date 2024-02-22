package com.codecozy.server.repository;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.PersonalDictionary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonalDictionaryRepository extends JpaRepository<PersonalDictionary, Long> {
    PersonalDictionary findByMemberAndBookAndName(Member member, Book book, String name);
    List<PersonalDictionary> findAllByMemberAndBook(Member member, Book book);

    // 특정 유저의 이름순 3개 인물사전 가져오기
    List<PersonalDictionary> findTop3ByMemberAndBookOrderByNameAsc(Member member, Book book);
}
