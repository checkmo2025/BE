package checkmo.clubMeeting.internal.service.command;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.repository.BookReviewRepository;
import checkmo.clubMeeting.internal.service.query.ClubBookReviewQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.BookReviewCreate;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubBookReviewCommandServiceImpl implements ClubBookReviewCommandService {

    private final ClubManagementAPI clubManagementAPI;

    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubBookReviewQueryService clubBookReviewQueryService;

    private final BookReviewRepository bookReviewRepository;

    // TODO: Aspect 로그
    // TODO: Test DB 설정 후, 낙관적 락 동작 테스트
    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300)
    )
    public Long createBookReview(Long meetingId, String memberId, BookReviewCreate request) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Long clubMemberId = clubManagementAPI.getActiveClubMemberInfo(meeting.getClubId(), memberId);

        BookReview bookReview = ClubMeetingConverter.toBookReview(request, clubMemberId, memberId);
        bookReview.setMeeting(meeting);

        meeting.addSumRate(bookReview.getRate());

        return bookReviewRepository.save(bookReview).getId();
    }

    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300)
    )
    public Long updateBookReview(Long meetingId, Long reviewId, String memberId, BookReviewCreate request) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Long clubMemberId = clubManagementAPI.getActiveClubMemberInfo(meeting.getClubId(), memberId);

        BookReview bookReview = clubBookReviewQueryService.validateBookReview(reviewId, meeting.getId());
        if (!bookReview.getClubMemberId().equals(clubMemberId)) {
            throw new GeneralException(ErrorStatus.BOOK_REVIEW_FORBIDDEN);
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

        return bookReview.getId();
    }

    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300)
    )
    public void deleteBookReview(Long meetingId, Long reviewId, String memberId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Long clubMemberId = clubManagementAPI.getActiveClubMemberInfo(meeting.getClubId(), memberId);

        BookReview bookReview = clubBookReviewQueryService.validateBookReview(reviewId, meetingId);
        if (!bookReview.getClubMemberId().equals(clubMemberId)) {
            throw new GeneralException(ErrorStatus.BOOK_REVIEW_FORBIDDEN);
        }

        meeting.subtractSumRate(bookReview.getRate());

        bookReview.removeMeeting();
    }
}
