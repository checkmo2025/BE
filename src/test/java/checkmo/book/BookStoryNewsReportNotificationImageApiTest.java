package checkmo.book;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import checkmo.book.internal.service.query.AladinApiService;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.bookStory.internal.repository.CommentRepository;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.infra.s3.internal.entity.FileUploadType;
import checkmo.infra.s3.internal.exception.S3ErrorStatus;
import checkmo.infra.s3.internal.exception.S3InfraException;
import checkmo.infra.s3.web.dto.S3ResponseDTO;
import checkmo.news.internal.entity.News;
import checkmo.news.internal.entity.NewsCarousel;
import checkmo.news.internal.repository.NewsRepository;
import checkmo.notification.internal.entity.Notification;
import checkmo.notification.internal.entity.Notification.NotificationType;
import checkmo.notification.internal.entity.NotificationSetting;
import checkmo.notification.internal.repository.NotificationRepository;
import checkmo.notification.internal.repository.NotificationSettingRepository;
import checkmo.report.internal.repository.ReportRepository;
import checkmo.support.ApiTestSupport;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class BookStoryNewsReportNotificationImageApiTest extends ApiTestSupport {

    private static final String ISBN = "9791169213882";

    @MockitoBean
    AladinApiService aladinApiService;

    @MockitoBean
    ClubManagementAPI clubManagementAPI;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    NotificationSettingRepository notificationSettingRepository;

    @BeforeEach
    void setUpDomainApiDoubles() {
        DetailInfo detail = bookDetail(ISBN);
        BookResponseDTO.BookList oneBookList = BookResponseDTO.BookList.builder()
                .detailInfoList(List.of(detail))
                .currentPage(1)
                .hasNext(false)
                .totalResults(123)
                .build();

        when(aladinApiService.retrieveSearchBooks(eq("자바"), eq(1), anyString())).thenReturn(oneBookList);
        when(aladinApiService.retrieveBookDetailInfo(ISBN)).thenReturn(detail);
        when(aladinApiService.retrieveRecommendedBooks()).thenReturn(BookResponseDTO.BookList.builder()
                .detailInfoList(IntStream.rangeClosed(1, 28)
                        .mapToObj(index -> bookDetail("979116921%04d".formatted(index)))
                        .toList())
                .currentPage(1)
                .hasNext(false)
                .build());
        when(aladinApiService.applyLikedByMe(any(BookResponseDTO.BookList.class), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(clubManagementAPI.fetchClubNamesByClubIds(anyList())).thenReturn(Map.of(77L, "테스트 독서모임"));
        when(s3Service.generatePresignedUploadUrl("profile.png", "image/png", FileUploadType.PROFILE))
                .thenReturn(S3ResponseDTO.PresignedUrl.builder()
                        .presignedUrl("https://upload.example.com/profile.png")
                        .imageUrl("https://cdn.example.com/profile.png")
                        .build());
        when(s3Service.generatePresignedUploadUrl("profile.txt", "text/plain", FileUploadType.PROFILE))
                .thenThrow(new S3InfraException(S3ErrorStatus.INVALID_FILE_TYPE));
    }

    @Test
    void bookSearchDetailRecommendLikeAndLikedListsSucceed() {
        TestUser user = createUser();

        given()
                .cookie(accessTokenCookie(user))
                .queryParam("keyword", "자바")
                .queryParam("page", 1)
                .when()
                .get("/api/v1/books/search")
                .then()
                .statusCode(200)
                .body("result.detailInfoList[0].isbn", equalTo(ISBN))
                .body("result.totalResults", equalTo(123));

        given()
                .when()
                .get("/api/v1/books/{isbn}", ISBN)
                .then()
                .statusCode(200)
                .body("result.isbn", equalTo(ISBN));

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/books/recommend")
                .then()
                .statusCode(200)
                .body("result.detailInfoList.size()", equalTo(4));

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .post("/api/v1/books/{isbn}/like", ISBN)
                .then()
                .statusCode(200)
                .body("result.liked", equalTo(true));

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/books/me/likes")
                .then()
                .statusCode(200)
                .body("result.books[0].isbn", equalTo(ISBN));

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/books/{memberNickname}/likes", user.nickName())
                .then()
                .statusCode(200)
                .body("result.books[0].likedByMe", equalTo(true));

        given()
                .when()
                .post("/api/v1/books/{isbn}/like", ISBN)
                .then()
                .statusCode(401);
    }

    @Test
    void bookStoryCrudListsLikeCommentAndFailuresSucceed() {
        TestUser author = createUser();
        TestUser other = createUser();
        Long clubId = 77L;
        when(clubManagementAPI.validateAndFetchActiveClubMemberId(eq(clubId), eq(author.id()))).thenReturn(1L);
        when(clubManagementAPI.fetchActiveMemberIds(clubId)).thenReturn(List.of(author.id()));

        Number storyIdNumber = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(author))
                .body(Map.of(
                        "isbn", ISBN,
                        "title", "책 이야기",
                        "description", "책 이야기를 충분히 작성합니다.",
                        "status", "PUBLISHED"
                ))
                .when()
                .post("/api/v1/book-stories")
                .then()
                .statusCode(200)
                .extract()
                .path("result");
        Long storyId = storyIdNumber.longValue();

        given().when().get("/api/v1/book-stories")
                .then().statusCode(200).body("result.basicInfoList.size()", greaterThanOrEqualTo(1));
        given().cookie(accessTokenCookie(author)).when().get("/api/v1/book-stories/me")
                .then().statusCode(200).body("result.basicInfoList.size()", equalTo(1));
        given().cookie(accessTokenCookie(author)).when().get("/api/v1/book-stories/following")
                .then().statusCode(200);
        given().cookie(accessTokenCookie(author)).when().get("/api/v1/book-stories/members/{nickname}", author.nickName())
                .then().statusCode(200).body("result.basicInfoList.size()", equalTo(1));
        given().cookie(accessTokenCookie(author)).when().get("/api/v1/book-stories/clubs/{clubId}", clubId)
                .then().statusCode(200).body("result.basicInfoList.size()", equalTo(1));
        given().cookie(accessTokenCookie(author)).when().get("/api/v1/book-stories/search/{bookId}", ISBN)
                .then().statusCode(200).body("result.basicInfoList.size()", equalTo(1));
        given().cookie(accessTokenCookie(author)).when().get("/api/v1/book-stories/{bookStoryId}", storyId)
                .then().statusCode(200).body("result.bookStoryId", equalTo(storyId.intValue()));

        given().cookie(accessTokenCookie(other)).when().post("/api/v1/book-stories/{bookStoryId}/like", storyId)
                .then().statusCode(200).body("message", equalTo("좋아요가 추가되었습니다."));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(other))
                .body(Map.of("content", "좋은 글입니다."))
                .when()
                .post("/api/v1/book-stories/{bookStoryId}/comments", storyId)
                .then()
                .statusCode(200);
        Long commentId = commentRepository.findAll().getFirst().getId();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(other))
                .body(Map.of("content", "수정 댓글입니다."))
                .when()
                .patch("/api/v1/book-stories/{bookStoryId}/comments/{commentId}", storyId, commentId)
                .then()
                .statusCode(200);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(author))
                .body(Map.of(
                        "isbn", ISBN,
                        "title", "수정된 책 이야기",
                        "description", "수정된 내용을 충분히 작성합니다.",
                        "status", "PUBLISHED"
                ))
                .when()
                .patch("/api/v1/book-stories/{bookStoryId}", storyId)
                .then()
                .statusCode(200);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(other))
                .body(Map.of("title", "권한 없음", "description", "권한 없음", "status", "PUBLISHED"))
                .when()
                .patch("/api/v1/book-stories/{bookStoryId}", storyId)
                .then()
                .statusCode(403);

        given().cookie(accessTokenCookie(other))
                .when().delete("/api/v1/book-stories/{bookStoryId}/comments/{commentId}", storyId, commentId)
                .then().statusCode(200);
        given().cookie(accessTokenCookie(author))
                .when().delete("/api/v1/book-stories/{bookStoryId}", storyId)
                .then().statusCode(200);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(author))
                .body(Map.of(
                        "isbn", "bad",
                        "title", "검증 실패",
                        "description", "ISBN 검증 실패를 확인할 설명입니다.",
                        "status", "PUBLISHED"
                ))
                .when()
                .post("/api/v1/book-stories")
                .then()
                .statusCode(400);
    }

    @Test
    void bookStorySitemapReturnsPublishedMetadataOnly() {
        TestUser author = createUser();
        Long olderPublishedId = createBookStory(author, "공개 책 이야기 1", "사이트맵에 포함될 공개 책 이야기입니다.", "PUBLISHED");
        Long newerPublishedId = createBookStory(author, "공개 책 이야기 2", "사이트맵에 포함될 더 최신 공개 책 이야기입니다.", "PUBLISHED");
        createBookStory(author, "임시 저장 책 이야기", "초안은 사이트맵에서 제외됩니다.", "DRAFT");
        Long deletedId = createBookStory(author, "삭제된 책 이야기", "삭제된 글은 사이트맵에서 제외됩니다.", "PUBLISHED");
        given()
                .cookie(accessTokenCookie(author))
                .when()
                .delete("/api/v1/book-stories/{bookStoryId}", deletedId)
                .then()
                .statusCode(200);

        List<Map<String, Object>> items = given()
                .when()
                .get("/api/v1/book-stories/sitemap")
                .then()
                .statusCode(200)
                .body("result.pageSize", equalTo(1000))
                .body("result.hasNext", equalTo(false))
                .extract()
                .path("result.items");

        assertThat(items).hasSize(2);
        assertThat(items.get(0).keySet()).containsExactly("id", "updatedAt");
        assertThat(items).extracting(item -> ((Number) item.get("id")).longValue())
                .containsExactly(newerPublishedId, olderPublishedId);

        given()
                .queryParam("limit", 1)
                .when()
                .get("/api/v1/book-stories/sitemap")
                .then()
                .statusCode(200)
                .body("result.items.size()", equalTo(1))
                .body("result.pageSize", equalTo(1))
                .body("result.hasNext", equalTo(true))
                .body("result.nextCursor", equalTo(newerPublishedId.intValue()));

        given()
                .queryParam("limit", 999999)
                .when()
                .get("/api/v1/book-stories/sitemap")
                .then()
                .statusCode(200)
                .body("result.pageSize", equalTo(5000));

        given()
                .queryParam("limit", 0)
                .when()
                .get("/api/v1/book-stories/sitemap")
                .then()
                .statusCode(400);
    }

    private Long createBookStory(TestUser author, String title, String description, String status) {
        Number storyId = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(author))
                .body(Map.of(
                        "isbn", ISBN,
                        "title", title,
                        "description", description,
                        "status", status
                ))
                .when()
                .post("/api/v1/book-stories")
                .then()
                .statusCode(200)
                .extract()
                .path("result");
        return storyId.longValue();
    }

    @Test
    void newsListMyListAndDetailUseH2Rows() {
        TestUser user = createUser();
        News news = newsRepository.save(News.builder()
                .title("서비스 소식")
                .requesterEmail(user.email())
                .content("공개되는 소식 본문입니다.")
                .thumbnailUrl("https://example.com/thumb.png")
                .originalLink("https://example.com/news")
                .publishStartAt(LocalDate.now().minusDays(1))
                .publishEndAt(LocalDate.now().plusDays(1))
                .carousel(NewsCarousel.GENERAL)
                .build());
        news.replaceImages(List.of("https://example.com/news-1.png"));
        newsRepository.save(news);

        given().when().get("/api/v1/news")
                .then().statusCode(200).body("result.basicInfoList[0].newsId", equalTo(news.getId().intValue()));
        given().cookie(accessTokenCookie(user)).when().get("/api/v1/news/me")
                .then().statusCode(200).body("result.basicInfoList[0].newsId", equalTo(news.getId().intValue()));
        given().when().get("/api/v1/news/{newsId}", news.getId())
                .then().statusCode(200).body("result.title", equalTo("서비스 소식"));
        given().when().get("/api/v1/news/{newsId}", 999999)
                .then().statusCode(404);
    }

    @Test
    void newsSitemapReturnsPublishedPromotionMetadataOnly() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        News olderPromotion = newsRepository.save(News.builder()
                .title("현재 프로모션 소식 1")
                .requesterEmail("requester-1@example.com")
                .content("사이트맵에 포함될 현재 프로모션 소식입니다.")
                .publishStartAt(today.minusDays(1))
                .publishEndAt(today.plusDays(1))
                .carousel(NewsCarousel.PROMOTION)
                .build());
        News newerPromotion = newsRepository.save(News.builder()
                .title("현재 프로모션 소식 2")
                .requesterEmail("requester-2@example.com")
                .content("사이트맵에 포함될 더 최신 현재 프로모션 소식입니다.")
                .publishStartAt(today.minusDays(1))
                .publishEndAt(today.plusDays(1))
                .carousel(NewsCarousel.PROMOTION)
                .build());
        newsRepository.save(News.builder()
                .title("현재 일반 소식")
                .requesterEmail("requester-3@example.com")
                .content("일반 소식은 사이트맵에서 제외됩니다.")
                .publishStartAt(today.minusDays(1))
                .publishEndAt(today.plusDays(1))
                .carousel(NewsCarousel.GENERAL)
                .build());
        newsRepository.save(News.builder()
                .title("만료된 프로모션 소식")
                .requesterEmail("requester-4@example.com")
                .content("만료된 프로모션은 사이트맵에서 제외됩니다.")
                .publishStartAt(today.minusDays(3))
                .publishEndAt(today.minusDays(1))
                .carousel(NewsCarousel.PROMOTION)
                .build());
        newsRepository.save(News.builder()
                .title("미래 프로모션 소식")
                .requesterEmail("requester-5@example.com")
                .content("미래 프로모션은 사이트맵에서 제외됩니다.")
                .publishStartAt(today.plusDays(1))
                .publishEndAt(today.plusDays(3))
                .carousel(NewsCarousel.PROMOTION)
                .build());

        List<Map<String, Object>> items = given()
                .when()
                .get("/api/v1/news/sitemap")
                .then()
                .statusCode(200)
                .body("result.pageSize", equalTo(1000))
                .body("result.hasNext", equalTo(false))
                .extract()
                .path("result.items");

        assertThat(items).hasSize(2);
        assertThat(items.get(0).keySet()).containsExactly("id", "updatedAt");
        assertThat(items).extracting(item -> ((Number) item.get("id")).longValue())
                .containsExactly(newerPromotion.getId(), olderPromotion.getId());

        given()
                .queryParam("limit", 1)
                .when()
                .get("/api/v1/news/sitemap")
                .then()
                .statusCode(200)
                .body("result.items.size()", equalTo(1))
                .body("result.pageSize", equalTo(1))
                .body("result.hasNext", equalTo(true))
                .body("result.nextCursor", equalTo(newerPromotion.getId().intValue()));

        given()
                .queryParam("limit", 999999)
                .when()
                .get("/api/v1/news/sitemap")
                .then()
                .statusCode(200)
                .body("result.pageSize", equalTo(5000));

        given()
                .queryParam("limit", 0)
                .when()
                .get("/api/v1/news/sitemap")
                .then()
                .statusCode(400);
    }

    @Test
    void reportCreateListValidationAndBusinessFailuresSucceed() {
        TestUser reporter = createUser();
        TestUser target = createUser();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(reporter))
                .body(Map.of(
                        "targetType", "MEMBER",
                        "targetId", target.nickName(),
                        "reason", "INSULT",
                        "content", "부적절한 표현이 있습니다."
                ))
                .when()
                .post("/api/v1/reports")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));

        given().cookie(accessTokenCookie(reporter))
                .when().get("/api/v1/reports/me")
                .then().statusCode(200)
                .body("result.reports[0].targetId", equalTo(target.nickName()))
                .body("result.reports[0].targetSummary", equalTo(target.nickName()));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(reporter))
                .body(Map.of("targetType", "MEMBER", "targetId", reporter.nickName(), "reason", "GENERAL"))
                .when()
                .post("/api/v1/reports")
                .then()
                .statusCode(400);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(reporter))
                .body(Map.of("targetType", "MEMBER", "targetId", target.nickName()))
                .when()
                .post("/api/v1/reports")
                .then()
                .statusCode(400);

        given().when().get("/api/v1/reports/me")
                .then().statusCode(401);

        assertThat(reportRepository.findAll()).hasSize(1);
    }

    @Test
    void notificationListPreviewReadAndSettingsSucceed() {
        TestUser receiver = createUser();
        TestUser sender = createUser();
        notificationSettingRepository.save(NotificationSetting.builder()
                .memberId(receiver.id())
                .build());
        Notification notification = notificationRepository.save(Notification.builder()
                .notificationType(NotificationType.FOLLOW)
                .sourceId(100L)
                .receiverId(receiver.id())
                .senderId(sender.id())
                .build());

        given().cookie(accessTokenCookie(receiver))
                .when().get("/api/v1/notifications")
                .then().statusCode(200)
                .body("result.notifications[0].notificationId", equalTo(notification.getId().intValue()));
        given().cookie(accessTokenCookie(receiver))
                .when().get("/api/v1/notifications/preview")
                .then().statusCode(200)
                .body("result.notifications[0].read", equalTo(false));
        given().cookie(accessTokenCookie(receiver))
                .when().patch("/api/v1/notifications/{notificationId}/read", notification.getId())
                .then().statusCode(200);
        given().cookie(accessTokenCookie(receiver))
                .when().get("/api/v1/notifications/settings")
                .then().statusCode(200)
                .body("result.newFollower", equalTo(true));
        given().cookie(accessTokenCookie(receiver))
                .when().patch("/api/v1/notifications/settings/{settingType}", "NEW_FOLLOWER")
                .then().statusCode(200);
        given().cookie(accessTokenCookie(receiver))
                .when().patch("/api/v1/notifications/settings/{settingType}", "UNKNOWN")
                .then().statusCode(400);

        given().when().get("/api/v1/notifications")
                .then().statusCode(401);
    }

    @Test
    void imageUploadUrlValidatesAuthenticationProfileAndFileType() {
        TestUser user = createUser();
        TestUser incomplete = createIncompleteUser();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of("originalFileName", "profile.png", "contentType", "image/png"))
                .when()
                .post("/api/v1/image/{type}/upload-url", "PROFILE")
                .then()
                .statusCode(200)
                .body("result.imageUrl", equalTo("https://cdn.example.com/profile.png"));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of("originalFileName", "profile.txt", "contentType", "text/plain"))
                .when()
                .post("/api/v1/image/{type}/upload-url", "PROFILE")
                .then()
                .statusCode(400);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("originalFileName", "profile.png", "contentType", "image/png"))
                .when()
                .post("/api/v1/image/{type}/upload-url", "PROFILE")
                .then()
                .statusCode(401);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(incomplete))
                .body(Map.of("originalFileName", "profile.png", "contentType", "image/png"))
                .when()
                .post("/api/v1/image/{type}/upload-url", "PROFILE")
                .then()
                .statusCode(200)
                .body("result.imageUrl", equalTo("https://cdn.example.com/profile.png"));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(incomplete))
                .body(Map.of("originalFileName", "club.png", "contentType", "image/png"))
                .when()
                .post("/api/v1/image/{type}/upload-url", "CLUB")
                .then()
                .statusCode(403);
    }

    private DetailInfo bookDetail(String isbn) {
        return DetailInfo.builder()
                .isbn(isbn)
                .title("테스트 책 " + isbn.substring(isbn.length() - 4))
                .author("테스트 저자")
                .imgUrl("https://example.com/book.png")
                .publisher("테스트 출판사")
                .description("테스트 설명")
                .link("https://example.com/books/" + isbn)
                .build();
    }
}
