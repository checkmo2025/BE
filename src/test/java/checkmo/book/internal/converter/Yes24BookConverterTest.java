package checkmo.book.internal.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.web.dto.Yes24ApiResponseDTO;
import checkmo.book.web.dto.Yes24ApiResponseDTO.Content;
import checkmo.book.web.dto.Yes24ApiResponseDTO.Data;
import checkmo.book.web.dto.Yes24ApiResponseDTO.Item;
import checkmo.book.web.dto.Yes24ApiResponseDTO.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class Yes24BookConverterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ISBN = "9788936434595";
    private static final String OTHER_ISBN = "9788966260959";

    @Test
    void decodesProviderJsonAndKeepsExistingBookResponseFields() throws Exception {
        var source = objectMapper.readValue("""
                {"success":true,"message":"성공","errorCode":null,"data":{
                  "meta":{"version":"v1"},"currentPage":1,"pageSize":10,"totalCount":34,
                  "items":[{"itemId":999,"sortOrder":1,"isbn13":"9788936434595",
                    "title":"채식주의자","author":"한강 저","publisher":"창비",
                    "cover":"https://image.yes24.com/goods/999/L",
                    "link":"https://www.yes24.com/product/goods/999",
                    "goodsType":"도서","contentDetail":{"bookIntroduction":"<p>소개 &amp; 내용</p>"}}]}}
                """, Yes24ApiResponseDTO.Response.class);

        var result = BookConverter.toYes24BookList(source);

        assertThat(result.getCurrentPage()).isEqualTo(1);
        assertThat(result.getTotalResults()).isEqualTo(34);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getDetailInfoList()).singleElement().satisfies(book -> {
            assertThat(book.getIsbn()).isEqualTo(ISBN);
            assertThat(book.getTitle()).isEqualTo("채식주의자");
            assertThat(book.getAuthor()).isEqualTo("한강 저");
            assertThat(book.getPublisher()).isEqualTo("창비");
            assertThat(book.getImgUrl()).isEqualTo("https://image.yes24.com/goods/999/L");
            assertThat(book.getLink()).isEqualTo("https://www.yes24.com/product/goods/999");
            assertThat(book.getDescription()).isEqualTo("소개 & 내용");
            assertThat(book.isLikedByMe()).isFalse();
            var json = objectMapper.valueToTree(book);
            assertThat(json.size()).isEqualTo(8);
            assertThat(json.has("itemId")).isFalse();
            assertThat(json.has("bookId")).isFalse();
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "0000000000000", "9788936434596", "1234567890128", "978-8936434595"})
    void excludesInvalidIdentifiersWithoutLosingPagination(String invalidIsbn) {
        var result = BookConverter.toYes24BookList(response(List.of(item(invalidIsbn)), 1, 10, 34));
        assertThat(result.getDetailInfoList()).isEmpty();
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getCurrentPage()).isEqualTo(1);
        assertThat(result.getTotalResults()).isEqualTo(34);
    }

    @Test
    void preservesProviderOrderAndStopsAtLastPage() {
        var result = BookConverter.toYes24BookList(response(
                Arrays.asList(item(OTHER_ISBN), null, item(ISBN)), 4, 10, 34));
        assertThat(result.getDetailInfoList()).extracting("isbn").containsExactly(OTHER_ISBN, ISBN);
        assertThat(result.isHasNext()).isFalse();
    }

    @Test
    void emptyResultKeepsExistingNullPageContract() {
        var result = BookConverter.toYes24BookList(response(List.of(), 1, 10, 0));
        assertThat(result.getDetailInfoList()).isEmpty();
        assertThat(result.getCurrentPage()).isNull();
        assertThat(result.getTotalResults()).isZero();
        assertThat(result.isHasNext()).isFalse();
    }

    @Test
    void selectsExactIsbnInsteadOfFirstProductAndUsesFirstExactCandidate() {
        var result = BookConverter.toYes24BookDetail(response(
                List.of(item(OTHER_ISBN), item(ISBN), item(ISBN)), 1, 10, 3), ISBN);
        assertThat(result.getIsbn()).isEqualTo(ISBN);
        assertThatThrownBy(() -> BookConverter.toYes24BookDetail(
                response(List.of(item(OTHER_ISBN)), 1, 10, 1), ISBN))
                .isInstanceOfSatisfying(BookException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(BookErrorStatus.BOOK_NOT_FOUND));
    }

    @Test
    void preservesLongIntroductionAndRemovesHtmlAndScriptText() {
        String longText = "긴 소개 ".repeat(200).trim();
        var book = new Item(ISBN, "책", "저자", "출판사", "cover", "link",
                new Content("<script>hidden()</script><style>hidden{}</style><p>" + longText
                        + "</p><br>&nbsp;끝", "사용하지 않을 요약"));
        var result = BookConverter.toYes24BookDetail(response(List.of(book), 1, 10, 1), ISBN);
        assertThat(result.getDescription()).isEqualTo(longText + " 끝").hasSizeGreaterThan(255);
    }

    @Test
    void handlesMissingContentAndFallsBackToSummary() {
        assertThat(BookConverter.toYes24BookDetail(response(List.of(item(ISBN)), 1, 10, 1), ISBN)
                .getDescription()).isEmpty();
        var book = new Item(ISBN, "책", null, null, null, null, new Content(" ", "<b>요약</b>"));
        assertThat(BookConverter.toYes24BookDetail(response(List.of(book), 1, 10, 1), ISBN)
                .getDescription()).isEqualTo("요약");
    }

    @Test
    void doesNotTurnProviderFailuresOrMalformedPayloadsIntoSuccessfulEmptyResults() {
        for (var source : Arrays.asList(null, new Response(false, "AUTH_002", null),
                new Response(true, null, null), response(null, 1, 10, 1),
                response(List.of(), null, 10, 1), response(List.of(), 1, 0, 1))) {
            assertThatThrownBy(() -> BookConverter.toYes24BookList(source))
                    .isInstanceOfSatisfying(BookException.class,
                            e -> assertThat(e.getErrorCode()).isEqualTo(BookErrorStatus.YES24_API_ERROR));
        }
    }

    private Item item(String isbn) {
        return new Item(isbn, "책", "저자", "출판사", "cover", "link", null);
    }

    private Response response(List<Item> items, Integer page, int size, int total) {
        return new Response(true, null, new Data(items, page, size, total));
    }
}
