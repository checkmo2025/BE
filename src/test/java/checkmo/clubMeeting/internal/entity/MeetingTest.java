package checkmo.clubMeeting.internal.entity;

import static checkmo.clubMeeting.internal.entity.MeetingTeamTestFixture.addTeam;
import static checkmo.clubMeeting.internal.entity.MeetingTeamTestFixture.findTeam;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MeetingTest {

    @Test
    void 요청한_팀번호에_맞춰_기존_팀을_유지하고_새_팀을_추가하며_빠진_팀을_제거한다() {
        Meeting meeting = Meeting.builder().clubId(1L).bookId("book").build();
        Team removed = addTeam(meeting, 1, 10L);
        Team retained = addTeam(meeting, 2, 20L);
        Map<Integer, List<Long>> requestedMembersByTeamNumber = new LinkedHashMap<>();
        requestedMembersByTeamNumber.put(2, List.of(21L, 22L));
        requestedMembersByTeamNumber.put(3, List.of(31L));

        meeting.organizeTeams(requestedMembersByTeamNumber);

        Team created = findTeam(meeting, 3);
        assertSoftly(softly -> {
            softly.assertThat(meeting.getTeams()).containsExactlyInAnyOrder(retained, created);
            softly.assertThat(findTeam(meeting, 2)).isSameAs(retained);
            softly.assertThat(removed.getMeeting()).isNull();
            softly.assertThat(created.getMeeting()).isSameAs(meeting);
            softly.assertThat(retained.getClubMemberTeams())
                    .extracting(ClubMemberTeam::getClubMemberId)
                    .containsExactly(21L, 22L);
            softly.assertThat(created.getClubMemberTeams())
                    .extracting(ClubMemberTeam::getClubMemberId)
                    .containsExactly(31L);
        });
        assertThat(retained.getClubMemberTeams())
                .allSatisfy(member -> assertThat(member.getTeam()).isSameAs(retained));
        assertThat(created.getClubMemberTeams())
                .allSatisfy(member -> assertThat(member.getTeam()).isSameAs(created));
    }

    @Test
    void 연관관계_내부_조작과_컬렉션을_public_API로_노출하지_않는다() {
        assertThat(Meeting.class.getMethods())
                .extracting(Method::getName)
                .doesNotContain(
                        "addTeam",
                        "removeTeam",
                        "getTeams",
                        "getTopics",
                        "getBookReviews",
                        "reviseBookReview",
                        "removeBookReview",
                        "addSumRate",
                        "subtractSumRate"
                );
        assertThat(BookReview.class.getMethods())
                .extracting(Method::getName)
                .doesNotContain("updateBookReview", "setMeeting", "removeMeeting");
    }

    @Test
    void 차감할_별점보다_합계가_작으면_현재_한줄평_합계를_다시_계산한_뒤_차감한다() {
        Meeting meeting = Meeting.builder().clubId(1L).bookId("book").sumRate(-6.0).build();
        BookReview removed = bookReview(1L, 3.0);
        meeting.addBookReview(removed);
        meeting.addBookReview(bookReview(2L, 4.0));

        meeting.removeBookReviewBy(new ClubMeetingActor(1L, false), removed);

        assertThat(meeting.getSumRate()).isEqualTo(4.0);
    }

    @Test
    void 한줄평을_추가하면_모임에_연결하고_별점_합계를_더한다() {
        Meeting meeting = Meeting.builder().clubId(1L).bookId("book").build();
        BookReview review = bookReview(1L, 4.0);

        meeting.addBookReview(review);

        assertSoftly(softly -> {
            softly.assertThat(review.getMeeting()).isSameAs(meeting);
            softly.assertThat(meeting.getSumRate()).isEqualTo(4.0);
            softly.assertThat(meeting.calculateAverageRate()).isEqualTo(4.0);
        });
    }

    @Test
    void 한줄평_수정은_내용과_별점을_먼저_바꾼_뒤_기존_별점을_차감하고_새_별점을_더한다() {
        Meeting meeting = Meeting.builder().clubId(1L).bookId("book").sumRate(-9.0).build();
        BookReview review = bookReview(1L, 4.0);
        meeting.addBookReview(review);
        meeting.addBookReview(bookReview(2L, 6.0));

        meeting.reviseBookReviewBy(new ClubMeetingActor(1L, false), review, "수정", 2.0);

        assertSoftly(softly -> {
            softly.assertThat(review.getDescription()).isEqualTo("수정");
            softly.assertThat(review.getRate()).isEqualTo(2.0);
            softly.assertThat(meeting.getSumRate()).isEqualTo(6.0);
        });
    }

    @Test
    void 한줄평을_삭제하면_별점을_먼저_차감한_뒤_모임_연관을_해제한다() {
        Meeting meeting = Meeting.builder().clubId(1L).bookId("book").sumRate(-9.0).build();
        BookReview review = bookReview(1L, 4.0);
        meeting.addBookReview(review);
        meeting.addBookReview(bookReview(2L, 6.0));

        meeting.removeBookReviewBy(new ClubMeetingActor(1L, false), review);

        assertSoftly(softly -> {
            softly.assertThat(review.getMeeting()).isNull();
            softly.assertThat(meeting.getSumRate()).isEqualTo(6.0);
            softly.assertThat(meeting.calculateAverageRate()).isEqualTo(6.0);
        });
    }

    @Test
    void 운영진은_다른_회원의_한줄평을_수정하고_삭제할_수_있다() {
        Meeting meeting = Meeting.builder().clubId(1L).bookId("book").build();
        BookReview review = bookReview(1L, 4.0);
        meeting.addBookReview(review);
        ClubMeetingActor staff = new ClubMeetingActor(2L, true);

        meeting.reviseBookReviewBy(staff, review, "운영진 수정", 2.0);
        meeting.removeBookReviewBy(staff, review);

        assertSoftly(softly -> {
            softly.assertThat(review.getDescription()).isEqualTo("운영진 수정");
            softly.assertThat(review.getRate()).isEqualTo(2.0);
            softly.assertThat(review.getMeeting()).isNull();
            softly.assertThat(meeting.getSumRate()).isZero();
        });
    }

    @Test
    void 일반_회원은_다른_회원의_한줄평을_수정할_수_없다() {
        Meeting meeting = Meeting.builder().clubId(1L).bookId("book").build();
        BookReview review = bookReview(1L, 4.0);
        meeting.addBookReview(review);

        ClubMeetingException thrown =
                catchThrowableOfType(
                        ClubMeetingException.class,
                        () -> meeting.reviseBookReviewBy(
                                new ClubMeetingActor(2L, false), review, "수정", 2.0
                        )
                );

        assertSoftly(softly -> {
            softly.assertThat(thrown.getErrorCode()).isEqualTo(
                    ClubMeetingErrorStatus.BOOK_REVIEW_FORBIDDEN
            );
            softly.assertThat(review.getDescription()).isEqualTo("한줄평");
            softly.assertThat(review.getRate()).isEqualTo(4.0);
            softly.assertThat(review.getMeeting()).isSameAs(meeting);
            softly.assertThat(meeting.getSumRate()).isEqualTo(4.0);
        });
    }

    @Test
    void 일반_회원은_다른_회원의_한줄평을_삭제할_수_없다() {
        Meeting meeting = Meeting.builder().clubId(1L).bookId("book").build();
        BookReview review = bookReview(1L, 4.0);
        meeting.addBookReview(review);

        ClubMeetingException thrown =
                catchThrowableOfType(
                        ClubMeetingException.class,
                        () -> meeting.removeBookReviewBy(new ClubMeetingActor(2L, false), review)
                );

        assertSoftly(softly -> {
            softly.assertThat(thrown.getErrorCode()).isEqualTo(
                    ClubMeetingErrorStatus.BOOK_REVIEW_FORBIDDEN
            );
            softly.assertThat(review.getDescription()).isEqualTo("한줄평");
            softly.assertThat(review.getRate()).isEqualTo(4.0);
            softly.assertThat(review.getMeeting()).isSameAs(meeting);
            softly.assertThat(meeting.getSumRate()).isEqualTo(4.0);
        });
    }

    private BookReview bookReview(Long id, double rate) {
        return BookReview.builder()
                .id(id)
                .description("한줄평")
                .rate(rate)
                .clubMemberId(id)
                .memberId(id)
                .build();
    }

}
