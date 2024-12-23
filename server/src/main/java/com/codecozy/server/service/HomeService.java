package com.codecozy.server.service;

import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.dto.response.FinishReadResponse;
import com.codecozy.server.dto.response.MainBooksResponse;
import com.codecozy.server.dto.response.GetMainResponse;
import com.codecozy.server.dto.response.ReadingResponse;
import com.codecozy.server.dto.response.SearchResponse;
import com.codecozy.server.dto.response.WantToReadResponse;
import com.codecozy.server.dto.response.SearchDto;
import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.BookReview;
import com.codecozy.server.entity.SelectReview;
import com.codecozy.server.entity.Member;
import com.codecozy.server.repository.BookRecordDateRepository;
import com.codecozy.server.repository.BookRecordRepository;
import com.codecozy.server.repository.BookRepository;
import com.codecozy.server.repository.BookReviewRepository;
import com.codecozy.server.repository.SelectReviewRepository;
import com.codecozy.server.repository.MemberRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeService {

    private final ConverterService converterService;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final BookRecordRepository bookRecordRepository;
    private final BookRecordDateRepository bookRecordDateRepository;
    private final BookReviewRepository bookReviewRepository;
    private final SelectReviewRepository selectReviewRepository;

    // 독서 상태 상수 값
    private final int UNREGISTERED = -1;    // 미등록
    private final int WANT_TO_READ = 0;     // 읽고 싶은
    private final int READING = 1;          // 읽는 중
    private final int FINISH_READ = 2;      // 다 읽음

    // 검색 시 불러온 응답 JSON 데이터를 원하는 값만 파싱하는 메소드
    private SearchResponse parsingData(String jsonStr) {
        // JSON 데이터 가져오기
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj = (JSONObject) jsonParser.parse(jsonStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        log.info("JSON 데이터 받아오기 성공");
        log.debug(jsonObj.toJSONString());

        // 검색 결과 리스트 가져오기
        JSONArray itemList = (JSONArray) jsonObj.get("item");

        // 총 검색 결과 수 가져오기
        int itemCount = itemList.size();

        // 프론트측에 보낼 응답 DTO 구성
        List<SearchDto> searchDto = new ArrayList<>();
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
            searchDto.add(new SearchDto(
                    isbn,
                    cover,
                    title,
                    author,
                    publisher,
                    publicationDate
            ));
        }

        // 최종 응답 DTO 구성
        return new SearchResponse(itemCount, searchDto);
    }

    // 메인 화면 조회
    public ResponseEntity<DefaultResponse> getMainPage(Long memberId) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        /** 전체 책 리스트 가져오기 **/
        List<MainBooksResponse> booksList = new ArrayList<>();

        // 읽고 있는 책 리스트 가져오기 (최대 10개)
        List<BookRecord> readingBooks = bookRecordDateRepository.getMainReadingBooks(member,
                READING);

        // dto 값 넣기
        for (BookRecord bookRecord : readingBooks) {
            Book book = bookRecord.getBook();

            int totalPage = book.getTotalPage();
            int readPage = bookRecord.getMarkPage();
            int readingPercent = converterService.pageToPercent(readPage, totalPage);

            // 리뷰 유무 판단
            Boolean isWriteReview = false;
            // 1. 한 단어 리뷰
            String keywordReview = bookRecord.getKeyWord();
            // 2. 선택 키워드 리뷰
            SelectReview selectReview = selectReviewRepository.findByBookRecord(bookRecord);
            // 3. 한줄평 리뷰
            BookReview commentReview = bookReviewRepository.findByBookRecord(bookRecord);
            // 리뷰들 중 하나라도 있으면 true
            if (!(keywordReview == null) || !(selectReview == null) || !(commentReview == null)) {
                isWriteReview = true;
            }

            booksList.add(new MainBooksResponse(
                    READING,
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    readingPercent,
                    totalPage,
                    readPage,
                    bookRecord.isMine(),
                    isWriteReview
            ));
        }

        // 읽고 싶은 책 리스트 가져오기 (최대 10개)
        List<BookRecord> wantToReadBooks = bookRecordRepository.findTop10ByMemberAndReadingStatusOrderByCreateDateDesc(
                member, WANT_TO_READ);

        // dto 값 넣기
        for (BookRecord bookRecord : wantToReadBooks) {
            Book book = bookRecord.getBook();

            booksList.add(new MainBooksResponse(
                    WANT_TO_READ,
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    -1,
                    -1,
                    -1,
                    null,
                    null
            ));
        }

        // 다 읽은 책 리스트 가져오기 (전체)
        List<BookRecord> finishReadBooks = bookRecordRepository.findAllByMemberAndReadingStatus(
                member, FINISH_READ);

        // dto 값 넣기
        for (BookRecord bookRecord : finishReadBooks) {
            Book book = bookRecord.getBook();

            // 리뷰 유무 판단
            Boolean isWriteReview = false;
            // 1. 한 단어 리뷰
            String keywordReview = bookRecord.getKeyWord();
            // 2. 선택 키워드 리뷰
            SelectReview selectReview = selectReviewRepository.findByBookRecord(bookRecord);
            // 3. 한줄평 리뷰
            BookReview commentReview = bookReviewRepository.findByBookRecord(bookRecord);
            // 리뷰들 중 하나라도 있으면 true
            if (!(keywordReview == null) || !(selectReview == null) || !(commentReview == null)) {
                isWriteReview = true;
            }

            booksList.add(new MainBooksResponse(
                    FINISH_READ,
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    -1,
                    -1,
                    -1,
                    bookRecord.isMine(),
                    isWriteReview
            ));
        }

        // 읽고 싶은 책 개수 계산
        List<BookRecord> tempWantToReadList = bookRecordRepository.findAllByMemberAndReadingStatus(
                member,
                WANT_TO_READ);
        int wantToReadCount = tempWantToReadList.size();

        // 읽고 있는 책 개수 계산
        List<BookRecord> tempReadingList = bookRecordRepository.findAllByMemberAndReadingStatus(
                member, READING);
        int readingCount = tempReadingList.size();

        // 응답 보내기
        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공",
                        new GetMainResponse(booksList, wantToReadCount,
                                readingCount)),
                HttpStatus.OK);
    }

    // 검색
    public ResponseEntity<DefaultResponse> getSearchList(Long memberId, String searchText)
            throws IOException {
        // 응답 DTO
        SearchResponse response = new SearchResponse(0, new ArrayList<>());

        // 독서노트 내 검색된 책들의 isbn 값을 저장하는 리스트 (중복 제거 위함)
        List<String> isbnList = new ArrayList<>();

        // 1. 사용자가 독서노트에 등록한 책 넣기
        Member member = memberRepository.findByMemberId(memberId);
        List<BookRecord> bookRecordList = bookRecordRepository.findAllByMemberAndBookTitleContains(
                member, searchText);
        response.setTotalCount(bookRecordList.size());
        for (BookRecord bookRecord : bookRecordList) {
            Book book = bookRecord.getBook();
            String publicationDateStr = converterService.dateToString(book.getPublicationDate());
            response.getSearchList().add(new SearchDto(
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getPublisher(),
                    publicationDateStr
            ));

            // isbn값 넣기
            isbnList.add(book.getIsbn());
        }

        // 2. 알라딘 내 검색한 책의 정보 넣기
        // 검색문 공백 제거 (오류 방지를 위함)
        searchText = searchText.replaceAll(" ", "");

        // 알라딘 내 책 검색 (1~4 페이지의 정보 모두 담기)
        for (int i = 1; i < 5; i++) {
            String urlStr =
                    "http://www.aladin.co.kr/ttb/api/ItemSearch.aspx?ttbkey=ttbtmzl2350811001&Query="
                            + searchText
                            + "&QueryType=Keyword&Start="
                            + i
                            + "&MaxResults=50&Cover=Big&Output=JS&Version=20131101";

            URL url = new URL(urlStr);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

            String returnLine;

            StringBuilder result = new StringBuilder();
            while ((returnLine = br.readLine()) != null) {
                result.append(returnLine + "\n\r");
            }

            urlConnection.disconnect();

            // 파싱이 제대로 이루어지도록 맨 끝 세미콜론 제거
            int charIndex = result.lastIndexOf(";");
            if (charIndex != -1) {
                result.deleteCharAt(charIndex);
            }

            // 파싱
            SearchResponse parsingData = parsingData(result.toString());

            // 파싱 데이터에서 불러온 책 count 수 저장
            int totalCount = parsingData.getTotalCount();

            // 만일 파싱에서 뽑아온 책의 count 수가 200이 넘는다면
            // 실제 불러온 정보는 200권까지이므로.... 200으로 조정
            if (totalCount > 200) {
                totalCount = 200;
            }
            // 파싱 데이터 DTO 집어넣기 수행
            for (SearchDto searchInfo : parsingData.getSearchList()) {
                // 중복된 책이면 책 카운트 줄이고 정보 안 담기
                if (isbnList.contains(searchInfo.isbn())) {
                    totalCount--;
                    continue;
                }
                // 이미 독서노트 내에서 검색된 책이 아닐 경우에만 정보 담기
                response.getSearchList().add(searchInfo);
            }
            response.setTotalCount(response.getTotalCount() + totalCount);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", response),
                HttpStatus.OK);
    }

    // 읽고 싶은 책 조회
    public ResponseEntity<DefaultResponse> getWantToReadBooks(Long memberId) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        // 읽고 싶은 책 목록 가져오기
        List<BookRecord> bookRecordList = bookRecordRepository.findAllByMemberAndReadingStatus(
                member,
                WANT_TO_READ);

        // 해당 값 dto 정보 담기
        List<WantToReadResponse> wantToReadBooks = new ArrayList<>();
        for (BookRecord bookRecord : bookRecordList) {
            // 해당 책 정보 꺼내기
            Book book = bookRecord.getBook();

            wantToReadBooks.add(new WantToReadResponse(
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    converterService.categoryNameToCode(book.getCategory())
            ));
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", wantToReadBooks),
                HttpStatus.OK);
    }

    // 읽고 있는 책 조회 시 필요한 정보들을 가져오는 메소드
    private ReadingResponse getReadingBookInfo(BookRecord bookRecord) {
        // 책 정보 가져오기
        Book book = bookRecord.getBook();

        // readingPercent 계산
        int totalPage = book.getTotalPage();
        int readPage = bookRecord.getMarkPage();
        int readingPercent = converterService.pageToPercent(readPage, totalPage);

        // 리뷰의 유무 가져오기
        Boolean isWriteReview = bookReviewRepository.findByBookRecord(bookRecord) != null;

        return new ReadingResponse(
                book.getIsbn(),
                book.getCover(),
                book.getTitle(),
                book.getAuthor(),
                readingPercent,
                totalPage,
                readPage,
                bookRecord.isHidden(),
                converterService.categoryNameToCode(book.getCategory()),
                bookRecord.getBookType(),
                bookRecord.isMine(),
                isWriteReview);
    }

    // 읽고 있는 책 조회
    public ResponseEntity<DefaultResponse> getReadingBooks(Long memberId) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        // 1. 읽고 있는 책 중, 숨기지 않은 책들만 먼저 가져오기
        List<BookRecord> notHiddenBooks = bookRecordRepository.findAllByMemberAndReadingStatusAndIsHidden(
                member,
                READING, false);

        // 2. 읽고 있는 책 중, 숨긴 책들도 가져오기
        List<BookRecord> hiddenBooks = bookRecordRepository.findAllByMemberAndReadingStatusAndIsHidden(
                member,
                READING, true);

        // dto 정보 넣기
        List<ReadingResponse> readingBooks = new ArrayList<>();

        // 숨기지 않은 책들 먼저 정보 넣기
        for (BookRecord bookRecord : notHiddenBooks) {
            readingBooks.add(getReadingBookInfo(bookRecord));
        }

        // 숨긴 책들 정보 넣기
        for (BookRecord bookRecord : hiddenBooks) {
            readingBooks.add(getReadingBookInfo(bookRecord));
        }

        // 응답 보내기
        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", readingBooks),
                HttpStatus.OK);
    }

    // 다 읽은 책 조회
    public ResponseEntity<DefaultResponse> getFinishReadBooks(Long memberId) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        // 책 정보 가져오기
        List<BookRecord> bookRecordList = bookRecordRepository.findAllByMemberAndReadingStatus(
                member, FINISH_READ);

        // dto 정보 넣기
        List<FinishReadResponse> finishReadBooks = new ArrayList<>();
        for (BookRecord bookRecord : bookRecordList) {
            // 책 정보 가져오기
            Book book = bookRecord.getBook();

            // 리뷰의 유무 가져오기
            Boolean isWriteReview = bookReviewRepository.findByBookRecord(bookRecord) != null;

            // dto 정보 추가
            finishReadBooks.add(new FinishReadResponse(
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    converterService.categoryNameToCode(book.getCategory()),
                    bookRecord.getBookType(),
                    bookRecord.isMine(),
                    isWriteReview
            ));
        }

        // 응답 보내기
        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", finishReadBooks),
                HttpStatus.OK);
    }

    // 읽고 있는 책 숨기기 & 꺼내기
    public ResponseEntity<DefaultResponse> modifyHidden(Long memberId, String isbn,
            boolean isHidden) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        // 해당 독서기록 가져오기
        Book book = bookRepository.findByIsbn(isbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 숨기기 & 꺼내기 상태 변경 적용
        bookRecord.setIsHidden(isHidden);
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }
}
