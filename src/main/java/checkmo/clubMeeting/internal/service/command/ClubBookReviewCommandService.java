package checkmo.clubMeeting.internal.service.command;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.repository.BookReviewRepository;
import checkmo.clubMeeting.internal.service.query.ClubBookReviewQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.BookReviewCreate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubBookReviewCommandService {

    private final ClubManagementAPI clubManagementAPI;

    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubBookReviewQueryService clubBookReviewQueryService;

    private final BookReviewRepository bookReviewRepository;

    // TODO: Aspect 로그
    // TODO: Test DB 설정 후, 낙관적 락 동작 테스트
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300)
    )
    public void createBookReview(Long clubId, Long meetingId, Long memberId, BookReviewCreate request) {
        clubManagementAPI.validateClub(clubId);
        Long clubMemberId = clubManagementAPI.validateAndFetchActiveClubMemberId(clubId, memberId);
        Meeting meeting = clubMeetingQueryService.validateMeeting(clubId, meetingId);

        BookReview bookReview = ClubMeetingConverter.toBookReview(request, clubMemberId, memberId);
        bookReview.setMeeting(meeting);

        meeting.addSumRate(bookReview.getRate());

        bookReviewRepository.save(bookReview);
    }

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300)
    )
    public void updateBookReview(Long clubId, Long meetingId, Long reviewId, Long memberId, BookReviewCreate request) {
        clubManagementAPI.validateClub(clubId);
        ClubManagementExternalDTO.MembershipInfo clubMembership = clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        if (!clubMembership.isActive()) {
            throw new ClubMeetingException(ClubMeetingErrorStatus.CLUB_MEMBER_INACTIVE);
        }
        Meeting meeting = clubMeetingQueryService.validateMeeting(clubId, meetingId);

        BookReview bookReview = clubBookReviewQueryService.validateBookReview(reviewId, meeting.getId());
        if (!bookReview.isOwnedBy(clubMembership.getClubMemberId()) && !clubMembership.isStaff()) {
            throw new ClubMeetingException(ClubMeetingErrorStatus.BOOK_REVIEW_FORBIDDEN);
        }

        double oldRate = bookReview.getRate();
        double newRate = request.getRate();

        bookReview.updateBookReview(
                request.getDescription(),
                request.getRate()
        );

        // 별점이 변경된 경우에만 미팅의 별점 합산
        if (oldRate != newRate) {
            meeting.subtractSumRate(oldRate);
            meeting.addSumRate(newRate);
        }
    }

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300)
    )
    public void deleteBookReview(Long clubId, Long meetingId, Long reviewId, Long memberId) {
        clubManagementAPI.validateClub(clubId);
        ClubManagementExternalDTO.MembershipInfo clubMembership = clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        if (!clubMembership.isActive()) {
            throw new ClubMeetingException(ClubMeetingErrorStatus.CLUB_MEMBER_INACTIVE);
        }
        Meeting meeting = clubMeetingQueryService.validateMeeting(clubId, meetingId);

        BookReview bookReview = clubBookReviewQueryService.validateBookReview(reviewId, meeting.getId());
        if (!bookReview.isOwnedBy(clubMembership.getClubMemberId()) && !clubMembership.isStaff()) {
            throw new ClubMeetingException(ClubMeetingErrorStatus.BOOK_REVIEW_FORBIDDEN);
        }

        meeting.subtractSumRate(bookReview.getRate());

        bookReview.removeMeeting();
    }

}
