package checkmo.report.internal.service.command;

import checkmo.bookStory.BookStoryAPI;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubNotice.ClubNoticeAPI;
import checkmo.member.MemberAPI;
import checkmo.realtime.RealtimeAPI;
import checkmo.report.internal.entity.Report;
import checkmo.report.internal.entity.ReportTargetType;
import checkmo.report.internal.exception.ReportErrorStatus;
import checkmo.report.internal.exception.ReportException;
import checkmo.report.internal.repository.ReportRepository;
import checkmo.report.web.dto.ReportRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportCommandService {

    private final MemberAPI memberAPI;
    private final BookStoryAPI bookStoryAPI;
    private final ClubManagementAPI clubManagementAPI;
    private final ClubNoticeAPI clubNoticeAPI;
    private final ClubMeetingAPI clubMeetingAPI;
    private final RealtimeAPI realtimeAPI;

    private final ReportRepository reportRepository;

    public Long createReport(String reporterId, ReportRequestDTO.Create request) {
        String redirectUrl = resolveRedirectUrl(
                reporterId,
                request.getTargetType(),
                request.getTargetId()
        );

        Report report = Report.builder()
                .reporterId(reporterId)
                .reportTargetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reportReason(request.getReason())
                .content(request.getContent())
                .redirectUrl(redirectUrl)
                .build();

        return reportRepository.save(report).getId();
    }

    public String resolveRedirectUrl(
            String reporterId,
            ReportTargetType targetType,
            String targetId
    ) {
        try {
            return switch (targetType) {
                case MEMBER -> resolveMember(reporterId, targetId);
                case CLUB -> resolveClub(targetId);

                case BOOK_STORY -> resolveBookStory(targetId);
                case BOOK_STORY_COMMENT -> resolveBookStoryComment(targetId);

                case CLUB_NOTICE -> resolveClubNotice(targetId);
                case CLUB_NOTICE_COMMENT -> resolveClubNoticeComment(targetId);

                case CLUB_TOPIC -> resolveTopic(targetId);
                case CLUB_BOOK_REVIEW -> resolveBookReview(targetId);
                case CHAT -> resolveChat(targetId);
            };
        } catch (NumberFormatException e) {
            throw new ReportException(ReportErrorStatus.INVALID_REPORT_TARGET_ID);
        }
    }

    private String resolveMember(String reporterId, String memberNickname) {
        String reportedMemberId = memberAPI.fetchMemberId(memberNickname);

        if (reporterId.equals(reportedMemberId)) {
            throw new ReportException(ReportErrorStatus.CANNOT_REPORT_SELF);
        }

        return ReportTargetType.MEMBER.createRedirectUrl(
                memberNickname,
                Map.of()
        );
    }

    private String resolveClub(String targetId) {
        Long clubId = Long.valueOf(targetId);

        clubManagementAPI.validateClub(clubId);

        return ReportTargetType.CLUB.createRedirectUrl(
                String.valueOf(clubId),
                Map.of()
        );
    }

    private String resolveBookStory(String targetId) {
        Long bookStoryId = Long.valueOf(targetId);

        bookStoryAPI.validateBookStory(bookStoryId);

        return ReportTargetType.BOOK_STORY.createRedirectUrl(
                String.valueOf(targetId),
                Map.of()
        );
    }

    private String resolveBookStoryComment(String targetId) {
        Long commentId = Long.valueOf(targetId);

        Long bookStoryId = bookStoryAPI.fetchBookStoryIdByBookStoryCommentId(commentId);

        return ReportTargetType.BOOK_STORY_COMMENT.createRedirectUrl(
                String.valueOf(targetId),
                Map.of("bookStoryId", String.valueOf(bookStoryId))
        );
    }

    private String resolveClubNotice(String targetId) {
        Long noticeId = Long.valueOf(targetId);

        var info = clubNoticeAPI.fetchNoticeReportInfo(noticeId);

        return ReportTargetType.CLUB_NOTICE.createRedirectUrl(
                String.valueOf(info.getNoticeId()),
                Map.of("clubId", String.valueOf(info.getClubId()))
        );
    }

    private String resolveClubNoticeComment(String targetId) {
        Long noticeCommentId = Long.valueOf(targetId);

        var info = clubNoticeAPI.fetchNoticeCommentReportInfo(noticeCommentId);

        return ReportTargetType.CLUB_NOTICE_COMMENT.createRedirectUrl(
                String.valueOf(info.getNoticeCommentId()),
                Map.of(
                        "clubId", String.valueOf(info.getClubId()),
                        "noticeId", String.valueOf(info.getNoticeId())
                )
        );
    }

    private String resolveTopic(String targetId) {
        Long topicId = Long.valueOf(targetId);

        var info = clubMeetingAPI.fetchTopicReportInfo(topicId);

        return ReportTargetType.CLUB_TOPIC.createRedirectUrl(
                String.valueOf(info.getTopicId()),
                Map.of(
                        "clubId", String.valueOf(info.getClubId()),
                        "meetingId", String.valueOf(info.getMeetingId())
                )
        );
    }

    private String resolveBookReview(String targetId) {
        Long bookReviewId = Long.valueOf(targetId);

        var info = clubMeetingAPI.fetchBookReviewReportInfo(bookReviewId);

        return ReportTargetType.CLUB_BOOK_REVIEW.createRedirectUrl(
                String.valueOf(info.getBookReviewId()),
                Map.of(
                        "clubId", String.valueOf(info.getClubId()),
                        "meetingId", String.valueOf(info.getMeetingId())
                )
        );
    }

    private String resolveChat(String targetId) {
        Long chatMessageId = Long.valueOf(targetId);

        var info = realtimeAPI.fetchTeamChatReportInfo(chatMessageId);

        return ReportTargetType.CHAT.createRedirectUrl(
                String.valueOf(info.getChatMessageId()),
                Map.of(
                        "clubId", String.valueOf(info.getClubId()),
                        "meetingId", String.valueOf(info.getMeetingId()),
                        "teamId", String.valueOf(info.getTeamId())
                )
        );
    }
}
