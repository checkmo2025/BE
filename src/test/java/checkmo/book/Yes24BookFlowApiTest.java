package checkmo.book;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import checkmo.book.internal.BookAPIImpl;
import checkmo.book.internal.entity.Book;
import checkmo.book.internal.repository.BookLikedRepository;
import checkmo.book.internal.repository.BookRepository;
import checkmo.book.internal.service.command.BookCommandService;
import checkmo.book.internal.service.query.BookQueryService;
import checkmo.book.internal.service.query.Yes24RateLimiter;
import checkmo.book.web.dto.Yes24ApiResponseDTO;
import checkmo.bookStory.internal.repository.BookStoryRepository;
import checkmo.clubManagement.internal.repository.ClubRepository;
import checkmo.clubMeeting.internal.repository.MeetingRepository;
import checkmo.support.ApiTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/** JWT/HTTP → 실제 도서 서비스/DB → 모임·책이야기·좋아요. 공급자 HTTP/Redis만 격리한다. */
class Yes24BookFlowApiTest extends ApiTestSupport {
    private static final String ISBN = "9788936434595";
    private static final String INTRO = "긴 소개 ".repeat(1000).trim();
    @Autowired BookCommandService command;
    @Autowired BookQueryService query;
    @Autowired BookRepository books;
    @Autowired BookLikedRepository likes;
    @Autowired BookStoryRepository stories;
    @Autowired MeetingRepository meetings;
    @Autowired ClubRepository clubs;
    @Autowired ObjectMapper mapper;
    @MockitoBean Yes24RateLimiter limiter;
    private MockRestServiceServer provider;

    @BeforeEach
    void connectRealBookModule() {
        // 공통 API 하네스의 BookAPI 대체 객체도 실제 구현에 위임한다.
        var implementation = new BookAPIImpl(command, query);
        doAnswer(delegatesTo(implementation)).when(bookAPI).fetchOrCreateBook(anyString());
        doAnswer(delegatesTo(implementation)).when(bookAPI).fetchBookBasicInfo(anyString());
        doAnswer(delegatesTo(implementation)).when(bookAPI).fetchBookDetailInfo(anyString());
        doAnswer(delegatesTo(implementation)).when(bookAPI).fetchBookBasicInfoByBookIds(anyList());
        var transport = new RestTemplate();
        provider = MockRestServiceServer.bindTo(transport).build();
        doAnswer(inv -> transport.exchange(inv.getArgument(0, URI.class), inv.getArgument(1, HttpMethod.class),
                inv.getArgument(2, HttpEntity.class), Yes24ApiResponseDTO.Response.class))
                .when(restTemplate).exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(Yes24ApiResponseDTO.Response.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"story", "meeting", "like"})
    void anyFirstRegistrationPreservesOneIsbnAcrossAllFeatures(String first) throws Exception {
        TestUser user = createUser();
        expectBook("/goods/itemList", ISBN, "예스24 책", INTRO);
        expectBook("/goods/itemDetail", ISBN, "예스24 책", INTRO);
        given().cookie(accessTokenCookie(user)).queryParam("keyword", "예스24").queryParam("page", 1)
                .get("/api/v1/books/search").then().statusCode(200)
                .body("result.detailInfoList[0].isbn", equalTo(ISBN));
        createFeature(first, user);
        assertThat(books.findById(ISBN).orElseThrow().getDescription()).isEqualTo(INTRO);
        // 등록 이후 공급자 요청 기대값이 없다. 다른 기능은 저장된 책을 재사용해야 한다.
        for (String feature : List.of("story", "meeting", "like")) {
            if (!feature.equals(first)) createFeature(feature, user);
        }
        assertConnections(user, "예스24 책");
        provider.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {404, 503})
    void providerMissingOrUnavailableKeepsExistingBookAndConnections(int status) {
        TestUser user = createUser();
        books.save(Book.builder().id(ISBN).title("기존 책").description("기존 소개")
                .imgUrl("https://image.aladin.co.kr/old.jpg").build());
        provider.expect(requestTo(org.hamcrest.Matchers.containsString("/goods/itemDetail")))
                .andRespond(withStatus(HttpStatus.valueOf(status)).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"success\":false,\"errorCode\":\"GOODS_001\"}"));
        given().get("/api/v1/books/{isbn}", ISBN).then().statusCode(200)
                .body("result.isbn", equalTo(ISBN)).body("result.title", equalTo("기존 책"))
                .body("result.imgUrl", equalTo("https://image.aladin.co.kr/old.jpg"))
                .body("result.link", nullValue());
        for (String feature : List.of("story", "meeting", "like")) createFeature(feature, user);
        assertConnections(user, "기존 책");
        assertThat(books.findById(ISBN).orElseThrow().getDescription()).isEqualTo("기존 소개");
        provider.verify();
    }

    @Test
    void mismatchedProviderIsbnDoesNotCreateStoryOrBook() throws Exception {
        TestUser user = createUser();
        expectBook("/goods/itemDetail", "9788966260959", "다른 책", "소개");
        given().cookie(accessTokenCookie(user)).contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(storyPayload("PUBLISHED")).post("/api/v1/book-stories")
                .then().statusCode(404).body("code", equalTo("BOOK_400"));
        assertThat(books.count()).isZero();
        assertThat(stories.count()).isZero();
        provider.verify();
    }

    @Test
    void actualSaveFailureRollsBackBookAndLike() throws Exception {
        TestUser user = createUser();
        expectBook("/goods/itemDetail", ISBN, "제목".repeat(200), "소개");
        given().cookie(accessTokenCookie(user)).post("/api/v1/books/{isbn}/like", ISBN)
                .then().statusCode(500);
        assertThat(books.count()).isZero();
        assertThat(likes.count()).isZero();
        provider.verify();
    }

    private void expectBook(String path, String isbn, String title, String intro) throws Exception {
        var item = new Yes24ApiResponseDTO.Item(isbn, title, "저자", "출판사",
                "https://image.yes24.com/goods/1/L", "https://www.yes24.com/Product/Goods/1",
                new Yes24ApiResponseDTO.Content("<p>" + intro + "</p>", null));
        var response = new Yes24ApiResponseDTO.Response(true, null,
                new Yes24ApiResponseDTO.Data(List.of(item), 1, 10, 1));
        provider.expect(requestTo(org.hamcrest.Matchers.containsString(path)))
                .andExpect(method(HttpMethod.GET)).andExpect(header("X-Api-Key", "test-yes24-key"))
                .andRespond(withSuccess(mapper.writeValueAsString(response), MediaType.APPLICATION_JSON));
    }

    private Map<String, Object> storyPayload(String status) {
        return Map.of("isbn", ISBN, "title", "이야기", "description", "도서 연결을 확인하는 이야기입니다.", "status", status);
    }

    private void createFeature(String feature, TestUser user) {
        switch (feature) {
            case "story" -> {
                Number id = given().cookie(accessTokenCookie(user)).contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(storyPayload("DRAFT")).post("/api/v1/book-stories")
                        .then().statusCode(200).extract().path("result");
                given().cookie(accessTokenCookie(user)).contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(storyPayload("PUBLISHED")).patch("/api/v1/book-stories/{id}", id.longValue())
                        .then().statusCode(200);
            }
            case "like" -> given().cookie(accessTokenCookie(user)).post("/api/v1/books/{isbn}/like", ISBN)
                    .then().statusCode(200).body("result.liked", equalTo(true));
            case "meeting" -> {
                given().cookie(accessTokenCookie(user)).contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(Map.of("name", "전환독서모임", "description", "도서 연결 확인", "open", true,
                                "region", "서울", "category", List.of("COMPUTER_IT"), "participantTypes", List.of("ONLINE")))
                        .post("/api/v1/clubs").then().statusCode(200);
                Long clubId = clubs.findAll().getFirst().getId();
                given().cookie(accessTokenCookie(user)).contentType(MediaType.APPLICATION_JSON_VALUE)
                        .body(Map.of("title", "정기모임", "meetingTime", LocalDateTime.now().plusDays(1).toString(),
                                "location", "온라인", "generation", 1, "tag", "소설", "isbn", ISBN))
                        .post("/api/v1/clubs/{id}/bookshelves", clubId).then().statusCode(200);
            }
            default -> throw new IllegalArgumentException(feature);
        }
    }

    private void assertConnections(TestUser user, String title) {
        assertThat(books.count()).isEqualTo(1);
        assertThat(stories.findAll()).singleElement().satisfies(story -> assertThat(story.getBookId()).isEqualTo(ISBN));
        assertThat(meetings.findAll()).singleElement().satisfies(meeting -> assertThat(meeting.getBookId()).isEqualTo(ISBN));
        assertThat(likes.findByBook_IdAndMemberId(ISBN, user.memberId())).isPresent();
        assertThat(books.findById(ISBN).orElseThrow().getLikes()).isEqualTo(1);
        given().cookie(accessTokenCookie(user)).get("/api/v1/books/me/likes").then().statusCode(200)
                .body("result.books[0].isbn", equalTo(ISBN)).body("result.books[0].title", equalTo(title));
        given().cookie(accessTokenCookie(user)).get("/api/v1/book-stories/search/{isbn}", ISBN)
                .then().statusCode(200).body("result.basicInfoList.size()", equalTo(1));
    }
}
