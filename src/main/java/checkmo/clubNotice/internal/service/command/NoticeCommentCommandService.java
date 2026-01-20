package checkmo.clubNotice.internal.service.command;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubNotice.internal.converter.ClubNoticeConverter;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.NoticeComment;
import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.clubNotice.internal.service.query.ClubNoticeQueryService;
import checkmo.clubNotice.internal.service.query.NoticeCommentQueryService;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeCommentCommandService {
    private final ClubManagementAPI clubManagementAPI;
    private final ClubNoticeQueryService clubNoticeQueryService;
    private final NoticeCommentQueryService noticeCommentQueryService;

    public void createNoticeComment(
            Long clubId, Long noticeId, String memberId, ClubNoticeRequestDTO.CreateClubNoticeComment request) {
        clubManagementAPI.validateClub(clubId);
        Long clubMemberId = clubManagementAPI.fetchActiveClubMemberId(clubId, memberId);
        Notice notice = clubNoticeQueryService.validateNotice(clubId, noticeId);
        NoticeComment noticeComment = ClubNoticeConverter.toNoticeComment(request, clubMemberId);
        notice.addComment(noticeComment);
    }

    public void updateNoticeComment(
            Long clubId, Long noticeId, Long commentId, String memberId,
            ClubNoticeRequestDTO.CreateClubNoticeComment request
    ) {
        clubManagementAPI.validateClub(clubId);
        Long clubMemberId = clubManagementAPI.fetchActiveClubMemberId(clubId, memberId);
        clubNoticeQueryService.validateNotice(clubId, noticeId);
        NoticeComment noticeComment = noticeCommentQueryService.validateNoticeComment(noticeId, commentId);
        if (!noticeComment.isAuthor(clubMemberId)) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_COMMENT_UNAUTHORIZED);
        }
        noticeComment.updateContent(request.getContent());
    }

    public void deleteNoticeComment(Long clubId, Long noticeId, Long commentId, String memberId) {
        clubManagementAPI.validateClub(clubId);
        Long clubMemberId = clubManagementAPI.fetchActiveClubMemberId(clubId, memberId);
        Notice notice = clubNoticeQueryService.validateNotice(clubId, noticeId);
        NoticeComment noticeComment = noticeCommentQueryService.validateNoticeComment(noticeId, commentId);
        if (!noticeComment.isAuthor(clubMemberId)) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_COMMENT_UNAUTHORIZED);
            // 운영진, 작성자 검증
        }
        notice.removeComment(noticeComment);
    }
}
