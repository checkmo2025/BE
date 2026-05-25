package checkmo.report.internal.service.query;

import checkmo.bookStory.BookStoryAPI;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubNotice.ClubNoticeAPI;
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.member.MemberAPI;
import checkmo.realtime.RealtimeAPI;
import checkmo.report.internal.converter.ReportConverter;
import checkmo.report.internal.entity.Report;
import checkmo.report.internal.entity.ReportTargetType;
import checkmo.report.internal.repository.ReportRepository;
import checkmo.report.web.dto.ReportResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ReportRepository reportRepository;

    private final MemberAPI memberAPI;
    private final ClubManagementAPI clubManagementAPI;
    private final BookStoryAPI bookStoryAPI;
    private final ClubNoticeAPI clubNoticeAPI;
    private final ClubMeetingAPI clubMeetingAPI;
    private final RealtimeAPI realtimeAPI;

    public ReportResponseDTO.MyReportList retrieveMyReports(String memberId, Long cursorId) {
        CursorResult<Report> reportCursorResult = CursorPagingHelper.getPage(
                size -> reportRepository.findMyReports(memberId, cursorId, size),
                Report::getId,
                DEFAULT_PAGE_SIZE
        );

        List<ReportResponseDTO.ReportInfo> reports = reportCursorResult.content().stream()
                .map(report -> {
                    DisplayInfo displayInfo = resolveDisplayInfo(
                            report.getReportTargetType(),
                            report.getTargetId()
                    );

                    return ReportConverter.toReportInfo(
                            report,
                            displayInfo.name(),
                            displayInfo.imageUrl()
                    );
                })
                .toList();

        return ReportResponseDTO.MyReportList.builder()
                .reports(reports)
                .hasNext(reportCursorResult.hasNext())
                .nextCursor(reportCursorResult.nextCursor())
                .build();
    }

    private DisplayInfo resolveDisplayInfo(ReportTargetType targetType, String targetId) {
        return switch (targetType) {
            case CLUB -> resolveClubDisplayInfo(Long.valueOf(targetId));

            case CLUB_NOTICE -> resolveClubNoticeDisplayInfo(Long.valueOf(targetId));

            case MEMBER -> resolveMemberDisplayInfoByNickname(targetId);

            case BOOK_STORY -> resolveMemberDisplayInfo(
                    bookStoryAPI.fetchBookStoryAuthorId(Long.valueOf(targetId))
            );

            case BOOK_STORY_COMMENT -> resolveMemberDisplayInfo(
                    bookStoryAPI.fetchBookStoryCommentAuthorId(Long.valueOf(targetId))
            );

            case CLUB_NOTICE_COMMENT -> resolveMemberDisplayInfo(
                    clubNoticeAPI.fetchNoticeCommentAuthorId(Long.valueOf(targetId))
            );

            case CLUB_TOPIC -> resolveMemberDisplayInfo(
                    clubMeetingAPI.fetchTopicAuthorId(Long.valueOf(targetId))
            );

            case CLUB_BOOK_REVIEW -> resolveMemberDisplayInfo(
                    clubMeetingAPI.fetchBookReviewAuthorId(Long.valueOf(targetId))
            );

            case CHAT -> resolveMemberDisplayInfo(
                    realtimeAPI.fetchChatSenderMemberId(Long.valueOf(targetId))
            );
        };
    }

    private DisplayInfo resolveClubDisplayInfo(Long clubId) {
        var clubInfo = clubManagementAPI.fetchDisplayInfo(clubId);

        return new DisplayInfo(
                clubInfo.getClubName(),
                clubInfo.getClubImageUrl()
        );
    }

    private DisplayInfo resolveClubNoticeDisplayInfo(Long noticeId) {
        var noticeInfo = clubNoticeAPI.fetchNoticeReportInfo(noticeId);

        return resolveClubDisplayInfo(noticeInfo.getClubId());
    }

    private DisplayInfo resolveMemberDisplayInfoByNickname(String nickname) {
        String memberId = memberAPI.fetchMemberId(nickname);

        return resolveMemberDisplayInfo(memberId);
    }

    private DisplayInfo resolveMemberDisplayInfo(String memberId) {
        var memberInfo = memberAPI.fetchMemberBasicInfo(memberId);

        return new DisplayInfo(
                memberInfo.getNickname(),
                memberInfo.getProfileImageUrl()
        );
    }

    private record DisplayInfo(
            String name,
            String imageUrl
    ) {
    }
}
