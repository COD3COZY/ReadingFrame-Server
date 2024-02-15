package com.codecozy.server.controller;

import com.codecozy.server.dto.request.BookDTO;
import com.codecozy.server.service.BookService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BookController {
    private final BookService bookService = new BookService();

    @GetMapping("/books/gets")
    public String getBook() {
        return "book";
    }

    @PostMapping("/books/create")
    public void postBook(@RequestBody BookDTO bookDTO) {
        bookService.create(bookDTO);
    }
}
