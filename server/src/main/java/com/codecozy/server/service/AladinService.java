package com.codecozy.server.service;

import com.codecozy.server.configuration.AladinConfig;
import com.codecozy.server.context.Category;
import com.codecozy.server.context.ReadingStatus;
import com.codecozy.server.dto.response.SearchOneBookResponse;
import com.codecozy.server.dto.response.SearchBookDto;
import com.codecozy.server.dto.response.SearchBookListResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AladinService {
    private final AladinConfig aladinConfig;
    private final ConverterService converterService;

    // (검색 기능 전용) 전체 책 검색 수행하는 메소드
    public SearchBookListResponse searchBookList(String searchText, int startPage) {
        // 1. request URL 생성
        String urlStr = aladinConfig.createSearchUrl(searchText, startPage);

        // 2. 알라딘 API에 request 보내고 응답 받아오기
        StringBuilder result = requestAPI(urlStr);

        // 3. 받아온 JSON 응답 -> DTO 파싱 후 반환
        return parsingBookListData(result);
    }

    // (단일 도서 전용) 특정 책의 정보를 가져오기를 수행하는 메소드
    public SearchOneBookResponse searchOneBook(String isbn) {
        // 1. request URL 생성
        String urlStr = aladinConfig.createGetOneBookUrl(isbn);

        // 2. 알라딘 API에 request 보내고 응답 받아오기
        StringBuilder result = requestAPI(urlStr);

        // 3. 받아온 JSON 응답 -> DTO 파싱 후 반환
        return parsingOneBookData(result.toString());
    }

    // 해당 URL로 요청을 보내 응답을 받아오는 메소드
    private StringBuilder requestAPI(String urlStr) {
        StringBuilder result = new StringBuilder();

        // URL 객체 생성 및 GET 요청
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            // 응답 데이터를 받아올 버퍼 생성
            // try-with-resources (버퍼를 명시적으로 close 하지 않고, 간결하게 처리하기 위함)
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {

                // 응답 데이터 기록
                result.append(br.readLine());
            }
        } catch (IOException e) {
            log.error(e.toString());
        }

        if (urlConnection != null) {
            urlConnection.disconnect();
        }

        return result;
    }

    // (검색 기능 전용) 알라딘에서 가져온 jSON 데이터를 DTO 객체로 파싱하는 메소드
    private SearchBookListResponse parsingBookListData(StringBuilder jsonStr) {
        // 파싱이 제대로 이루어지도록 맨 끝 세미콜론 제거
        int charIndex = jsonStr.lastIndexOf(";");
        if (charIndex != -1) {
            jsonStr.deleteCharAt(charIndex);
        }

        // JSON 데이터 가져오기
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj = (JSONObject) jsonParser.parse(jsonStr.toString());
        } catch (ParseException e) {
            log.error(e.toString());
        }

        // 검색 결과 리스트 가져오기
        JSONArray itemList = (JSONArray) jsonObj.get("item");

        // 총 검색 결과 수 가져오기
        int itemCount = itemList.size();

        // 프론트측에 보낼 응답 DTO 구성
        List<SearchBookDto> searchBookDto = new ArrayList<>();
        for (int i = 0; i < itemList.size(); i++) {
            JSONObject item = (JSONObject) itemList.get(i);

            String isbn = item.get("isbn13").toString();
            String cover = item.get("cover").toString();
            String title = item.get("title").toString();
            String author = item.get("author").toString();
            String publisher = item.get("publisher").toString();

            // 날짜 형식 변환 (yyyy-MM-dd -> yyyy.MM.dd)
            String tempPublicationDate = item.get("pubDate").toString();
            LocalDate tempDate = LocalDate.parse(tempPublicationDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String publicationDate = converterService.dateToString(tempDate);

            // DTO 정보 넣기
            searchBookDto.add(new SearchBookDto(
                    isbn,
                    cover,
                    title,
                    author,
                    publisher,
                    publicationDate
            ));
        }

        // 최종 응답 DTO 구성
        return new SearchBookListResponse(itemCount, searchBookDto);
    }

    // (단일 도서 전용) 알라딘에서 가져온 jSON 데이터를 DTO 객체로 파싱하는 메소드
    private SearchOneBookResponse parsingOneBookData(String jsonStr) {
        JSONObject jsonResult, jsonResultSub, jsonString = new JSONObject();
        JSONParser jsonParser = new JSONParser();
        try {
            // 파싱할 json 문자열
            jsonString = (JSONObject) jsonParser.parse(jsonStr);
        } catch (ParseException e) {
            log.error(e.toString());
        }

        // item 데이터 받기
        JSONArray jsonArray = (JSONArray) jsonString.get("item");
        jsonResult = (JSONObject) jsonArray.get(0);

        // subInfo 데이터 받기
        jsonResultSub = (JSONObject) jsonResult.get("subInfo");

        // categoryName 수정
        String categoryName = jsonResult.get("categoryName").toString();
        categoryName = Category.extractCategory(categoryName.substring(categoryName.lastIndexOf(">") + 1));
        int categoryValue = Category.getValueByName(categoryName);

        return new SearchOneBookResponse(jsonResult.get("cover").toString(),
                jsonResult.get("title").toString(),
                jsonResult.get("author").toString(),
                categoryValue,
                ReadingStatus.UNREGISTERED,
                jsonResult.get("publisher").toString(),
                jsonResult.get("pubDate").toString(),
                Integer.parseInt(jsonResultSub.get("itemPage").toString()),
                jsonResult.get("description").toString(), 0, null, null);
    }
}
