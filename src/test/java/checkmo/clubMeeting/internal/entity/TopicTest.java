package checkmo.clubMeeting.internal.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class TopicTest {

    @Test
    void 작성자는_발제를_수정하고_삭제할_수_있다() {
        Meeting meeting = meeting();
        Topic topic = topic(meeting);
        ClubMeetingActor owner = new ClubMeetingActor(1L, false);

        topic.updateBy(owner, "수정");
        topic.removeBy(owner);

        assertSoftly(softly -> {
            softly.assertThat(topic.getDescription()).isEqualTo("수정");
            softly.assertThat(topic.getMeeting()).isNull();
        });
    }

    @Test
    void 운영진은_다른_작성자의_발제를_수정하고_삭제할_수_있다() {
        Meeting meeting = meeting();
        Topic topic = topic(meeting);
        ClubMeetingActor staff = new ClubMeetingActor(2L, true);

        topic.updateBy(staff, "운영진 수정");
        topic.removeBy(staff);

        assertSoftly(softly -> {
            softly.assertThat(topic.getDescription()).isEqualTo("운영진 수정");
            softly.assertThat(topic.getMeeting()).isNull();
        });
    }

    @Test
    void 일반_회원은_다른_작성자의_발제를_수정할_수_없다() {
        Meeting meeting = meeting();
        Topic topic = topic(meeting);

        ClubMeetingException thrown = catchThrowableOfType(
                ClubMeetingException.class,
                () -> topic.updateBy(new ClubMeetingActor(2L, false), "수정")
        );

        assertSoftly(softly -> {
            softly.assertThat(thrown.getErrorCode()).isEqualTo(ClubMeetingErrorStatus.TOPIC_FORBIDDEN);
            softly.assertThat(topic.getDescription()).isEqualTo("기존");
            softly.assertThat(topic.getMeeting()).isSameAs(meeting);
        });
    }

    @Test
    void 일반_회원은_다른_작성자의_발제를_삭제할_수_없다() {
        Meeting meeting = meeting();
        Topic topic = topic(meeting);

        ClubMeetingException thrown = catchThrowableOfType(
                ClubMeetingException.class,
                () -> topic.removeBy(new ClubMeetingActor(2L, false))
        );

        assertSoftly(softly -> {
            softly.assertThat(thrown.getErrorCode()).isEqualTo(ClubMeetingErrorStatus.TOPIC_FORBIDDEN);
            softly.assertThat(topic.getDescription()).isEqualTo("기존");
            softly.assertThat(topic.getMeeting()).isSameAs(meeting);
        });
    }

    @Test
    void 저수준_발제_변경_메서드를_public_API로_노출하지_않는다() {
        assertThat(Topic.class.getMethods())
                .extracting(Method::getName)
                .doesNotContain("updateTopic", "removeMeeting");
    }

    private Meeting meeting() {
        return Meeting.builder().clubId(1L).bookId("book").build();
    }

    private Topic topic(Meeting meeting) {
        Topic topic = Topic.builder()
                .description("기존")
                .clubMemberId(1L)
                .memberId(1L)
                .build();
        topic.setMeeting(meeting);
        return topic;
    }
}
