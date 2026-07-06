package checkmo.clubNotice;

public interface ClubNoticeAPI {

    ClubNoticeExternalDTO.NoticeReportInfo fetchNoticeReportInfo(Long noticeId);

    ClubNoticeExternalDTO.NoticeCommentReportInfo fetchNoticeCommentReportInfo(Long noticeCommentId);

    Long fetchNoticeCommentAuthorId(Long noticeCommentId);
}
