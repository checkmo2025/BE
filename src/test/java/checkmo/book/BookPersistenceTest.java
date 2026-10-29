package checkmo.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import checkmo.common.config.QueryDslConfig;
import checkmo.clubManagement.ClubManagementAPI;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import checkmo.book.internal.entity.Book;
import checkmo.book.internal.repository.BookRepository;
import checkmo.book.internal.service.command.BookCommandService;
import checkmo.book.internal.service.query.Yes24ApiService;
import checkmo.book.internal.exception.BookException;
import checkmo.book.web.dto.BookResponseDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({BookCommandService.class, QueryDslConfig.class})
class BookPersistenceTest {
    private static final String ISBN = "9788936434595";
    @Autowired BookRepository repository;
    @Autowired EntityManager entityManager;
    @Autowired BookCommandService service;
    @MockitoBean Yes24ApiService catalog;
    @MockitoBean ClubManagementAPI clubManagementAPI;

    @AfterEach
    void clean() { repository.deleteAll(); }

    @Test
    void longIntroductionIsStoredWithoutTruncation() {
        String text = "긴 소개 ".repeat(1000);
        when(catalog.retrieveBookDetailInfo(ISBN)).thenReturn(detail(text));
        assertThat(service.fetchOrCreateBook(ISBN)).isEqualTo(ISBN);
        assertThat(repository.findById(ISBN).orElseThrow().getDescription()).isEqualTo(text);
    }

    @Test
    void existingBookIsReusedWithoutProviderLookupOrMetadataOverwrite() {
        repository.saveAndFlush(Book.builder().id(ISBN).title("기존 제목").likes(8).build());
        assertThat(service.fetchOrCreateBook(ISBN)).isEqualTo(ISBN);
        verifyNoInteractions(catalog);
        repository.insertIfAbsent(Book.builder().id(ISBN).title("새 제목").build(), LocalDateTime.now());
        entityManager.clear();
        assertThat(repository.findById(ISBN).orElseThrow().getTitle()).isEqualTo("기존 제목");
        assertThat(repository.findById(ISBN).orElseThrow().getLikes()).isEqualTo(8);
    }

    @Test
    void wrongIsbnIsNotSaved() {
        when(catalog.retrieveBookDetailInfo(ISBN)).thenReturn(BookResponseDTO.DetailInfo.builder().isbn("9788966260959").build());
        assertThatThrownBy(() -> service.fetchOrCreateBook(ISBN)).isInstanceOf(BookException.class);
        assertThat(repository.count()).isZero();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentFirstRegistrationsCreateOneBook() throws Exception {
        when(catalog.retrieveBookDetailInfo(anyString())).thenReturn(detail("소개"));
        try (var pool = Executors.newFixedThreadPool(8)) {
            List<Callable<String>> calls = IntStream.range(0, 8)
                    .mapToObj(i -> (Callable<String>) () -> service.fetchOrCreateBook(ISBN)).toList();
            for (var result : pool.invokeAll(calls)) assertThat(result.get()).isEqualTo(ISBN);
        }
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void unrelatedColumnOverflowIsNotReportedAsSuccessfulCreation() {
        when(catalog.retrieveBookDetailInfo(ISBN)).thenReturn(BookResponseDTO.DetailInfo.builder()
                .isbn(ISBN).title("제목".repeat(200)).description("소개").build());
        assertThatThrownBy(() -> service.fetchOrCreateBook(ISBN)).isInstanceOf(DataIntegrityViolationException.class);
    }

    private BookResponseDTO.DetailInfo detail(String text) {
        return BookResponseDTO.DetailInfo.builder().isbn(ISBN).title("책").author("저자")
                .publisher("출판사").description(text).imgUrl("https://image.yes24.com/goods/test/L").build();
    }
}
