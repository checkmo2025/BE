package checkmo.clubMeeting.internal.service.command;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.repository.BookReviewRepository;
import checkmo.clubMeeting.internal.service.query.ClubBookReviewQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.BookReviewDTO;
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

    // 자신의 QueryService
    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubBookReviewQueryService clubBookReviewQueryService;

    // 자신의 Repository
    private final BookReviewRepository bookReviewRepository;

    // TODO: Aspect 로그
    // TODO: Test DB 설정 후, 낙관적 락 동작 테스트
    @Override
    @Retryable( // OptimisticLockingFailureException 발생 시 재시도
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300) // 300ms 간격으로 재시도
    )
    public Long createBookReview(Long meetingId, String memberId, BookReviewDTO request) {
        // 유효성 검증 (meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Long clubMemberId = clubManagementAPI.getActiveClubMemberInfo(meeting.getClubId(), memberId);

        // 한줄평 생성
        BookReview bookReview
                = ClubMeetingConverter.fromBookReviewDTOToBookReview(request, clubMemberId, memberId);
        bookReview.setMeeting(meeting);

        // 미팅의 별점 합산
        meeting.addSumRate(bookReview.getRate());

        return bookReviewRepository.save(bookReview).getId();
    }

    @Override
    @Retryable( // OptimisticLockingFailureException 발생 시 재시도
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300) // 300ms 간격으로 재시도
    )
    public Long updateBookReview(Long meetingId, Long reviewId, String memberId, BookReviewDTO request) {
        // 유효성 검증 (meeting, clubMember, bookReview)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Long clubMemberId = clubManagementAPI.getActiveClubMemberInfo(meeting.getClubId(), memberId);

        // 한줄평 조회 및 존재 여부 확인
        BookReview bookReview = clubBookReviewQueryService.validateBookReview(reviewId, meeting.getId());

        if (!bookReview.getClubMemberId().equals(clubMemberId)) {
            throw new GeneralException(ErrorStatus.BOOK_REVIEW_FORBIDDEN);
        }

        // 한줄평 수정
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
    @Retryable( // OptimisticLockingFailureException 발생 시 재시도
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300) // 300ms 간격으로 재시도
    )
    public void deleteBookReview(Long meetingId, Long reviewId, String memberId) {
        // 유효성 검증 (meeting, clubMember, bookReview)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Long clubMemberId = clubManagementAPI.getActiveClubMemberInfo(meeting.getClubId(), memberId);

        // 한줄평 조회 및 존재 여부 확인
        BookReview bookReview = clubBookReviewQueryService.validateBookReview(reviewId, meetingId);

        // 한줄평 작성자와 삭제자가 같은지 확인
        if (!bookReview.getClubMemberId().equals(clubMemberId)) {
            throw new GeneralException(ErrorStatus.BOOK_REVIEW_FORBIDDEN);
        }

        // 미팅의 별점 합산에서 제외
        meeting.subtractSumRate(bookReview.getRate());

        // 한줄평 삭제
        bookReview.removeMeeting();
    }
}
