package checkmo.book.internal.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import checkmo.book.internal.config.properties.Yes24Properties;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.entity.Book;
import checkmo.book.internal.repository.BookLikedRepository;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.common.monitoring.CheckmoMetrics;
import checkmo.common.monitoring.SentryCaptureClient;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;
import checkmo.book.internal.repository.BookRepository;

class Yes24ApiServiceTest {

    private Yes24Client yes24Client;
    private BookRepository bookRepository;
    private Yes24Properties yes24Properties;
    private BookLikedRepository bookLikedRepository;
    private BookSearchCacheService bookSearchCacheService;
    private Yes24SearchClient yes24SearchClient;
    private Yes24SearchPrefetchService yes24SearchPrefetchService;
    private SentryCaptureClient sentryCaptureClient;
    private Yes24ApiService yes24ApiService;

    @BeforeEach
    void setUp() {
        yes24Client = mock(Yes24Client.class);
        bookRepository = mock(BookRepository.class);
        yes24Properties = yes24Properties();
        bookLikedRepository = mock(BookLikedRepository.class, Answers.CALLS_REAL_METHODS);
        bookSearchCacheService = mock(BookSearchCacheService.class);
        yes24SearchClient = mock(Yes24SearchClient.class);
        yes24SearchPrefetchService = mock(Yes24SearchPrefetchService.class);
        sentryCaptureClient = mock(SentryCaptureClient.class);
        yes24ApiService = new Yes24ApiService(
                yes24Client,
                bookRepository,
                yes24Properties,
                bookLikedRepository,
                bookSearchCacheService,
                yes24SearchClient,
                yes24SearchPrefetchService,
                sentryCaptureClient,
                new CheckmoMetrics(new SimpleMeterRegistry())
        );
    }

    @Test
    void applyLikedByMePreservesSearchMetadata() {
        when(bookLikedRepository.findLikedBookIds(eq(1L), anyList()))
                .thenReturn(List.of("9791169213882"));

        BookResponseDTO.BookList response = yes24ApiService.applyLikedByMe(bookList(false, true), 1L);

        assertThat(response.getTotalResults()).isEqualTo(37);
        assertThat(response.getCurrentPage()).isEqualTo(2);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getDetailInfoList()).singleElement()
                .extracting(DetailInfo::isLikedByMe)
                .isEqualTo(true);
    }

    @Test
    void searchBooksUsesCachedRawResultAndAppliesLikedByMe() {
        when(bookSearchCacheService.retrieve("java", 10, 1)).thenReturn(Optional.of(bookList(false, true)));
        when(bookLikedRepository.findLikedBookIds(eq(1L), anyList()))
                .thenReturn(List.of("9791169213882"));

        BookResponseDTO.BookList response = yes24ApiService.retrieveSearchBooks("java", 1, 1L);

        verify(yes24SearchClient, never()).fetchSearchBooks("java", 1);
        assertThat(response.getTotalResults()).isEqualTo(37);
        assertThat(response.getDetailInfoList()).singleElement()
                .extracting(DetailInfo::isLikedByMe)
                .isEqualTo(true);
    }

    @Test
    void searchBooksReturnsEmptyResultWhenKeywordIsBlank() {
        BookResponseDTO.BookList response = yes24ApiService.retrieveSearchBooks("   ", 1, 1L);

        assertThat(response.getTotalResults()).isZero();
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getDetailInfoList()).isEmpty();
        verifyNoInteractions(
                bookLikedRepository,
                bookSearchCacheService,
                yes24SearchClient,
                yes24SearchPrefetchService,
                sentryCaptureClient
        );
    }

    @Test
    void searchBooksCachesSuccessfulRawResultAndPrefetchesNextPage() {
        BookResponseDTO.BookList rawBookList = bookList(false, true);
        when(bookSearchCacheService.retrieve("java", 10, 1)).thenReturn(Optional.empty());
        when(yes24SearchClient.fetchSearchBooks("java", 1)).thenReturn(rawBookList);
        when(bookLikedRepository.findLikedBookIds(eq(1L), anyList()))
                .thenReturn(List.of("9791169213882"));

        BookResponseDTO.BookList response = yes24ApiService.retrieveSearchBooks("java", 1, 1L);

        verify(bookSearchCacheService).save("java", 10, 1, rawBookList);
        verify(yes24SearchPrefetchService).prefetchNextPage("java", 1);
        assertThat(response.getDetailInfoList()).singleElement()
                .extracting(DetailInfo::isLikedByMe)
                .isEqualTo(true);
    }

    @Test
    void searchBooksDoesNotPrefetchWhenHasNextIsFalse() {
        BookResponseDTO.BookList rawBookList = bookList(false, false);
        when(bookSearchCacheService.retrieve("java", 10, 1)).thenReturn(Optional.empty());
        when(yes24SearchClient.fetchSearchBooks("java", 1)).thenReturn(rawBookList);

        yes24ApiService.retrieveSearchBooks("java", 1, 1L);

        verify(yes24SearchPrefetchService, never()).prefetchNextPage("java", 1);
    }

    @Test
    void searchBooksReturnsCachedFallbackAndCapturesYes24Failure() {
        RuntimeException failure = new RuntimeException("aladin unavailable");
        when(bookSearchCacheService.retrieve("java", 10, 1))
                .thenReturn(Optional.empty(), Optional.of(bookList(false, false)));
        when(yes24SearchClient.fetchSearchBooks("java", 1)).thenThrow(failure);
        when(bookLikedRepository.findLikedBookIds(eq(1L), anyList()))
                .thenReturn(List.of("9791169213882"));

        BookResponseDTO.BookList response = yes24ApiService.retrieveSearchBooks("java", 1, 1L);

        verify(sentryCaptureClient).captureException(failure);
        assertThat(response.getTotalResults()).isEqualTo(37);
        assertThat(response.getDetailInfoList()).singleElement()
                .extracting(DetailInfo::isLikedByMe)
                .isEqualTo(true);
    }

    @Test
    void searchBooksThrowsBook500WhenYes24FailsAndCacheMissing() {
        RuntimeException failure = new RuntimeException("aladin unavailable");
        when(bookSearchCacheService.retrieve("java", 10, 1)).thenReturn(Optional.empty());
        when(yes24SearchClient.fetchSearchBooks("java", 1)).thenThrow(failure);

        assertThatThrownBy(() -> yes24ApiService.retrieveSearchBooks("java", 1, 1L))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.ALADIN_API_ERROR)
                )
                .hasCause(failure);
    }

    private Yes24Properties yes24Properties() {
        Yes24Properties properties = new Yes24Properties();
        properties.getSearch().setMaxResults(10);
        return properties;
    }

    @ParameterizedTest
    @EnumSource(value = BookErrorStatus.class, names = {"BOOK_NOT_FOUND", "ALADIN_API_ERROR"})
    void detailFailureReturnsStoredBookWithoutFabricatingLink(BookErrorStatus status) {
        String isbn = "9788936434595";
        var failure = new BookException(status);
        when(yes24Client.retrieveBookDetail(isbn)).thenThrow(failure);
        when(bookRepository.findById(isbn)).thenReturn(Optional.of(Book.builder()
                .id(isbn).title("기존 책").author("기존 저자").publisher("기존 출판사")
                .description("기존 소개").imgUrl("https://image.aladin.co.kr/old.jpg").build()));

        var result = yes24ApiService.retrieveBookDetailInfo(isbn);

        assertThat(result.getIsbn()).isEqualTo(isbn);
        assertThat(result.getTitle()).isEqualTo("기존 책");
        assertThat(result.getDescription()).isEqualTo("기존 소개");
        assertThat(result.getImgUrl()).isEqualTo("https://image.aladin.co.kr/old.jpg");
        assertThat(result.getLink()).isNull();
        verify(sentryCaptureClient).captureException(failure);
        verify(bookRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = BookErrorStatus.class, names = {"BOOK_NOT_FOUND", "ALADIN_API_ERROR"})
    void detailWithoutStoredBookPreservesMissingVersusUnavailableError(BookErrorStatus status) {
        var failure = new BookException(status);
        when(yes24Client.retrieveBookDetail("9788936434595")).thenThrow(failure);
        when(bookRepository.findById("9788936434595")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> yes24ApiService.retrieveBookDetailInfo("9788936434595"))
                .isSameAs(failure);
    }

    @Test
    void successfulDetailDoesNotOverwriteOrReadStoredBook() {
        var detail = DetailInfo.builder().isbn("9788936434595").title("예스24 책").build();
        when(yes24Client.retrieveBookDetail(detail.getIsbn())).thenReturn(detail);
        assertThat(yes24ApiService.retrieveBookDetailInfo(detail.getIsbn())).isSameAs(detail);
        verifyNoInteractions(bookRepository);
    }

    private BookResponseDTO.BookList bookList(boolean likedByMe, boolean hasNext) {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(List.of(DetailInfo.builder()
                        .isbn("9791169213882")
                        .title("테스트 책")
                        .author("테스트 저자")
                        .imgUrl("https://example.com/book.jpg")
                        .publisher("테스트 출판사")
                        .description("테스트 설명")
                        .link("https://example.com/book")
                        .likedByMe(likedByMe)
                        .build()))
                .hasNext(hasNext)
                .currentPage(2)
                .totalResults(37)
                .build();
    }
}
