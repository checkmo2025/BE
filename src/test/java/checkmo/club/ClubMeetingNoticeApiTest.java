package checkmo.club;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.repository.ClubMemberRepository;
import checkmo.clubManagement.internal.repository.ClubRepository;
import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.internal.repository.BookReviewRepository;
import checkmo.clubMeeting.internal.repository.MeetingRepository;
import checkmo.clubMeeting.internal.repository.TeamRepository;
import checkmo.clubMeeting.internal.repository.TopicRepository;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.repository.NoticeCommentRepository;
import checkmo.clubNotice.internal.repository.NoticeRepository;
import checkmo.realtime.internal.entity.TeamChatMessage;
import checkmo.realtime.internal.repository.TeamChatMessageRepository;
import checkmo.support.ApiTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ClubMeetingNoticeApiTest extends ApiTestSupport {

    @Autowired
    ClubRepository clubRepository;

    @Autowired
    ClubMemberRepository clubMemberRepository;

    @Autowired
    MeetingRepository meetingRepository;

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    BookReviewRepository bookReviewRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    NoticeRepository noticeRepository;

    @Autowired
    NoticeCommentRepository noticeCommentRepository;

    @Autowired
    TeamChatMessageRepository teamChatMessageRepository;

    @Test
    void clubManagementFlowCoversClubEndpoints() {
        TestUser owner = createUser();
        TestUser member = createUser();
        Club club = createClub(owner, "club" + uniqueSuffix(owner));

        given().cookie(accessTokenCookie(owner))
                .queryParam("clubName", club.getName())
                .when().get("/api/clubs/check-name")
                .then().statusCode(200).body("result", equalTo(true));

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/search")
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/recommendations")
                .then().statusCode(200);

        given().when().get("/api/clubs/{clubId}/home", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(member))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("joinMessage", "함께 읽고 싶습니다."))
                .when().post("/api/clubs/{clubId}/join", club.getId())
                .then().statusCode(200);

        ClubMember joinedMember = clubMemberRepository.findByClubIdAndMemberId(club.getId(), member.id()).orElseThrow();

        given().cookie(accessTokenCookie(owner))
                .queryParam("status", "ACTIVE")
                .when().get("/api/clubs/{clubId}/members", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("command", "CHANGE_ROLE", "status", "STAFF"))
                .when().patch("/api/clubs/{clubId}/members/{clubMemberId}", club.getId(), joinedMember.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(member))
                .when().get("/api/clubs/{clubId}/me", club.getId())
                .then().statusCode(200);

        given().queryParam("memberNickname", member.nickName())
                .when().get("/api/clubs")
                .then().statusCode(200);

        given().cookie(accessTokenCookie(member))
                .when().get("/api/me/clubs")
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(clubDetailPayload(club.getName() + "-edit"))
                .when().put("/api/clubs/{clubId}", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/clubs/{clubId}/leave", club.getId())
                .then().statusCode(403);

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/clubs/{clubId}", club.getId())
                .then().statusCode(200);
    }

    @Test
    void bookshelfMeetingTopicReviewAndChatFlowCoversRestEndpoints() {
        TestUser owner = createUser();
        Club club = createClub(owner, "bookshelf" + uniqueSuffix(owner));
        Meeting meeting = createMeeting(owner, club.getId());
        ClubMember ownerClubMember = clubMemberRepository.findByClubIdAndMemberId(club.getId(), owner.id()).orElseThrow();

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}/bookshelves", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}/bookshelves/{meetingId}", club.getId(), meeting.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}/bookshelves/{meetingId}/edit", club.getId(), meeting.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "title", "수정책장",
                        "meetingTime", LocalDateTime.now().plusDays(2).toString(),
                        "location", "온라인",
                        "generation", 2,
                        "tag", "수정"
                ))
                .when().patch("/api/clubs/{clubId}/bookshelves/{meetingId}", club.getId(), meeting.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}/meetings/next", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}/meetings/{meetingId}", club.getId(), meeting.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("description", "첫 번째 발제"))
                .when().post("/api/clubs/{clubId}/bookshelves/{meetingId}/topics", club.getId(), meeting.getId())
                .then().statusCode(200);
        Topic topic = topicRepository.findAllByMeetingIdOrderByIdDesc(meeting.getId()).getFirst();

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}/bookshelves/{meetingId}/topics", club.getId(), meeting.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("description", "수정된 발제"))
                .when().patch("/api/clubs/{clubId}/bookshelves/{meetingId}/topics/{topicId}", club.getId(), meeting.getId(), topic.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("description", "좋았습니다", "rate", 4.5))
                .when().post("/api/clubs/{clubId}/bookshelves/{meetingId}/reviews", club.getId(), meeting.getId())
                .then().statusCode(200);
        BookReview review = bookReviewRepository.findAll().getFirst();

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}/bookshelves/{meetingId}/reviews", club.getId(), meeting.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("description", "수정 한줄평", "rate", 5.0))
                .when().patch("/api/clubs/{clubId}/bookshelves/{meetingId}/reviews/{reviewId}", club.getId(), meeting.getId(), review.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}/meetings/{meetingId}/members", club.getId(), meeting.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("teamMemberList", List.of(Map.of("teamNumber", 1, "clubMemberIds", List.of(ownerClubMember.getId())))))
                .when().put("/api/clubs/{clubId}/meetings/{meetingId}/teams", club.getId(), meeting.getId())
                .then().statusCode(200);
        Team team = teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(meeting.getId()).getFirst();

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}/meetings/{meetingId}/teams/{teamId}/topics", club.getId(), meeting.getId(), team.getId())
                .then().statusCode(200);

        teamChatMessageRepository.save(TeamChatMessage.builder()
                .clubId(club.getId())
                .meetingId(meeting.getId())
                .teamId(team.getId())
                .senderMemberId(owner.id())
                .content("안녕하세요")
                .sentAt(LocalDateTime.now())
                .build());

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}/meetings/{meetingId}/teams/{teamId}/chat/messages", club.getId(), meeting.getId(), team.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/clubs/{clubId}/bookshelves/{meetingId}/reviews/{reviewId}", club.getId(), meeting.getId(), review.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/clubs/{clubId}/bookshelves/{meetingId}/topics/{topicId}", club.getId(), meeting.getId(), topic.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/clubs/{clubId}/bookshelves/{meetingId}", club.getId(), meeting.getId())
                .then().statusCode(200);
    }

    @Test
    void noticeVoteAndCommentFlowCoversNoticeEndpoints() {
        TestUser owner = createUser();
        Club club = createClub(owner, "notice" + uniqueSuffix(owner));

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(noticePayload())
                .when().post("/api/clubs/{clubId}/notices", club.getId())
                .then().statusCode(200);
        Notice notice = noticeRepository.findTop1ByClubIdOrderByCreatedAtDescIdDesc(club.getId()).orElseThrow();
        Long voteId = noticeRepository.findWithVoteAndClubMemberVotesByIdAndClubId(notice.getId(), club.getId())
                .orElseThrow()
                .getVote()
                .getId();

        given().when().get("/api/clubs/{clubId}/notices/latest", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}/notices", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}/notices/{noticeId}", club.getId(), notice.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("selectedItemNumbers", List.of(1)))
                .when().post("/api/clubs/{clubId}/notices/{noticeId}/votes/{voteId}", club.getId(), notice.getId(), voteId)
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("content", "댓글"))
                .when().post("/api/clubs/{clubId}/notices/{noticeId}/comments", club.getId(), notice.getId())
                .then().statusCode(200);
        Long commentId = noticeCommentRepository.findAll().getFirst().getId();

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/clubs/{clubId}/notices/{noticeId}/comments", club.getId(), notice.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("content", "수정댓글"))
                .when().patch("/api/clubs/{clubId}/notices/{noticeId}/comments/{commentId}", club.getId(), notice.getId(), commentId)
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "title", "수정 공지",
                        "content", "수정 내용",
                        "isPinned", false,
                        "imageUrls", List.of(),
                        "vote", Map.of("deadline", LocalDateTime.now().plusDays(2).toString())
                ))
                .when().patch("/api/clubs/{clubId}/notices/{noticeId}", club.getId(), notice.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/clubs/{clubId}/notices/{noticeId}/comments/{commentId}", club.getId(), notice.getId(), commentId)
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/clubs/{clubId}/notices/{noticeId}", club.getId(), notice.getId())
                .then().statusCode(200);
    }

    @Test
    void nonMemberCannotAccessMemberOnlyClubResources() {
        TestUser owner = createUser();
        TestUser outsider = createUser();
        Club club = createClub(owner, "forbidden" + uniqueSuffix(owner));

        given().cookie(accessTokenCookie(outsider))
                .when().get("/api/clubs/{clubId}/bookshelves", club.getId())
                .then().statusCode(404);
    }

    @Test
    void authenticatedClubEndpointRequiresCookie() {
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(clubDetailPayload("no-auth-club"))
                .when().post("/api/clubs")
                .then().statusCode(401);
    }

    private Club createClub(TestUser owner, String name) {
        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(clubDetailPayload(name))
                .when().post("/api/clubs")
                .then().statusCode(200);
        return clubRepository.findAll().stream()
                .filter(club -> club.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private Meeting createMeeting(TestUser owner, Long clubId) {
        String title = "첫책장-" + uniqueSuffix(owner);
        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "title", title,
                        "meetingTime", LocalDateTime.now().plusDays(1).toString(),
                        "location", "온라인",
                        "generation", 1,
                        "tag", "소설",
                        "isbn", "9781234567890"
                ))
                .when().post("/api/clubs/{clubId}/bookshelves", clubId)
                .then().statusCode(200);
        return meetingRepository.findAllByClubId(clubId).stream()
                .filter(meeting -> meeting.getTitle().equals(title))
                .findFirst()
                .orElseThrow();
    }

    private String uniqueSuffix(TestUser user) {
        return user.id().substring(user.id().length() - 4).toLowerCase();
    }

    private Map<String, Object> clubDetailPayload(String name) {
        return Map.of(
                "name", name,
                "description", "테스트 독서 모임",
                "open", true,
                "region", "서울",
                "category", List.of("COMPUTER_IT"),
                "participantTypes", List.of("ONLINE")
        );
    }

    private Map<String, Object> noticePayload() {
        return Map.of(
                "title", "공지",
                "content", "공지 내용",
                "isPinned", false,
                "imageUrls", List.of(),
                "vote", Map.of(
                        "title", "투표",
                        "content", "투표 내용",
                        "item1", "찬성",
                        "item2", "반대",
                        "anonymity", false,
                        "duplication", false,
                        "startTime", LocalDateTime.now().minusHours(1).toString(),
                        "deadline", LocalDateTime.now().plusDays(1).toString()
                )
        );
    }
}
