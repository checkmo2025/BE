package checkmo.clubNotice.internal.service.command;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO;
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

    public void createNoticeComment(Long clubId, Long noticeId, Long memberId, ClubNoticeRequestDTO.CreateClubNoticeComment request) {
        clubManagementAPI.validateClub(clubId);
        Long clubMemberId = clubManagementAPI.validateAndFetchActiveClubMemberId(clubId, memberId);
        Notice notice = clubNoticeQueryService.validateNotice(clubId, noticeId);
        NoticeComment noticeComment = ClubNoticeConverter.toNoticeComment(request, clubMemberId);
        notice.addComment(noticeComment);
    }

    public void updateNoticeComment(
            Long clubId, Long noticeId, Long commentId, Long memberId,
            ClubNoticeRequestDTO.CreateClubNoticeComment request
    ) {
        clubManagementAPI.validateClub(clubId);
        ClubManagementExternalDTO.MembershipInfo clubMembership = clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        if (!clubMembership.isActive()) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.CLUB_MEMBER_INACTIVE);
        }
        clubNoticeQueryService.validateNotice(clubId, noticeId);
        NoticeComment noticeComment = noticeCommentQueryService.validateNoticeComment(noticeId, commentId);
        if (!noticeComment.isAuthor(clubMembership.getClubMemberId()) && !clubMembership.isStaff()) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_COMMENT_UNAUTHORIZED);
        }
        noticeComment.updateContent(request.getContent());
    }

    public void deleteNoticeComment(Long clubId, Long noticeId, Long commentId, Long memberId) {
        clubManagementAPI.validateClub(clubId);
        ClubManagementExternalDTO.MembershipInfo clubMembership = clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        if (!clubMembership.isActive()) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.CLUB_MEMBER_INACTIVE);
        }
        Notice notice = clubNoticeQueryService.validateNotice(clubId, noticeId);
        NoticeComment noticeComment = noticeCommentQueryService.validateNoticeComment(noticeId, commentId);
        if (!noticeComment.isAuthor(clubMembership.getClubMemberId()) && !clubMembership.isStaff()) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_COMMENT_UNAUTHORIZED);
        }
        notice.removeComment(noticeComment);
    }
}
