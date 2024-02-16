package com.codecozy.server.repository;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.PersonalDictionary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalDictionaryRepository extends JpaRepository<PersonalDictionary, Long> {
    PersonalDictionary findByMemberAndBookAndName(Member member, Book book, String name);
}
