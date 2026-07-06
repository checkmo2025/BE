package checkmo.clubNotice.internal;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubNotice.ClubNoticeAPI;
import checkmo.clubNotice.ClubNoticeExternalDTO;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.NoticeComment;
import checkmo.clubNotice.internal.service.query.ClubNoticeQueryService;
import checkmo.clubNotice.internal.service.query.NoticeCommentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubNoticeAPIImpl implements ClubNoticeAPI {

    private final ClubManagementAPI clubManagementAPI;
    private final ClubNoticeQueryService clubNoticeQueryService;
    private final NoticeCommentQueryService noticeCommentQueryService;

    @Override
    public ClubNoticeExternalDTO.NoticeReportInfo fetchNoticeReportInfo(Long noticeId) {
        Notice notice = clubNoticeQueryService.validateNotice(noticeId);
        return ClubNoticeExternalDTO.NoticeReportInfo.builder()
                .noticeId(notice.getId())
                .clubId(notice.getClubId())
                .build();
    }

    @Override
    public ClubNoticeExternalDTO.NoticeCommentReportInfo fetchNoticeCommentReportInfo(Long noticeCommentId) {
        NoticeComment noticeComment = noticeCommentQueryService.validateNoticeComment(noticeCommentId);
        Notice notice = noticeComment.getNotice();

        return ClubNoticeExternalDTO.NoticeCommentReportInfo.builder()
                .noticeCommentId(noticeComment.getId())
                .noticeId(notice.getId())
                .clubId(notice.getClubId())
                .build();
    }

    @Override
    public Long fetchNoticeCommentAuthorId(Long noticeCommentId) {
        NoticeComment noticeComment = noticeCommentQueryService.validateNoticeComment(noticeCommentId);

        return clubManagementAPI.fetchMembershipInfoByClubMemberIds(Set.of(noticeComment.getClubMemberId()))
                .get(noticeComment.getClubMemberId())
                .getMemberId();
    }
}
