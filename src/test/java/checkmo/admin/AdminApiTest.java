package checkmo.admin;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;

import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.BookStoryStatus;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.internal.repository.BookStoryRepository;
import checkmo.bookStory.internal.repository.CommentRepository;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubContact;
import checkmo.clubManagement.internal.entity.ClubInterestCategory;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.entity.ClubParticipantType;
import checkmo.clubManagement.internal.repository.ClubMemberRepository;
import checkmo.clubManagement.internal.repository.ClubRepository;
import checkmo.news.internal.repository.NewsRepository;
import checkmo.report.internal.entity.Report;
import checkmo.report.internal.entity.ReportReason;
import checkmo.report.internal.entity.ReportTargetType;
import checkmo.report.internal.repository.ReportRepository;
import checkmo.support.ApiTestSupport;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class AdminApiTest extends ApiTestSupport {

    private static final String ISBN = "9791169213882";

    @Autowired
    ClubRepository clubRepository;

    @Autowired
    ClubMemberRepository clubMemberRepository;

    @Autowired
    BookStoryRepository bookStoryRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    ReportRepository reportRepository;

    @Test
    void memberAdminEndpointsRequireAdminAndReturnMemberData() {
        TestUser admin = createAdmin();
        TestUser user = createUser();
        TestUser nonAdmin = createUser();

        given().cookie(accessTokenCookie(admin))
                .queryParam("keyword", user.email().substring(0, 8))
                .queryParam("limit", 10)
                .when().get("/api/v1/admin/members/emails")
                .then().statusCode(200)
                .body("result.emails.size()", greaterThanOrEqualTo(1));

        given().cookie(accessTokenCookie(admin))
                .queryParam("keyword", user.email())
                .when().get("/api/v1/admin/members")
                .then().statusCode(200)
                .body("result.memberList.size()", greaterThanOrEqualTo(1));

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/members/{memberNickName}", user.nickName())
                .then().statusCode(200)
                .body("result.email", equalTo(user.email()));

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/members/{memberNickName}", "unknown-member")
                .then().statusCode(404);

        given().cookie(accessTokenCookie(nonAdmin))
                .when().get("/api/v1/admin/members")
                .then().statusCode(403);

        given().when().get("/api/v1/admin/members")
                .then().statusCode(401);
    }

    @Test
    void memberReportAdminEndpointReturnsReporterReportsWithCursorAndRequiresAdmin() {
        TestUser admin = createAdmin();
        TestUser reporter = createUser();
        TestUser target = createUser();
        TestUser otherReporter = createUser();
        TestUser nonAdmin = createUser();

        for (int index = 0; index < 21; index++) {
            reportRepository.save(Report.builder()
                    .reporterId(reporter.id())
                    .reportTargetType(ReportTargetType.MEMBER)
                    .targetId(target.nickName())
                    .reportReason(ReportReason.GENERAL)
                    .content("관리자 신고 조회 " + index)
                    .redirectUrl("/profile/" + target.nickName())
                    .build());
        }
        reportRepository.save(Report.builder()
                .reporterId(otherReporter.id())
                .reportTargetType(ReportTargetType.MEMBER)
                .targetId(target.nickName())
                .reportReason(ReportReason.SPAM)
                .content("다른 회원 신고")
                .redirectUrl("/profile/" + target.nickName())
                .build());

        Response firstPage = given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/members/{nickname}/reports", reporter.nickName());

        firstPage.then().statusCode(200)
                .body("result.reports.size()", equalTo(20))
                .body("result.reports[0].content", equalTo("관리자 신고 조회 20"))
                .body("result.reports[0].reason", equalTo("GENERAL"))
                .body("result.reports[0].targetLabel", equalTo(target.nickName()))
                .body("result.reports[0].targetAvailable", equalTo(true))
                .body("result.reports[0].targetUrl", equalTo("/profile/" + target.nickName()))
                .body("result.hasNext", equalTo(true));

        Number nextCursor = firstPage.path("result.nextCursor");
        given().cookie(accessTokenCookie(admin))
                .queryParam("cursorId", nextCursor.longValue())
                .when().get("/api/v1/admin/members/{nickname}/reports", reporter.nickName())
                .then().statusCode(200)
                .body("result.reports.size()", equalTo(1))
                .body("result.reports[0].content", equalTo("관리자 신고 조회 0"))
                .body("result.hasNext", equalTo(false))
                .body("result.nextCursor", nullValue());

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/members/{nickname}/reports", "unknown-member")
                .then().statusCode(404);

        given().cookie(accessTokenCookie(nonAdmin))
                .when().get("/api/v1/admin/members/{nickname}/reports", reporter.nickName())
                .then().statusCode(403);

        given().when().get("/api/v1/admin/members/{nickname}/reports", reporter.nickName())
                .then().statusCode(401);
    }

    @Test
    void memberReportAdminEndpointKeepsReportWhenTargetIsUnavailable() {
        TestUser admin = createAdmin();
        TestUser reporter = createUser();

        reportRepository.save(Report.builder()
                .reporterId(reporter.id())
                .reportTargetType(ReportTargetType.BOOK_STORY)
                .targetId("999999")
                .reportReason(ReportReason.INSULT)
                .content(null)
                .redirectUrl("/stories/999999")
                .build());

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/members/{nickname}/reports", reporter.nickName())
                .then().statusCode(200)
                .body("result.reports.size()", equalTo(1))
                .body("result.reports[0].content", nullValue())
                .body("result.reports[0].targetType", equalTo("BOOK_STORY"))
                .body("result.reports[0].targetId", equalTo("999999"))
                .body("result.reports[0].targetLabel", equalTo("삭제되었거나 확인할 수 없는 대상"))
                .body("result.reports[0].targetAvailable", equalTo(false))
                .body("result.reports[0].targetUrl", nullValue());
    }

    @Test
    void clubAdminEndpointsCoverListDetailUpdateMembersAndSecurity() {
        TestUser admin = createAdmin();
        TestUser owner = createUser();
        TestUser nonAdmin = createUser();
        Club club = createClub(owner, "admin-club-" + owner.legacyId().substring(owner.legacyId().length() - 4).toLowerCase());

        given().cookie(accessTokenCookie(admin))
                .queryParam("keyword", "admin-club")
                .queryParam("page", 1)
                .when().get("/api/v1/admin/clubs")
                .then().statusCode(200)
                .body("result.clubs.size()", greaterThanOrEqualTo(1));

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/clubs/{clubId}", club.getId())
                .then().statusCode(200)
                .body("result.name", equalTo(club.getName()));

        given().cookie(accessTokenCookie(admin))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(clubPayload(club.getName() + "-edit"))
                .when().put("/api/v1/admin/clubs/{clubId}", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/clubs/{clubId}/active-members", club.getId())
                .then().statusCode(200)
                .body("result.members.size()", equalTo(1));

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/clubs/members/{memberNickname}", owner.nickName())
                .then().statusCode(200)
                .body("result.clubList.size()", equalTo(1));

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/clubs/{clubId}", 999999)
                .then().statusCode(404);

        given().cookie(accessTokenCookie(admin))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("name", ""))
                .when().put("/api/v1/admin/clubs/{clubId}", club.getId())
                .then().statusCode(400);

        given().cookie(accessTokenCookie(nonAdmin))
                .when().get("/api/v1/admin/clubs")
                .then().statusCode(403);

        given().when().get("/api/v1/admin/clubs")
                .then().statusCode(401);
    }

    @Test
    void newsAdminEndpointsCoverCrudMemberLookupValidationAndSecurity() {
        TestUser admin = createAdmin();
        TestUser requester = createUser();
        TestUser nonAdmin = createUser();

        Integer newsId = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(admin))
                .body(newsPayload("관리자 소식", requester.email()))
                .when().post("/api/v1/admin/news")
                .then().statusCode(200)
                .extract().path("result");

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/news")
                .then().statusCode(200)
                .body("result.basicInfoList.size()", greaterThanOrEqualTo(1));

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/news/{newsId}", newsId)
                .then().statusCode(200)
                .body("result.title", equalTo("관리자 소식"));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(admin))
                .body(newsPayload("수정된 소식", requester.email()))
                .when().patch("/api/v1/admin/news/{newsId}", newsId)
                .then().statusCode(200);

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/news/members/{memberNickname}", requester.nickName())
                .then().statusCode(200)
                .body("result.basicInfoList.size()", greaterThanOrEqualTo(1));

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(admin))
                .body(Map.of("title", ""))
                .when().post("/api/v1/admin/news")
                .then().statusCode(400);

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/news/{newsId}", 999999)
                .then().statusCode(404);

        given().cookie(accessTokenCookie(nonAdmin))
                .when().get("/api/v1/admin/news")
                .then().statusCode(403);

        given().when().get("/api/v1/admin/news")
                .then().statusCode(401);

        given().cookie(accessTokenCookie(admin))
                .when().delete("/api/v1/admin/news/{newsId}", newsId)
                .then().statusCode(200);

        assertThat(newsRepository.findById(newsId.longValue())).isEmpty();
    }

    @Test
    void bookStoryAdminEndpointsCoverListDetailDeleteCommentMemberLookupAndSecurity() {
        TestUser admin = createAdmin();
        TestUser author = createUser();
        TestUser nonAdmin = createUser();
        BookStory story = bookStoryRepository.save(BookStory.builder()
                .memberId(Long.valueOf(author.id()))
                .bookId(ISBN)
                .title("관리자 책 이야기")
                .description("관리자 테스트용 책 이야기입니다.")
                .status(BookStoryStatus.PUBLISHED)
                .build());
        Comment comment = commentRepository.save(Comment.builder()
                .memberId(Long.valueOf(author.id()))
                .bookStory(story)
                .content("관리자 삭제 대상 댓글")
                .build());

        given().cookie(accessTokenCookie(admin))
                .queryParam("keyword", "관리자")
                .when().get("/api/v1/admin/book-stories")
                .then().statusCode(200)
                .body("result.basicInfoList.size()", greaterThanOrEqualTo(1));

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/book-stories/{bookStoryId}", story.getId())
                .then().statusCode(200)
                .body("result.bookStoryId", equalTo(story.getId().intValue()));

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/book-stories/members/{memberNickname}", author.nickName())
                .then().statusCode(200)
                .body("result.basicInfoList.size()", greaterThanOrEqualTo(1));

        given().cookie(accessTokenCookie(admin))
                .when().delete("/api/v1/admin/book-stories/{bookStoryId}/comments/{commentId}", story.getId(), comment.getId())
                .then().statusCode(200)
                .body("result", equalTo(comment.getId().intValue()));
        assertThat(commentRepository.findById(comment.getId()).orElseThrow().isDeleted()).isTrue();

        given().cookie(accessTokenCookie(admin))
                .when().delete("/api/v1/admin/book-stories/{bookStoryId}", story.getId())
                .then().statusCode(200);
        assertThat(bookStoryRepository.findById(story.getId())).isEmpty();

        given().cookie(accessTokenCookie(admin))
                .when().get("/api/v1/admin/book-stories/{bookStoryId}", 999999)
                .then().statusCode(404);

        given().cookie(accessTokenCookie(nonAdmin))
                .when().get("/api/v1/admin/book-stories")
                .then().statusCode(403);

        given().when().get("/api/v1/admin/book-stories")
                .then().statusCode(401);
    }

    private Club createClub(TestUser owner, String clubName) {
        Club club = Club.builder()
                .name(clubName)
                .description("관리자 테스트 클럽")
                .profileImgUrl("https://example.com/club.png")
                .isOpen(true)
                .region("서울")
                .participantTypes(new ArrayList<>(List.of(ClubParticipantType.ONLINE)))
                .interestCategories(new HashSet<>(Set.of(ClubInterestCategory.COMPUTER_IT)))
                .links(new ArrayList<>(List.of(ClubContact.builder()
                        .link("https://example.com")
                        .label("홈")
                        .build())))
                .build();
        Club saved = clubRepository.save(club);
        ClubMember clubOwner = saved.createOwnerMember(Long.valueOf(owner.id()), LocalDateTime.now());
        clubMemberRepository.save(clubOwner);
        return saved;
    }

    private Map<String, Object> clubPayload(String name) {
        return Map.of(
                "name", name,
                "description", "관리자가 수정한 클럽 설명",
                "profileImageUrl", "https://example.com/club-edit.png",
                "open", true,
                "region", "서울",
                "category", List.of("COMPUTER_IT"),
                "participantTypes", List.of("ONLINE"),
                "links", List.of(Map.of("link", "https://example.com/edit", "label", "수정"))
        );
    }

    private Map<String, Object> newsPayload(String title, String requesterEmail) {
        return Map.of(
                "title", title,
                "requesterEmail", requesterEmail,
                "content", "관리자 소식 본문입니다.",
                "thumbnailUrl", "https://example.com/news.png",
                "originalLink", "https://example.com/news",
                "publishStartAt", LocalDate.now().minusDays(1).toString(),
                "publishEndAt", LocalDate.now().plusDays(1).toString(),
                "carousel", "GENERAL",
                "imageUrls", List.of("https://example.com/news-1.png")
        );
    }
}
