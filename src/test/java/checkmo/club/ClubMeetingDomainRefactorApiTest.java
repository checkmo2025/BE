package checkmo.club;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.Matchers.equalTo;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.repository.ClubMemberRepository;
import checkmo.clubManagement.internal.repository.ClubRepository;
import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.clubMeeting.internal.entity.ClubMemberTeam;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.entity.TeamTopic;
import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.internal.repository.BookReviewRepository;
import checkmo.clubMeeting.internal.repository.ClubMemberTeamRepository;
import checkmo.clubMeeting.internal.repository.MeetingRepository;
import checkmo.clubMeeting.internal.repository.TeamRepository;
import checkmo.clubMeeting.internal.repository.TeamTopicRepository;
import checkmo.clubMeeting.internal.repository.TopicRepository;
import checkmo.support.ApiTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ClubMeetingDomainRefactorApiTest extends ApiTestSupport {

    @Autowired
    ClubRepository clubRepository;

    @Autowired
    ClubMemberRepository clubMemberRepository;

    @Autowired
    MeetingRepository meetingRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    ClubMemberTeamRepository clubMemberTeamRepository;

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    TeamTopicRepository teamTopicRepository;

    @Autowired
    BookReviewRepository bookReviewRepository;

    @Test
    void 팀_재구성_HTTP는_팀_ID를_유지하고_자식_행을_교체한_뒤_빈_요청으로_모두_제거한다() {
        TestUser owner = createUser();
        TestUser firstMember = createUser();
        TestUser secondMember = createUser();
        Club club = createClub(owner);
        joinClub(firstMember, club.getId());
        joinClub(secondMember, club.getId());
        Meeting meeting = createMeeting(owner, club.getId());
        ClubMember ownerMembership = membershipOf(club, owner);
        ClubMember firstMembership = membershipOf(club, firstMember);
        ClubMember secondMembership = membershipOf(club, secondMember);

        manageTeams(owner, club.getId(), meeting.getId(), List.of(
                teamPayload(1, List.of(ownerMembership.getId()))
        ));
        Team retainedTeam = teamRepository.findByMeetingIdAndTeamNumber(meeting.getId(), 1).orElseThrow();
        Long retainedTeamId = retainedTeam.getId();
        List<Long> replacedMemberRowIds = clubMemberTeamRepository.findAllByTeamIds(List.of(retainedTeamId)).stream()
                .map(ClubMemberTeam::getId)
                .toList();

        manageTeams(owner, club.getId(), meeting.getId(), List.of(
                teamPayload(1, List.of(firstMembership.getId(), secondMembership.getId()))
        ));

        Team reloadedRetainedTeam = teamRepository.findByMeetingIdAndTeamNumber(meeting.getId(), 1).orElseThrow();
        List<ClubMemberTeam> replacementRows = clubMemberTeamRepository.findAllByTeamIds(List.of(retainedTeamId));
        assertSoftly(softly -> {
            softly.assertThat(reloadedRetainedTeam.getId()).isEqualTo(retainedTeamId);
            softly.assertThat(replacementRows)
                    .extracting(ClubMemberTeam::getClubMemberId)
                    .containsExactlyInAnyOrder(firstMembership.getId(), secondMembership.getId());
            softly.assertThat(replacedMemberRowIds)
                    .allSatisfy(rowId -> softly.assertThat(clubMemberTeamRepository.findById(rowId)).isEmpty());
        });

        Topic topic = createTopic(owner, club.getId(), meeting.getId(), "삭제될 팀의 발제");
        TeamTopic teamTopic = TeamTopic.builder()
                .team(reloadedRetainedTeam)
                .topic(topic)
                .build();
        teamTopicRepository.saveAndFlush(teamTopic);

        manageTeams(owner, club.getId(), meeting.getId(), List.of());

        assertSoftly(softly -> {
            softly.assertThat(teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(meeting.getId())).isEmpty();
            softly.assertThat(clubMemberTeamRepository.count()).isZero();
            softly.assertThat(teamTopicRepository.count()).isZero();
        });
    }

    @Test
    void 한줄평_HTTP는_생성_수정_삭제마다_미팅_별점_합계와_행을_함께_갱신한다() {
        TestUser owner = createUser();
        Club club = createClub(owner);
        Meeting meeting = createMeeting(owner, club.getId());

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reviewPayload("좋았습니다", 4.5))
                .when().post("/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews", club.getId(), meeting.getId())
                .then().statusCode(200);

        BookReview review = bookReviewRepository.findAll().getFirst();
        assertThat(meetingRepository.findById(meeting.getId()).orElseThrow().getSumRate()).isEqualTo(4.5);

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reviewPayload("더 좋았습니다", 5.0))
                .when().patch(
                        "/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews/{reviewId}",
                        club.getId(), meeting.getId(), review.getId()
                )
                .then().statusCode(200);

        assertThat(meetingRepository.findById(meeting.getId()).orElseThrow().getSumRate()).isEqualTo(5.0);

        given().cookie(accessTokenCookie(owner))
                .when().delete(
                        "/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews/{reviewId}",
                        club.getId(), meeting.getId(), review.getId()
                )
                .then().statusCode(200);

        assertSoftly(softly -> {
            softly.assertThat(meetingRepository.findById(meeting.getId()).orElseThrow().getSumRate()).isZero();
            softly.assertThat(bookReviewRepository.findById(review.getId())).isEmpty();
        });
    }

    @Test
    void 콘텐츠_HTTP는_활동_여부와_작성자_권한을_구분하고_운영진에게_타인_콘텐츠_관리를_허용한다() {
        TestUser owner = createUser();
        TestUser author = createUser();
        TestUser anotherActiveMember = createUser();
        TestUser inactiveMember = createUser();
        Club club = createClub(owner);
        joinClub(author, club.getId());
        joinClub(anotherActiveMember, club.getId());
        joinClub(inactiveMember, club.getId());
        given().cookie(accessTokenCookie(inactiveMember))
                .when().delete("/api/v1/clubs/{clubId}/leave", club.getId())
                .then().statusCode(200);
        Meeting meeting = createMeeting(owner, club.getId());
        Topic topic = createTopic(author, club.getId(), meeting.getId(), "작성자의 발제");
        BookReview review = createReview(author, club.getId(), meeting.getId(), "작성자의 한줄평", 4.0);

        assertForbiddenContentRequests(
                anotherActiveMember,
                club.getId(),
                meeting.getId(),
                topic.getId(),
                review.getId(),
                "TOPIC_403",
                "BOOK_REVIEW_403"
        );
        assertContentUnchanged(
                meeting.getId(), topic.getId(), review.getId(), "작성자의 발제", "작성자의 한줄평", 4.0
        );

        assertForbiddenContentRequests(
                inactiveMember,
                club.getId(),
                meeting.getId(),
                topic.getId(),
                review.getId(),
                "CLUB_MEETING_404",
                "CLUB_MEETING_404"
        );
        assertContentUnchanged(
                meeting.getId(), topic.getId(), review.getId(), "작성자의 발제", "작성자의 한줄평", 4.0
        );

        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("description", "운영진이 수정한 발제"))
                .when().patch(
                        "/api/v1/clubs/{clubId}/bookshelves/{meetingId}/topics/{topicId}",
                        club.getId(), meeting.getId(), topic.getId()
                )
                .then().statusCode(200);
        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reviewPayload("운영진이 수정한 한줄평", 5.0))
                .when().patch(
                        "/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews/{reviewId}",
                        club.getId(), meeting.getId(), review.getId()
                )
                .then().statusCode(200);

        assertContentUnchanged(
                meeting.getId(), topic.getId(), review.getId(),
                "운영진이 수정한 발제", "운영진이 수정한 한줄평", 5.0
        );

        given().cookie(accessTokenCookie(owner))
                .when().delete(
                        "/api/v1/clubs/{clubId}/bookshelves/{meetingId}/topics/{topicId}",
                        club.getId(), meeting.getId(), topic.getId()
                )
                .then().statusCode(200);
        given().cookie(accessTokenCookie(owner))
                .when().delete(
                        "/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews/{reviewId}",
                        club.getId(), meeting.getId(), review.getId()
                )
                .then().statusCode(200);

        assertSoftly(softly -> {
            softly.assertThat(topicRepository.findById(topic.getId())).isEmpty();
            softly.assertThat(bookReviewRepository.findById(review.getId())).isEmpty();
            softly.assertThat(meetingRepository.findById(meeting.getId()).orElseThrow().getSumRate()).isZero();
        });
    }

    private void assertForbiddenContentRequests(
            TestUser actor,
            Long clubId,
            Long meetingId,
            Long topicId,
            Long reviewId,
            String expectedTopicCode,
            String expectedReviewCode
    ) {
        given().cookie(accessTokenCookie(actor))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("description", "권한 없는 발제 수정"))
                .when().patch(
                        "/api/v1/clubs/{clubId}/bookshelves/{meetingId}/topics/{topicId}",
                        clubId, meetingId, topicId
                )
                .then().statusCode(403).body("code", equalTo(expectedTopicCode));
        given().cookie(accessTokenCookie(actor))
                .when().delete(
                        "/api/v1/clubs/{clubId}/bookshelves/{meetingId}/topics/{topicId}",
                        clubId, meetingId, topicId
                )
                .then().statusCode(403).body("code", equalTo(expectedTopicCode));
        given().cookie(accessTokenCookie(actor))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reviewPayload("권한 없는 한줄평 수정", 1.0))
                .when().patch(
                        "/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews/{reviewId}",
                        clubId, meetingId, reviewId
                )
                .then().statusCode(403).body("code", equalTo(expectedReviewCode));
        given().cookie(accessTokenCookie(actor))
                .when().delete(
                        "/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews/{reviewId}",
                        clubId, meetingId, reviewId
                )
                .then().statusCode(403).body("code", equalTo(expectedReviewCode));
    }

    private void assertContentUnchanged(
            Long meetingId,
            Long topicId,
            Long reviewId,
            String topicDescription,
            String reviewDescription,
            double expectedRate
    ) {
        assertSoftly(softly -> {
            Meeting reloadedMeeting = meetingRepository.findById(meetingId).orElseThrow();
            Topic reloadedTopic = topicRepository.findById(topicId).orElseThrow();
            BookReview reloadedReview = bookReviewRepository.findById(reviewId).orElseThrow();
            softly.assertThat(reloadedTopic.getDescription()).isEqualTo(topicDescription);
            softly.assertThat(reloadedReview.getDescription()).isEqualTo(reviewDescription);
            softly.assertThat(reloadedReview.getRate()).isEqualTo(expectedRate);
            softly.assertThat(reloadedMeeting.getSumRate()).isEqualTo(expectedRate);
        });
    }

    private Club createClub(TestUser owner) {
        String name = "domain-refactor-" + UUID.randomUUID().toString().substring(0, 8);
        given().cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "name", name,
                        "description", "도메인 리팩토링 API 테스트",
                        "open", true,
                        "region", "서울",
                        "category", List.of("COMPUTER_IT"),
                        "participantTypes", List.of("ONLINE")
                ))
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

    private ClubMember membershipOf(Club club, TestUser user) {
        return clubMemberRepository.findByClubIdAndMemberId(club.getId(), Long.valueOf(user.id())).orElseThrow();
    }

    private Meeting createMeeting(TestUser owner, Long clubId) {
        String title = "회귀-" + UUID.randomUUID().toString().substring(0, 6);
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

    private Topic createTopic(TestUser author, Long clubId, Long meetingId, String description) {
        given().cookie(accessTokenCookie(author))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("description", description))
                .when().post("/api/v1/clubs/{clubId}/bookshelves/{meetingId}/topics", clubId, meetingId)
                .then().statusCode(200);
        return topicRepository.findAllByMeetingIdOrderByIdDesc(meetingId).getFirst();
    }

    private BookReview createReview(TestUser author, Long clubId, Long meetingId, String description, double rate) {
        given().cookie(accessTokenCookie(author))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reviewPayload(description, rate))
                .when().post("/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews", clubId, meetingId)
                .then().statusCode(200);
        return bookReviewRepository.findAll().getFirst();
    }

    private void manageTeams(TestUser staff, Long clubId, Long meetingId, List<Map<String, Object>> teams) {
        given().cookie(accessTokenCookie(staff))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("teamMemberList", teams))
                .when().put("/api/v1/clubs/{clubId}/meetings/{meetingId}/teams", clubId, meetingId)
                .then().statusCode(200);
    }

    private Map<String, Object> teamPayload(Integer teamNumber, List<Long> clubMemberIds) {
        return Map.of("teamNumber", teamNumber, "clubMemberIds", clubMemberIds);
    }

    private Map<String, Object> reviewPayload(String description, double rate) {
        return Map.of("description", description, "rate", rate);
    }
}
