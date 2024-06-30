package com.codecozy.server.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class ConverterService {
    // 장르 이름 -> 코드로 변경하는 메소드
    public int categoryNameToCode(String name) {
        if (name.equals("인문사회")) {
            return 0;
        } else if (name.equals("문학")) {
            return 1;
        } else if (name.equals("에세이")) {
            return 2;
        } else if (name.equals("과학")) {
            return 3;
        } else if (name.equals("자기계발")) {
            return 4;
        } else if (name.equals("예술")) {
            return 5;
        } else if (name.equals("원서")) {
            return 6;
        } else {  // 기타
            return 7;
        }
    }

    // 장르 코드 -> 이름으로 변경하는 메소드
    public String categoryCodeToName(int code) {
        if (code == 0) {
            return "인문사회";
        } else if (code == 1) {
            return "문학";
        } else if (code == 2) {
            return "에세이";
        } else if (code == 3) {
            return "과학";
        } else if (code == 4) {
            return "자기계발";
        } else if (code == 5) {
            return "예술";
        } else if (code == 6) {
            return "원서";
        } else {  // 기타
            return "기타";
        }
    }

    // page -> percent 값으로 변경하는 메소드
    public int pageToPercent(int readPage, int totalPage) {
        return (int) ((double) readPage / totalPage * 100);
    }

    // percent -> page 값으로 변경하는 메소드
    public int percentToPage(int percent, int totalPage) {
        return (int) (percent / 100.0 * totalPage);
    }

    // LocalDate -> String format(프론트측 형식)으로 변경하는 메소드
    public String dateToString(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }

    // String format(프론트측 형식) -> LocalDate로 변경하는 메소드
    public LocalDate stringToDate(String dateStr) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }
}
