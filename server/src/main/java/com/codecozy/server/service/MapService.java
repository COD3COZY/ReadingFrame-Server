package com.codecozy.server.service;

import com.codecozy.server.context.ResponseMessages;
import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.request.MarkDetailRequest;
import com.codecozy.server.dto.response.AllLocationResponse;
import com.codecozy.server.dto.response.AllMarkerResponse;
import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.dto.response.LocationInfoDto;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.Bookmark;
import com.codecozy.server.entity.LocationInfo;
import com.codecozy.server.entity.Member;
import com.codecozy.server.repository.BookRecordRepository;
import com.codecozy.server.repository.BookmarkRepository;
import com.codecozy.server.repository.LocationInfoRepository;
import com.codecozy.server.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapService {

    private final ConverterService converterService;
    private final MemberRepository memberRepository;
    private final BookRecordRepository bookRecordRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LocationInfoRepository locationInfoRepository;

    // 전체 위치 조회
    public ResponseEntity<DefaultResponse> getAllLocation(Long memberId, int orderNumber) {
        // 응답으로 보낼 위치 리스트
        List<LocationInfoDto> locationInfo = new ArrayList<>();

        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 조회할 위치가 더 있는지에 대한 flag
        BooleanWrapper isBookRecordEnd = new BooleanWrapper();
        isBookRecordEnd.setValue(false);
        BooleanWrapper isBookmarkEnd = new BooleanWrapper();
        isBookmarkEnd.setValue(false);

        addLocationsAll(member, orderNumber, false, locationInfo, isBookRecordEnd);
        addLocationsAll(member, orderNumber, true, locationInfo, isBookmarkEnd);

        if (locationInfo.isEmpty()) {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_LOCATION.get()),
                    HttpStatus.NOT_FOUND);
        }

        AllLocationResponse allLocationResponse = new AllLocationResponse(locationInfo,
                (isBookRecordEnd.getValue() && isBookmarkEnd.getValue()));

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), allLocationResponse),
                HttpStatus.OK);
    }

    // 지도 마크 조회
    public ResponseEntity<DefaultResponse> getAllMarker(Long memberId) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 독서노트의 대표위치, 책갈피의 위치를 가져와 리스트 생성
        List<AllMarkerResponse> allMarkers = Stream.concat(
                member.getBookRecords().stream()
                      .map(bookRecord -> new AllMarkerResponse(
                              bookRecord.getLocationInfo().getLocationId(),
                              bookRecord.getLocationInfo().getLatitude(),
                              bookRecord.getLocationInfo().getLongitude(),
                              false)),
                bookmarkRepository.findAllByBookRecordMember(member).stream()
                                  .map(bookmark -> new AllMarkerResponse(
                                          bookmark.getLocationInfo().getLocationId(),
                                          bookmark.getLocationInfo().getLatitude(),
                                          bookmark.getLocationInfo().getLongitude(),
                                          true
                                  ))
        ).collect(Collectors.toList());

        if (allMarkers.isEmpty()) { // 조회할 마크가 하나도 없는 경우
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_MARKER.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), allMarkers),
                HttpStatus.OK);
    }

    // 마크 세부 조회
    public ResponseEntity<DefaultResponse> getMarkDetail(Long memberId, MarkDetailRequest request) {
        // 응답으로 보낼 위치 리스트
        List<LocationInfoDto> locationInfoList = new ArrayList<>();

        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        int orderNumber = request.orderNumber();
        long locationId = request.locationId();

        // 조회할 위치가 더 있는지에 대한 flag
        BooleanWrapper isBookRecordEnd = new BooleanWrapper();
        isBookRecordEnd.setValue(false);
        BooleanWrapper isBookmarkEnd = new BooleanWrapper();
        isBookmarkEnd.setValue(false);

        // locationId로 위치 정보 검색
        LocationInfo locationInfo = locationInfoRepository.findByLocationId(locationId);

        // 독서노트, 북마크에서 위치 정보를 가져오는 메소드 호출
        addLocations(member, locationInfo, orderNumber, false, locationInfoList, isBookRecordEnd);
        addLocations(member, locationInfo, orderNumber, true, locationInfoList, isBookmarkEnd);

        if (locationInfoList.isEmpty()) { // 세부 조회할 마크가 하나도 없는 경우
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_MARKER_TO_DETAIL.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(),
                        new AllLocationResponse(locationInfoList,
                                (isBookRecordEnd.getValue() && isBookmarkEnd.getValue()))),
                HttpStatus.OK);
    }

    /** 헬퍼 클래스 및 메소드 **/

    // Boolean을 참조 타입으로 전달하기 위한 클래스
    private class BooleanWrapper {
        private boolean value;

        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }
    }

    // 전체 위치 조회를 위한 독서노트, 책갈피에서의 위치 정보 조회
    private void addLocationsAll(Member member, int orderNumber, boolean isBookmark, List<LocationInfoDto> locationInfo,
            BooleanWrapper isEnd) {
        // 정보 받아오기
        List<?> records = isBookmark ? bookmarkRepository.findAllByBookRecordMember(member) : member.getBookRecords();

        int size = records.size();
        BookRecord bookRecord;
        Bookmark bookmark;

        for (int i = (orderNumber * 20); i < 20 + (orderNumber * 20); i++) {
            // 이번이 마지막 조회면 (전체 개수와 이번 orderNumber를 사용하여 비교)
            if ((orderNumber + 1) * 20 >= size) {
                isEnd.setValue(true);
            }

            // 현재 조회하는 컬럼이 받아온 컬럼들의 사이즈보다 같거나 크면 break
            if (i >= size) {
                break;
            }

            // Response 전달 시 들어가는 데이터
            String date;
            String title;
            int readPage;
            long locationId;
            String placeName;

            if (isBookmark) { // 책갈피면
                bookmark = (Bookmark) records.get(i);
                if (bookmark.getLocationInfo() == null) {
                    break;
                }

                // 책갈피에서 날짜, 제목, 읽은 페이지, 위치 아이디, 장소명을 받아오기
                date = converterService.dateToString(bookmark.getDate());
                title = bookmark.getBookRecord().getBook().getTitle();
                readPage = bookmark.getMarkPage();
                locationId = bookmark.getLocationInfo().getLocationId();
                placeName = bookmark.getLocationInfo().getPlaceName();

            } else { // 독서노트면
                bookRecord = (BookRecord) records.get(i);
                if (bookRecord.getLocationInfo() == null) {
                    break;
                }

                // 독서노트에서 날짜, 제목, 읽은 페이지, 위치 아이디, 장소명을 받아오기
                date = converterService.dateToString(bookRecord.getStartDate());
                title = bookRecord.getBook().getTitle();
                readPage = bookRecord.getMarkPage();
                locationId = bookRecord.getLocationInfo().getLocationId();
                placeName = bookRecord.getLocationInfo().getPlaceName();
            }

            // 받아온 정보로 위치 정보 추가
            locationInfo.add(new LocationInfoDto(date, isBookmark, title, readPage, locationId, placeName));
        }
    }

    // 마크 세부 조회를 위한 독서노트, 책갈피에서의 위치 정보 조회
    private void addLocations(Member member, LocationInfo locationInfo, int orderNumber, boolean isBookmark,
            List<LocationInfoDto> locationInfoList, BooleanWrapper isEnd) {
        // 북마크인지 여부에 따라 필요한 레포지토리 검색
        List<?> records = isBookmark ? bookmarkRepository.findAllByBookRecordMemberAndLocationInfo(member, locationInfo)
                : bookRecordRepository.findAllByMemberAndLocationInfo(member, locationInfo);

        int size = records.size();
        BookRecord bookRecord;
        Bookmark bookmark;

        for (int i = (orderNumber * 20); i < 20 + (orderNumber * 20); i++) {
            // 이번이 마지막 조회면 (전체 개수와 이번 orderNumber를 사용하여 비교)
            if ((orderNumber + 1) * 20 >= size) {
                isEnd.setValue(true);
            }

            // 현재 조회하는 컬럼이 받아온 컬럼들의 사이즈보다 같거나 크면 break
            if (i >= size) {
                break;
            }

            // Response 전달 시 들어가는 데이터
            String date;
            String title;
            int readPage;
            long locationId;
            String placeName;

            if (isBookmark) { // 책갈피면
                bookmark = (Bookmark) records.get(i);
                if (bookmark.getLocationInfo() == null) {
                    break;
                }

                // 책갈피에서 날짜, 제목, 읽은 페이지, 위치 아이디, 장소명을 받아오기
                date = converterService.dateToString(bookmark.getDate());
                title = bookmark.getBookRecord().getBook().getTitle();
                readPage = bookmark.getMarkPage();
                locationId = bookmark.getLocationInfo().getLocationId();
                placeName = bookmark.getLocationInfo().getPlaceName();

            } else { // 독서노트면
                bookRecord = (BookRecord) records.get(i);
                if (bookRecord.getLocationInfo() == null) {
                    break;
                }

                // 독서노트에서 날짜, 제목, 읽은 페이지, 위치 아이디, 장소명을 받아오기
                date = converterService.dateToString(bookRecord.getStartDate());
                title = bookRecord.getBook().getTitle();
                readPage = bookRecord.getMarkPage();
                locationId = bookRecord.getLocationInfo().getLocationId();
                placeName = bookRecord.getLocationInfo().getPlaceName();
            }

            // 받아온 정보로 위치 정보 추가
            locationInfoList.add(new LocationInfoDto(date, isBookmark, title, readPage, locationId, placeName));
        }
    }
}
