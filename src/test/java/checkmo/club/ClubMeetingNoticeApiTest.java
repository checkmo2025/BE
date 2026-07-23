package checkmo.club;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.entity.ClubMemberStatus;
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
import io.restassured.response.Response;
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
    void clubSitemapReturnsOpenClubMetadataOnly() {
        TestUser owner = createUser();
        Club openClub = createClub(owner, "sitemap-open-" + uniqueSuffix(owner));
        Club secondOpenClub = createClub(owner, "sitemap-open-second-" + uniqueSuffix(owner));
        Club closedClub = createClub(owner, "sitemap-closed-" + uniqueSuffix(owner), false);

        Response response = given()
                .when().get("/api/v1/clubs/sitemap")
                .then().statusCode(200)
                .extract().response();

        List<Long> clubIds = response.jsonPath().getList("result.items.id", Long.class);
        assertThat(clubIds).contains(openClub.getId(), secondOpenClub.getId());
        assertThat(clubIds).doesNotContain(closedClub.getId());

        List<Map<String, Object>> items = response.jsonPath().getList("result.items");
        assertThat(items).isNotEmpty();
        assertThat(items).allSatisfy(item ->
                assertThat(item.keySet()).containsExactly("id", "updatedAt")
        );

        given().queryParam("limit", 1)
                .when().get("/api/v1/clubs/sitemap")
                .then().statusCode(200)
                .body("result.pageSize", equalTo(1))
                .body("result.hasNext", equalTo(true))
                .body("result.nextCursor", not(equalTo(null)));

        given().queryParam("limit", 999999)
                .when().get("/api/v1/clubs/sitemap")
                .then().statusCode(200)
                .body("result.pageSize", equalTo(5000));

        given().queryParam("limit", 0)
                .when().get("/api/v1/clubs/sitemap")
                .then().statusCode(400);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/groups/sitemap")
                .then().statusCode(400);
    }

    @Test
    void clubManagementFlowCoversClubEndpoints() {
        TestUser owner = createUser();
        TestUser member = createUser();
        Club club = createClub(owner, "club" + uniqueSuffix(owner));

        given().cookie(accessTokenCookie(owner))
                .queryParam("clubName", club.getName())
                .when().get("/api/v1/clubs/check-name")
                .then().statusCode(200).body("result", equalTo(true));

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/search")
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/recommendations")
                .then().statusCode(200);

        given().when().get("/api/v1/clubs/{clubId}/home", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(member))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("joinMessage", "함께 읽고 싶습니다."))
                .when().post("/api/v1/clubs/{clubId}/join", club.getId())
                .then().statusCode(200);

        ClubMember joinedMember = clubMemberRepository.findByClubIdAndMemberId(
                club.getId(),
                Long.valueOf(member.id())
        ).orElseThrow();

        given().cookie(accessTokenCookie(owner))
                .queryParam("status", "ACTIVE")
                .when().get("/api/v1/clubs/{clubId}/members", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("command", "CHANGE_ROLE", "status", "STAFF"))
                .when().patch("/api/v1/clubs/{clubId}/members/{clubMemberId}", club.getId(), joinedMember.getId())
                .then().statusCode(200);
        assertThat(clubMemberRepository.findById(joinedMember.getId()).orElseThrow().getClubMemberStatus())
                .isEqualTo(ClubMemberStatus.STAFF);

        given().cookie(accessTokenCookie(member))
                .when().get("/api/v1/clubs/{clubId}/me", club.getId())
                .then().statusCode(200);

        given().queryParam("memberNickname", member.nickName())
                .when().get("/api/v1/clubs")
                .then().statusCode(200);

        given().cookie(accessTokenCookie(member))
                .when().get("/api/v1/me/clubs")
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(clubDetailPayload(club.getName() + "-edit"))
                .when().put("/api/v1/clubs/{clubId}", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/v1/clubs/{clubId}/leave", club.getId())
                .then().statusCode(403)
                .body("code", equalTo("CLUB_MEMBER_405"));

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/v1/clubs/{clubId}", club.getId())
                .then().statusCode(200);
    }

    @Test
    void clubRecommendationsReturnSixClubs() {
        TestUser owner = createUser();
        TestUser requester = createUser();

        for (int index = 0; index < 7; index++) {
            createClub(owner, "recommend-" + index + "-" + uniqueSuffix(owner));
        }

        Response response = given().cookie(accessTokenCookie(requester))
                .when().get("/api/v1/clubs/recommendations")
                .then().statusCode(200)
                .extract().response();

        List<Map<String, Object>> recommendations = response.jsonPath().getList("result.recommendations");
        assertThat(recommendations).hasSize(6);
    }

    @Test
    void publicClubParticipantsCanBeViewedByLoggedInNonMemberWithFollowStatus() {
        TestUser owner = createUser();
        TestUser member = createUser();
        TestUser viewer = createUser();
        Club club = createClub(owner, "participants-open-" + uniqueSuffix(owner));
        joinClub(member, club.getId());

        given().cookie(accessTokenCookie(viewer))
                .when().post("/api/v1/members/{memberNickname}/following", member.nickName())
                .then().statusCode(200);

        Response response = given().cookie(accessTokenCookie(viewer))
                .when().get("/api/v1/clubs/{clubId}/participants", club.getId())
                .then().statusCode(200)
                .body("result.totalCount", equalTo(2))
                .body("result.hasNext", equalTo(false))
                .extract().response();

        List<Map<String, Object>> participants = response.jsonPath().getList("result.clubMembers");
        assertThat(participants).hasSize(2);
        assertThat(participants).allSatisfy(participant ->
                assertThat(participant.keySet()).contains(
                        "clubMemberId",
                        "nickname",
                        "profileImageUrl",
                        "following",
                        "clubMemberStatus",
                        "staff"
                )
        );
        assertThat(participants).anySatisfy(participant -> {
            assertThat(participant).containsEntry("nickname", owner.nickName());
            assertThat(participant).containsEntry("following", false);
            assertThat(participant).containsEntry("clubMemberStatus", "OWNER");
            assertThat(participant).containsEntry("staff", true);
        });
        assertThat(participants).anySatisfy(participant -> {
            assertThat(participant).containsEntry("nickname", member.nickName());
            assertThat(participant).containsEntry("following", true);
            assertThat(participant).containsEntry("clubMemberStatus", "MEMBER");
            assertThat(participant).containsEntry("staff", false);
        });

        given().cookie(accessTokenCookie(member))
                .queryParam("status", "ACTIVE")
                .when().get("/api/v1/clubs/{clubId}/members", club.getId())
                .then().statusCode(403);
    }

    @Test
    void privateClubParticipantsRequireActiveClubMember() {
        TestUser owner = createUser();
        TestUser pendingMember = createUser();
        TestUser outsider = createUser();
        Club club = createClub(owner, "participants-private-" + uniqueSuffix(owner), false);
        joinClub(pendingMember, club.getId());

        Response response = given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/participants", club.getId())
                .then().statusCode(200)
                .body("result.totalCount", equalTo(1))
                .extract().response();

        List<Map<String, Object>> participants = response.jsonPath().getList("result.clubMembers");
        assertThat(participants).hasSize(1);
        assertThat(participants.getFirst())
                .containsEntry("nickname", owner.nickName())
                .containsEntry("clubMemberStatus", "OWNER")
                .containsEntry("staff", true);

        assertParticipantsJoinRequired(pendingMember, club.getId());
        assertParticipantsJoinRequired(outsider, club.getId());
    }

    @Test
    void bookshelfMeetingTopicReviewAndChatFlowCoversRestEndpoints() {
        TestUser owner = createUser();
        Club club = createClub(owner, "bookshelf" + uniqueSuffix(owner));
        Meeting meeting = createMeeting(owner, club.getId());
        ClubMember ownerClubMember = clubMemberRepository.findByClubIdAndMemberId(
                club.getId(),
                Long.valueOf(owner.id())
        ).orElseThrow();

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/bookshelves", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/bookshelves/{meetingId}", club.getId(), meeting.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/bookshelves/{meetingId}/edit", club.getId(), meeting.getId())
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
                .when().patch("/api/v1/clubs/{clubId}/bookshelves/{meetingId}", club.getId(), meeting.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/meetings/next", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/meetings/{meetingId}", club.getId(), meeting.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("description", "첫 번째 발제"))
                .when().post("/api/v1/clubs/{clubId}/bookshelves/{meetingId}/topics", club.getId(), meeting.getId())
                .then().statusCode(200);
        Topic topic = topicRepository.findAllByMeetingIdOrderByIdDesc(meeting.getId()).getFirst();

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/bookshelves/{meetingId}/topics", club.getId(), meeting.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("description", "수정된 발제"))
                .when().patch("/api/v1/clubs/{clubId}/bookshelves/{meetingId}/topics/{topicId}", club.getId(), meeting.getId(), topic.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("description", "좋았습니다", "rate", 4.5))
                .when().post("/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews", club.getId(), meeting.getId())
                .then().statusCode(200);
        BookReview review = bookReviewRepository.findAll().getFirst();

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews", club.getId(), meeting.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("description", "수정 한줄평", "rate", 5.0))
                .when().patch("/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews/{reviewId}", club.getId(), meeting.getId(), review.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/meetings/{meetingId}/members", club.getId(), meeting.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("teamMemberList", List.of(Map.of("teamNumber", 1, "clubMemberIds", List.of(ownerClubMember.getId())))))
                .when().put("/api/v1/clubs/{clubId}/meetings/{meetingId}/teams", club.getId(), meeting.getId())
                .then().statusCode(200);
        Team team = teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(meeting.getId()).getFirst();

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/meetings/{meetingId}/teams/{teamId}/topics", club.getId(), meeting.getId(), team.getId())
                .then().statusCode(200);

        teamChatMessageRepository.save(TeamChatMessage.builder()
                .clubId(club.getId())
                .meetingId(meeting.getId())
                .teamId(team.getId())
                .senderMemberId(Long.valueOf(owner.id()))
                .content("안녕하세요")
                .sentAt(LocalDateTime.now())
                .build());

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/meetings/{meetingId}/teams/{teamId}/chat/messages", club.getId(), meeting.getId(), team.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews/{reviewId}", club.getId(), meeting.getId(), review.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/v1/clubs/{clubId}/bookshelves/{meetingId}/topics/{topicId}", club.getId(), meeting.getId(), topic.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/v1/clubs/{clubId}/bookshelves/{meetingId}", club.getId(), meeting.getId())
                .then().statusCode(200);
    }

    @Test
    void noticeVoteAndCommentFlowCoversNoticeEndpoints() {
        TestUser owner = createUser();
        Club club = createClub(owner, "notice" + uniqueSuffix(owner));

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(noticePayload())
                .when().post("/api/v1/clubs/{clubId}/notices", club.getId())
                .then().statusCode(200);
        Notice notice = noticeRepository.findTop1ByClubIdOrderByCreatedAtDescIdDesc(club.getId()).orElseThrow();
        Long voteId = noticeRepository.findWithVoteAndClubMemberVotesByIdAndClubId(notice.getId(), club.getId())
                .orElseThrow()
                .getVote()
                .getId();

        given().when().get("/api/v1/clubs/{clubId}/notices/latest", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/notices", club.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/notices/{noticeId}", club.getId(), notice.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("selectedItemNumbers", List.of(1)))
                .when().post("/api/v1/clubs/{clubId}/notices/{noticeId}/votes/{voteId}", club.getId(), notice.getId(), voteId)
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "content", "댓글",
                        "imageUrls", List.of(
                                "https://example.com/notice-comment-1.jpg",
                                "https://example.com/notice-comment-2.jpg"
                        )
                ))
                .when().post("/api/v1/clubs/{clubId}/notices/{noticeId}/comments", club.getId(), notice.getId())
                .then().statusCode(200);
        Long commentId = noticeCommentRepository.findAll().getFirst().getId();

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/notices/{noticeId}/comments", club.getId(), notice.getId())
                .then()
                .statusCode(200)
                .body(
                        "result.comments[0].imageUrls",
                        equalTo(List.of(
                                "https://example.com/notice-comment-1.jpg",
                                "https://example.com/notice-comment-2.jpg"
                        ))
                );

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("content", "수정댓글"))
                .when().patch("/api/v1/clubs/{clubId}/notices/{noticeId}/comments/{commentId}", club.getId(), notice.getId(), commentId)
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().get("/api/v1/clubs/{clubId}/notices/{noticeId}/comments", club.getId(), notice.getId())
                .then()
                .statusCode(200)
                .body(
                        "result.comments[0].imageUrls",
                        equalTo(List.of(
                                "https://example.com/notice-comment-1.jpg",
                                "https://example.com/notice-comment-2.jpg"
                        ))
                );

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "content", "사진 제거 댓글",
                        "imageUrls", List.of()
                ))
                .when().patch("/api/v1/clubs/{clubId}/notices/{noticeId}/comments/{commentId}", club.getId(), notice.getId(), commentId)
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "content", "사진 제한 초과 댓글",
                        "imageUrls", List.of("1", "2", "3", "4", "5", "6")
                ))
                .when().post("/api/v1/clubs/{clubId}/notices/{noticeId}/comments", club.getId(), notice.getId())
                .then().statusCode(400);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "title", "수정 공지",
                        "content", "수정 내용",
                        "isPinned", false,
                        "imageUrls", List.of(),
                        "vote", Map.of("deadline", LocalDateTime.now().plusDays(2).toString())
                ))
                .when().patch("/api/v1/clubs/{clubId}/notices/{noticeId}", club.getId(), notice.getId())
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/v1/clubs/{clubId}/notices/{noticeId}/comments/{commentId}", club.getId(), notice.getId(), commentId)
                .then().statusCode(200);

        given().cookie(accessTokenCookie(owner))
                .when().delete("/api/v1/clubs/{clubId}/notices/{noticeId}", club.getId(), notice.getId())
                .then().statusCode(200);
    }

    @Test
    void nonMemberCannotAccessMemberOnlyClubResources() {
        TestUser owner = createUser();
        TestUser outsider = createUser();
        Club club = createClub(owner, "forbidden" + uniqueSuffix(owner));

        given().cookie(accessTokenCookie(outsider))
                .when().get("/api/v1/clubs/{clubId}/bookshelves", club.getId())
                .then().statusCode(404);
    }

    @Test
    void authenticatedClubEndpointRequiresCookie() {
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(clubDetailPayload("no-auth-club"))
                .when().post("/api/v1/clubs")
                .then().statusCode(401);
    }

    private Club createClub(TestUser owner, String name) {
        return createClub(owner, name, true);
    }

    private Club createClub(TestUser owner, String name, boolean open) {
        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(clubDetailPayload(name, open))
                .when().post("/api/v1/clubs")
                .then().statusCode(200);
        return clubRepository.findAll().stream()
                .filter(club -> club.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private void joinClub(TestUser user, Long clubId) {
        given().cookie(accessTokenCookie(user))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("joinMessage", "함께 읽고 싶습니다."))
                .when().post("/api/v1/clubs/{clubId}/join", clubId)
                .then().statusCode(200);
    }

    private void assertParticipantsJoinRequired(TestUser user, Long clubId) {
        given().cookie(accessTokenCookie(user))
                .when().get("/api/v1/clubs/{clubId}/participants", clubId)
                .then().statusCode(403)
                .body("message", equalTo("모임 회원은 가입 후에 조회 가능합니다"));
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
                .when().post("/api/v1/clubs/{clubId}/bookshelves", clubId)
                .then().statusCode(200);
        return meetingRepository.findAllByClubId(clubId).stream()
                .filter(meeting -> meeting.getTitle().equals(title))
                .findFirst()
                .orElseThrow();
    }

    private String uniqueSuffix(TestUser user) {
        return user.legacyId().substring(user.legacyId().length() - 4).toLowerCase();
    }

    private Map<String, Object> clubDetailPayload(String name) {
        return clubDetailPayload(name, true);
    }

    private Map<String, Object> clubDetailPayload(String name, boolean open) {
        return Map.of(
                "name", name,
                "description", "테스트 독서 모임",
                "open", open,
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
