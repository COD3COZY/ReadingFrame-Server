package com.codecozy.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.codecozy.server.configuration.AladinConfig;
import com.codecozy.server.dto.response.SearchBookListResponse;
import com.codecozy.server.dto.response.SearchOneBookResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(profiles = "local")
@SpringBootTest(classes = {AladinConfig.class, AladinService.class})
public class AladinServiceTest {
    @MockBean
    private ConverterService converterService;

    @Autowired
    private AladinService aladinService;

    @Test
    @DisplayName("특정 텍스트가 제목에 포함된 책 모두를 알라딘에서 검색해 불러온다")
    void searchBookList() {
        // given
        String searchText = "우리가빛의속도로갈수없다면";
        when(converterService.dateToString(any(LocalDate.class))).thenCallRealMethod();

        // when
        SearchBookListResponse findList = aladinService.searchBookList(searchText, 1);

        // then
        assertThat(findList.getTotalCount()).isEqualTo(findList.getSearchList().size());
        assertThat(findList.getSearchList()).isNotEmpty();
        assertThat(findList.getSearchList().get(0).title()).contains("우리가 빛의 속도로 갈 수 없다면");

        // 날짜가 'yyyy.MM.dd' 형태로 변환됐는지 확인
        assertThat(findList.getSearchList().get(0).publicationDate()).matches("\\d{4}\\.\\d{2}\\.\\d{2}");
    }

    @Test
    @DisplayName("ISBN 값을 통해 특정 책의 정보를 알라딘에서 불러온다")
    void searchOneBook() {
        // given
        String isbn = "9791190090018";

        // when
        SearchOneBookResponse find = aladinService.searchOneBook(isbn);

        // then
        assertThat(find).isNotNull();
        assertThat(find.getTitle()).contains("우리가 빛의 속도로 갈 수 없다면");
    }
}
