package checkmo.report;

import static org.assertj.core.api.Assertions.assertThat;

import checkmo.report.internal.entity.ReportTargetType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReportTargetTypeTest {

    @Test
    void createsFrontendRoutesForAllReportTargets() {
        assertThat(ReportTargetType.MEMBER.createRedirectUrl("reader", Map.of()))
                .isEqualTo("/profile/reader");
        assertThat(ReportTargetType.CLUB.createRedirectUrl("11", Map.of()))
                .isEqualTo("/groups/11");
        assertThat(ReportTargetType.BOOK_STORY.createRedirectUrl("12", Map.of()))
                .isEqualTo("/stories/12");
        assertThat(ReportTargetType.BOOK_STORY_COMMENT.createRedirectUrl(
                "13",
                Map.of("bookStoryId", "12")
        )).isEqualTo("/stories/12?commentId=13");
        assertThat(ReportTargetType.CLUB_NOTICE.createRedirectUrl(
                "14",
                Map.of("clubId", "11")
        )).isEqualTo("/groups/11/notice/14");
        assertThat(ReportTargetType.CLUB_NOTICE_COMMENT.createRedirectUrl(
                "15",
                Map.of("clubId", "11", "noticeId", "14")
        )).isEqualTo("/groups/11/notice/14?commentId=15");
        assertThat(ReportTargetType.CLUB_TOPIC.createRedirectUrl(
                "16",
                Map.of("clubId", "11", "meetingId", "21")
        )).isEqualTo("/groups/11/bookcase/21?tab=topic&topicId=16");
        assertThat(ReportTargetType.CLUB_BOOK_REVIEW.createRedirectUrl(
                "17",
                Map.of("clubId", "11", "meetingId", "21")
        )).isEqualTo("/groups/11/bookcase/21?tab=review&reviewId=17");
        assertThat(ReportTargetType.CHAT.createRedirectUrl(
                "18",
                Map.of("clubId", "11", "meetingId", "21", "teamId", "31")
        )).isEqualTo("/groups/11/bookcase/21/meeting?teamId=31&messageId=18");
    }
}
