package checkmo.report.internal.service.query;

import checkmo.bookStory.BookStoryAPI;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubNotice.ClubNoticeAPI;
import checkmo.common.apiPayload.exception.GeneralException;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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

    public ReportResponseDTO.MyReportList retrieveMyReports(Long memberId, Long cursorId) {
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

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ReportResponseDTO.AdminMemberReportList retrieveMemberReportsForAdmin(
            String memberNickname,
            Long cursorId
    ) {
        Long reporterId = Long.valueOf(memberAPI.fetchMemberId(memberNickname));

        CursorResult<Report> reportCursorResult = CursorPagingHelper.getPage(
                size -> reportRepository.findMyReports(reporterId, cursorId, size),
                Report::getId,
                DEFAULT_PAGE_SIZE
        );

        List<ReportResponseDTO.AdminMemberReportInfo> reports = reportCursorResult.content().stream()
                .map(report -> {
                    AdminTarget target = resolveAdminTarget(report);
                    return ReportConverter.toAdminMemberReportInfo(
                            report,
                            target.label(),
                            target.url(),
                            target.available()
                    );
                })
                .toList();

        return ReportResponseDTO.AdminMemberReportList.builder()
                .reports(reports)
                .hasNext(reportCursorResult.hasNext())
                .nextCursor(reportCursorResult.nextCursor())
                .build();
    }

    private AdminTarget resolveAdminTarget(Report report) {
        try {
            String targetId = report.getTargetId();
            ReportTargetType targetType = report.getReportTargetType();

            return switch (targetType) {
                case MEMBER -> {
                    DisplayInfo member = resolveMemberDisplayInfoByNickname(targetId);
                    yield availableTarget(
                            member.name(),
                            targetType.createRedirectUrl(targetId, Map.of())
                    );
                }
                case CLUB -> {
                    DisplayInfo club = resolveClubDisplayInfo(Long.valueOf(targetId));
                    yield availableTarget(
                            club.name(),
                            targetType.createRedirectUrl(targetId, Map.of())
                    );
                }
                case BOOK_STORY -> {
                    bookStoryAPI.fetchBookStoryAuthorId(Long.valueOf(targetId));
                    yield availableTarget(
                            targetLabel(report),
                            targetType.createRedirectUrl(targetId, Map.of())
                    );
                }
                case BOOK_STORY_COMMENT -> {
                    Long bookStoryId = bookStoryAPI.fetchBookStoryIdByBookStoryCommentId(Long.valueOf(targetId));
                    yield availableTarget(
                            targetLabel(report),
                            targetType.createRedirectUrl(
                                    targetId,
                                    Map.of("bookStoryId", String.valueOf(bookStoryId))
                            )
                    );
                }
                case CLUB_NOTICE -> {
                    var info = clubNoticeAPI.fetchNoticeReportInfo(Long.valueOf(targetId));
                    yield availableTarget(
                            targetLabel(report),
                            targetType.createRedirectUrl(
                                    targetId,
                                    Map.of("clubId", String.valueOf(info.getClubId()))
                            )
                    );
                }
                case CLUB_NOTICE_COMMENT -> {
                    var info = clubNoticeAPI.fetchNoticeCommentReportInfo(Long.valueOf(targetId));
                    yield availableTarget(
                            targetLabel(report),
                            targetType.createRedirectUrl(
                                    targetId,
                                    Map.of(
                                            "clubId", String.valueOf(info.getClubId()),
                                            "noticeId", String.valueOf(info.getNoticeId())
                                    )
                            )
                    );
                }
                case CLUB_TOPIC -> {
                    var info = clubMeetingAPI.fetchTopicReportInfo(Long.valueOf(targetId));
                    yield availableTarget(
                            targetLabel(report),
                            targetType.createRedirectUrl(
                                    targetId,
                                    Map.of(
                                            "clubId", String.valueOf(info.getClubId()),
                                            "meetingId", String.valueOf(info.getMeetingId())
                                    )
                            )
                    );
                }
                case CLUB_BOOK_REVIEW -> {
                    var info = clubMeetingAPI.fetchBookReviewReportInfo(Long.valueOf(targetId));
                    yield availableTarget(
                            targetLabel(report),
                            targetType.createRedirectUrl(
                                    targetId,
                                    Map.of(
                                            "clubId", String.valueOf(info.getClubId()),
                                            "meetingId", String.valueOf(info.getMeetingId())
                                    )
                            )
                    );
                }
                case CHAT -> {
                    var info = realtimeAPI.fetchTeamChatReportInfo(Long.valueOf(targetId));
                    yield availableTarget(
                            targetLabel(report),
                            targetType.createRedirectUrl(
                                    targetId,
                                    Map.of(
                                            "clubId", String.valueOf(info.getClubId()),
                                            "meetingId", String.valueOf(info.getMeetingId()),
                                            "teamId", String.valueOf(info.getTeamId())
                                    )
                            )
                    );
                }
            };
        } catch (NumberFormatException exception) {
            return AdminTarget.unavailable();
        } catch (GeneralException exception) {
            if (exception.getErrorReasonHttpStatus().getHttpStatus() == HttpStatus.NOT_FOUND) {
                return AdminTarget.unavailable();
            }
            throw exception;
        }
    }

    private String targetLabel(Report report) {
        return report.getReportTargetType().getDescription() + " #" + report.getTargetId();
    }

    private AdminTarget availableTarget(String label, String url) {
        return new AdminTarget(label, url, true);
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

    private record AdminTarget(
            String label,
            String url,
            boolean available
    ) {
        private static AdminTarget unavailable() {
            return new AdminTarget("삭제되었거나 확인할 수 없는 대상", null, false);
        }
    }
}
