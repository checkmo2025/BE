package checkmo.clubNotice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ClubNoticeExternalDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticeReportInfo {
        private Long noticeId;
        private Long clubId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticeCommentReportInfo {
        private Long noticeCommentId;
        private Long noticeId;
        private Long clubId;
    }
}