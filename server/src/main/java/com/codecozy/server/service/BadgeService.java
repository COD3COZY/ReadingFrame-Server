package com.codecozy.server.service;

import com.codecozy.server.context.BadgeActionType;
import com.codecozy.server.dto.etc.BadgeEvent;
import com.codecozy.server.dto.etc.CreateBookAction;
import com.codecozy.server.dto.etc.NoteWithReview;
import com.codecozy.server.entity.Badge;
import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.Bookmark;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.Memo;
import com.codecozy.server.entity.PersonalDictionary;
import com.codecozy.server.repository.BadgeRepository;
import com.codecozy.server.repository.BookRecordRepository;
import com.codecozy.server.repository.BookmarkRepository;
import com.codecozy.server.repository.MemberRepository;
import com.codecozy.server.repository.MemoRepository;
import com.codecozy.server.repository.PersonalDictionaryRepository;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {
    private final BadgeRepository badgeRepository;
    private final MemberRepository memberRepository;
    private final BookRecordRepository bookRecordRepository;

    private final BookmarkRepository bookmarkRepository;
    private final PersonalDictionaryRepository personalDictionaryRepository;
    private final MemoRepository memoRepository;

    private final ConverterService converterService;

    // 독서상태 값 모음
    private static final int UNREGISTERED = -1;    // 미등록
    private static final int WANT_TO_READ = 0;     // 읽고 싶은
    private static final int READING = 1;          // 읽는 중
    private static final int FINISH_READ = 2;      // 다 읽음

    // 뱃지 코드 모음
//    private static final int BOOK_COUNT_CODE = 0;
    private static final int FINISHER_CODE = 10;
    private static final int RECORD_MVP_CODE = 20;
    private static final int REVIEW_MASTER_CODE = 30;
    private static final int GENRE_CODE = 40;

    // 뱃지 획득 기준 모음
    // '서재에 등록한 책' 뱃지
    private static final int[] BOOK_COUNT_ARR = new int[]{1, 10, 50, 100, 200, 500};
    // '완독가' 뱃지
    private static final int[] FINISHER_COUNT_ARR = new int[]{1, 10, 50, 100};
    // '리뷰 마스터' 뱃지
    private static final int[] REVIEW_MASTER_COUNT_ARR = new int[]{30, 100};

    // 이벤트를 받아 각 액션별 뱃지 획득 조건을 검사하는 메소드
    @Transactional
    public void verifyBadgeActivity(BadgeEvent event) {
        log.info("---뱃지 획득 조건 검사 시작---");
        log.info("들어온 이벤트: " + Arrays.toString(event.actionType()));
        Member member = memberRepository.findByMemberId(event.memberId());
        List<Integer> acquiredBadgeList = member.getBadges().stream()
                                                .map(Badge::getBadgeCode)
                                                .toList();

        for (BadgeActionType actionType : event.actionType()) {
            switch (actionType) {
                // 서재에 등록한 책, 완독가, 장르 애호가
                case CREATE_BOOK -> {
                    log.info("CREATE_BOOK 검사");
                    // 읽는중, 다읽음 상태의 독서노트 모두 가져오기
                    List<Integer> readingStatusList = List.of(READING, FINISH_READ);
                    List<CreateBookAction> nowBookList = bookRecordRepository.findAllByMemberAndReadingStatusIn(
                            member, readingStatusList);

                    // 1. '서재에 등록한 책' 뱃지 검사
                    int bookCount = nowBookList.size();
                    for (int code = 0; code < BOOK_COUNT_ARR.length; code++) {
                        if (BOOK_COUNT_ARR[code] <= bookCount) {
                            // 뱃지를 획득하지 않은 상태라면 뱃지 획득 수행
                            if (!acquiredBadgeList.contains(code)) {
                                badgeRepository.save(Badge.create(member, code, event.date()));
                            }
                        } else {
                            // 획득 조건을 못 넘었다면 중단
                            break;
                        }
                    }

                    // 2. '완독가' 뱃지 검사
                    bookCount = nowBookList.stream()
                                           .filter(bookRecord -> bookRecord.readingStatus() == FINISH_READ)
                                           .toList().size();
                    for (int index = 0; index < FINISHER_COUNT_ARR.length; index++) {
                        if (FINISHER_COUNT_ARR[index] <= bookCount) {
                            // 뱃지 코드 조합
                            int code = index + FINISHER_CODE;

                            // 뱃지를 획득하지 않은 상태라면 뱃지 획득 수행
                            if (!acquiredBadgeList.contains(code)) {
                                badgeRepository.save(Badge.create(member, code, event.date()));
                            }
                        } else {
                            break;
                        }
                    }

                    // 3. '장르 애호가' 뱃지 검사
                    // TODO: 추후 테스트 필요
                    List<String> nowCategoryList = nowBookList.stream()
                                                              .map(CreateBookAction::book)
                                                              .map(Book::getCategory)
                                                              .distinct()
                                                              .toList();
                    for (String category : nowCategoryList) {
                        int code = converterService.categoryNameToCode(category);

                        // 뱃지를 획득하는 장르가 아닐 경우 넘김
                        if (code >= 6) {
                            continue;
                        }

                        // 이미 획득한 뱃지가 아니라면 획득 수행
                        code += GENRE_CODE;
                        if (!acquiredBadgeList.contains(code)) {
                            badgeRepository.save(Badge.create(member, code, event.date()));
                        }
                    }
                }

                // 완독가
                case UPDATE_READING -> {
                    log.info("UPDATE_READING 검사");
                    // 읽는중, 다읽음 상태의 독서노트 모두 가져오기
                    List<Integer> readingStatusList = List.of(READING, FINISH_READ);
                    List<CreateBookAction> nowBookList = bookRecordRepository.findAllByMemberAndReadingStatusIn(
                            member, readingStatusList);

                    int bookCount = nowBookList.stream()
                                               .filter(bookRecord -> bookRecord.readingStatus() == FINISH_READ)
                                               .toList().size();
                    for (int index = 0; index < FINISHER_COUNT_ARR.length; index++) {
                        if (FINISHER_COUNT_ARR[index] <= bookCount) {
                            // 뱃지 코드 조합
                            int code = index + FINISHER_CODE;

                            // 뱃지를 획득하지 않은 상태라면 뱃지 획득 수행
                            if (!acquiredBadgeList.contains(code)) {
                                badgeRepository.save(Badge.create(member, code, event.date()));
                            }
                        } else {
                            break;
                        }
                    }
                }

                // 기록 MVP (책갈피, 인물사전, 메모)
                case CREATE_RECORD -> {
                    log.info("CREATE_RECORD 검사");
                    // 책갈피, 인물사전, 메모 가져오기
                    List<Bookmark> bookmarks = bookmarkRepository.findAllByBookRecordMember(member);
                    List<PersonalDictionary> personalDictionaries =
                            personalDictionaryRepository.findAllByBookRecordMember(member);
                    boolean hasMemos = memoRepository.existsByBookRecordMember(member);

                    // 1. '첫 기록' 뱃지 검사
                    int code = RECORD_MVP_CODE;
                    // 현재 획득한 뱃지가 아니라면 검사 수행
                    if (!acquiredBadgeList.contains(code)) {
                        if (bookmarks.size() > 0 && personalDictionaries.size() > 0 && hasMemos) {
                            badgeRepository.save(Badge.create(member, code, event.date()));
                        }
                    }

                    // 2. '두꺼운 인물사전' 뱃지 검사
                    code++;
                    if (!acquiredBadgeList.contains(code)) {
                        if (personalDictionaries.size() >= 100) {
                            badgeRepository.save(Badge.create(member, code, event.date()));
                        }
                    }

                    // 3. '책갈피의 산' 뱃지 검사
                    code++;
                    if (!acquiredBadgeList.contains(code)) {
                        if (bookmarks.size() >= 100) {
                            badgeRepository.save(Badge.create(member, code, event.date()));
                        }
                    }
                }

                // 리뷰 마스터 (키워드, 선택, 한줄평)
                case CREATE_REVIEW -> {
                    log.info("CREATE_REVIEW 검사");
                    // 읽는중, 다읽음 상태의 리뷰가 하나라도 존재하는 독서노트 모두 가져오기
                    List<Integer> readingStatusList = List.of(READING, FINISH_READ);
                    List<NoteWithReview> reviewList = bookRecordRepository.getBookRecordWithReviews(member,
                            readingStatusList);

                    // 1. '리뷰계의 라이징스타' 뱃지 검사
                    int code = REVIEW_MASTER_CODE;
                    if (!acquiredBadgeList.contains(code)) {
                        // 0: 키워드
                        // 1: 선택
                        // 2: 한줄평
                        Boolean[] hasReview = new Boolean[3];
                        for (NoteWithReview review : reviewList) {
                            if (review.keyword() != null) {
                                hasReview[0] = true;
                            }
                            if (review.selectReview() != null) {
                                hasReview[1] = true;
                            }
                            if (review.bookReview() != null) {
                                hasReview[2] = true;
                            }

                            // 다 획득했다면 끝
                            boolean isAllReview = Arrays.stream(hasReview).allMatch(r -> r);
                            if (isAllReview) {
                                badgeRepository.save(Badge.create(member, code, event.date()));
                                break;
                            }
                        }
                    }

                    // 2. '리뷰 마스터', '리뷰 인플루언서' 뱃지 검사 (리뷰 개수 관련 뱃지)
                    for (int count : REVIEW_MASTER_COUNT_ARR) {
                        // 획득 조건을 넘었다면
                        if (count <= reviewList.size()) {
                            code++;
                            if (!acquiredBadgeList.contains(code)) {
                                badgeRepository.save(Badge.create(member, code, event.date()));
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        log.info("---뱃지 획득 조건 검사 끝---");
    }
}
