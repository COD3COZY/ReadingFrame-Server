package com.codecozy.server.controller;

import com.codecozy.server.dto.BookDTO;
import com.codecozy.server.entity.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BookController {
    private final Book book = new Book();
    private final PersonalDictionary personalDictionary = new PersonalDictionary();
    private final Bookmark bookmark = new Bookmark();
    private final BookRecord bookRecord = new BookRecord();
    private final Memo memo = new Memo();

    //@GetMapping("/books/gets")
    //public String getBook() {
    //    return "hello!";
    //}

    @PostMapping("/books/create")
    public void postBook(@RequestBody BookDTO bookDTO) {
    }
}
