# ReadingFrame-Server

### Commit Message Convention
```
feat: 새로운 기능을 추가하는 경우(일반적인 구현)

test: 테스트 코드를 추가하는 경우

refactor: 코드를 리펙토링한 경우

fix: 버그를 고친 경우

docs: 문서를 수정한 경우

style: 코드 포맷 변경, 세미콜론 누락 수정 등 코드 수정이 없는 경우

rename: 파일명 또는 폴더명을 수정한 경우

move: 코드 또는 파일의 이동이 있는 경우

remove: 코드 또는 파일을 삭제한 경우

comment: 필요한 주석 추가 및 변경
```

```
ex) feat: commit message
```

### Test Code Convention
- 테스트 코드 작성 시 `assertJ` 라이브러리를 사용합니다.
- `@DisplayName`을 통해 해당 테스트가 무슨 테스트인지 작성합니다.
- 단위 테스트로 진행하며, given, when, then의 구조로 작성합니다.
  > `given` : 테스트 수행을 위한 사전 조건 세팅</br>
  > `when` : 테스트 수행 로직</br>
  > `then` : 테스트 결과 검증

```java
// 테스트 코드 예시

// 기본 사용법
// import org.assertj.core.api.Assertions;

// 정적 import를 통해 더 간단하게 작성 할 수 있습니다.
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class BookRecordTest {
    @Test
    @DisplayName("독서노트의 키워드 리뷰 변경 테스트")
    void changeKeywordReview() {
        // given
        BookRecord bookRecord = BookRecord.create( ... );

        // when
        bookRecord.setKeyword("재밌는 책");

        // then
        assertThat(bookRecord.getKeyword()).isEqualTo("재밌는 책");
}
```
