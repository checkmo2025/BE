package checkmo.clubNotice.internal.service.command;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubNotice.ClubNoticeEvent;
import checkmo.clubNotice.ClubNoticeEvent.ClubNoticeCreated;
import checkmo.clubNotice.internal.converter.ClubNoticeConverter;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.NoticeTag;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.clubNotice.internal.repository.NoticeRepository;
import checkmo.clubNotice.internal.service.query.ClubNoticeQueryService;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO.CreateClubNotice;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO.CreateClubVote;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO.UpdateClubNotice;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO.VoteResult;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class ClubNoticeCommandService {

    private static final int MAX_PINNED_COUNT = 5;

    private final ClubManagementAPI clubManagementAPI;
    private final ClubMeetingAPI clubMeetingAPI;

    private final ClubNoticeQueryService clubNoticeQueryService;

    private final NoticeRepository noticeRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    public Notice createNotice(Long clubId, String memberId, CreateClubNotice request) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);
        NoticeTag tag = NoticeTag.decideTag(request.getVote() != null, request.getMeetingId() != null);
        if (tag.isMeeting() && !clubMeetingAPI.isMeetingInClub(clubId, request.getMeetingId())) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.MEETING_NOT_IN_CLUB);
        }
        if (request.isPinned()) {
            validatePinnedLimit(clubId);
        }

        Notice notice = ClubNoticeConverter.toNotice(request, tag, clubId);
        notice.replaceImages(request.getImageUrls());
        CreateClubVote vote = request.getVote();
        if (vote != null) {
            notice.attachVote(
                    vote.getTitle(),
                    vote.getContent(),
                    vote.getItem1(),
                    vote.getItem2(),
                    vote.getItem3(),
                    vote.getItem4(),
                    vote.getItem5(),
                    vote.getItem6(),
                    vote.isAnonymity(),
                    vote.isDuplication(),
                    vote.getStartTime(),
                    vote.getDeadline()
            );
        }
        noticeRepository.save(notice);

        publishNoticeCreatedEvent(notice, clubId);

        return notice;
    }

    private void publishNoticeCreatedEvent(Notice notice, Long clubId) {
        String clubName = clubManagementAPI.fetchClubName(clubId);
        ClubNoticeCreated event = ClubNoticeCreated.builder()
                .eventId(notice.getId())
                .clubId(clubId)
                .clubName(clubName)
                .build();
        applicationEventPublisher.publishEvent(event);
    }

    public void updateNotice(Long clubId, Long noticeId, String memberId, UpdateClubNotice request) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);

        Notice notice = clubNoticeQueryService.validateNotice(clubId, noticeId);
        if (request.getMeetingId() != null && !clubMeetingAPI.isMeetingInClub(clubId, request.getMeetingId())) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.MEETING_NOT_IN_CLUB);
        }
        if (!notice.isPinned() && request.isPinned()) {
            validatePinnedLimit(clubId);
        }
        notice.update(
                request.getTitle(),
                request.getContent(),
                request.isPinned(),
                request.getMeetingId()
        );
        if (request.getVote() != null) {
            notice.updateVoteDeadline(request.getVote().getDeadline());
        }

        List<String> removedImages = notice.replaceImages(request.getImageUrls());
        noticeRepository.flush();
        if (!removedImages.isEmpty()) {
            publishNoticeImageDeletedEvent(removedImages);
        }
    }

    private void validatePinnedLimit(Long clubId) {
        long pinnedCount = noticeRepository.countByClubIdAndPinnedTrue(clubId);
        if (pinnedCount >= MAX_PINNED_COUNT) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.PINNED_NOTICE_LIMIT_EXCEEDED);
        }
    }

    public void deleteNotice(Long clubId, String memberId, Long noticeId) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);

        Notice notice = clubNoticeQueryService.validateNotice(clubId, noticeId);
        publishNoticeImageDeletedEvent(notice.getImageUrls());

        noticeRepository.delete(notice);
    }

    public Long haveVote(Long clubId, Long noticeId, Long voteId, String memberId, VoteResult request) {
        clubManagementAPI.validateClub(clubId);
        Long clubMemberId = clubManagementAPI.fetchActiveClubMemberId(clubId, memberId);

        Notice notice = clubNoticeQueryService.validateNotice(clubId, noticeId);
        Vote vote = notice.getVote();
        if (vote == null || vote.getId() == null || !vote.getId().equals(voteId)) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.VOTE_NOT_FOUND);
        }

        validateVotingTime(vote);
        vote.validateSelectedItemNumbersExist(request.getSelectedItemNumbers());
        vote.validateChoiceCountBasedOnDuplication(request.countSelectedItems());

        vote.upsertClubMemberVote(
                clubMemberId,
                request.getSelectedItemNumbers(),
                ClubNoticeConverter.toClubMemberVote(vote, clubMemberId, request)
        );

        return vote.getId();
    }

    private void validateVotingTime(Vote vote) {
        LocalDateTime now = LocalDateTime.now();
        vote.validateVotingTime(now);
    }

    public void deleteAllByClubId(Long clubId) {
        List<Notice> notices = noticeRepository.findAllWithImagesByClubId(clubId);
        if (notices.isEmpty()) {
            return;
        }
        publishNoticeImageDeletedIfAny(notices);
        noticeRepository.deleteAll(notices);
        noticeRepository.flush();
    }

    public void deleteAllByMeetingId(Long clubId, Long meetingId) {
        if (meetingId == null) {
            return;
        }
        List<Notice> notices = noticeRepository.findAllWithImagesByClubIdAndMeetingId(clubId, meetingId);
        if (notices.isEmpty()) {
            return;
        }
        publishNoticeImageDeletedIfAny(notices);
        noticeRepository.deleteAll(notices);
        noticeRepository.flush();
    }

    private void publishNoticeImageDeletedIfAny(List<Notice> notices) {
        List<String> imageUrls = notices.stream()
                .flatMap(n -> n.getImageUrls().stream())
                .distinct()
                .toList();
        if (!imageUrls.isEmpty()) {
            publishNoticeImageDeletedEvent(imageUrls);
        }
    }

    private void publishNoticeImageDeletedEvent(List<String> removedImages) {
        applicationEventPublisher.publishEvent(
                ClubNoticeEvent.DeleteNoticeImage.builder()
                        .imageUrls(removedImages)
                        .build()
        );
    }
}
