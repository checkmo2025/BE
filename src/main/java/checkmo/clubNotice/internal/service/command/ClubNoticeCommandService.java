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

    private final ClubManagementAPI clubManagementAPI;
    private final ClubMeetingAPI clubMeetingAPI;

    private final ClubNoticeQueryService clubNoticeQueryService;

    private final NoticeRepository noticeRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    public Notice createNotice(Long clubId, String memberId, CreateClubNotice request) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);
        NoticeTag tag = NoticeTag.decideTag(request.getVote() != null, request.getMeetingId() != null);
        if (tag.isMeeting() && clubMeetingAPI.isNotMeetingBelongsToClub(clubId, request.getMeetingId())) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.MEETING_NOT_IN_CLUB);
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
        if (request.getMeetingId() != null && clubMeetingAPI.isNotMeetingBelongsToClub(clubId,
                request.getMeetingId())) {
            throw new ClubNoticeException(ClubNoticeErrorStatus.MEETING_NOT_IN_CLUB);
        }
        notice.update(
                request.getTitle(),
                request.getContent(),
                request.isImportant(),
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

    public void deleteNotice(Long clubId, String memberId, Long noticeId) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);

        Notice notice = clubNoticeQueryService.validateNotice(clubId, noticeId);
        publishNoticeImageDeletedEvent(notice.getImageUrls());

        noticeRepository.delete(notice);
    }

    private void publishNoticeImageDeletedEvent(List<String> removedImages) {
        applicationEventPublisher.publishEvent(
                ClubNoticeEvent.DeleteNoticeImage.builder()
                        .imageUrls(removedImages)
                        .build()
        );
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
}
