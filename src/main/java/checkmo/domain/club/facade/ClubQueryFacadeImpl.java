package checkmo.domain.club.facade;

import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.meeting.BookReview;
import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.domain.club.service.query.ClubMeetingQueryService;
import checkmo.domain.club.service.query.ClubMemberQueryService;
import checkmo.domain.club.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.club.web.dto.meeting.MeetingResponseDTO;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.global.dto.ClubSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubQueryFacadeImpl implements ClubQueryFacade {

    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubMemberQueryService clubMemberQueryService;
    private final MemberQueryFacade memberQueryFacade;

    @Override
    public ClubSharedDTO.MyClubListDTO getMyClubListForShare(String memberId) {
        return null;
    }

    @Override
    public ClubSharedDTO.MyClubListDTO getMyClubListForShare(String memberId, int size) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubListDTO getClubList(String keyword, int region, int participants, Long cursorId) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubDetailDTO getClubInfo(Long clubId, String memberId) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubMemberListDTO getClubMemberListByStatus(Long clubId, String memberId, String clubMemberStatus, Long cursorId) {
        return null;
    }

    @Override
    public boolean isDuplicateClubName(String clubName) {
        return false;
    }

    @Override
    public ClubResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, Long cursorId) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubNoticeListDTO getImportantNotices(Long clubId, String memberId, int size) {
        return null;
    }

    @Override
    public ClubSharedDTO.ClubUpdatePreviewListDTO getNoticeForHome(String memberId, int size) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO getNoticeDetail(Long clubId, Long noticeId) {
        return null;
    }

    @Override
    public ClubResponseDTO.BookRecommendListDTO getRecommendedBooks(Long clubId, Long cursorId) {
        return null;
    }

    @Override
    public ClubResponseDTO.BookRecommendDetailDTO getRecommendedBookDetail(Long clubId, Long bookRecommendId) {
        return null;
    }

    @Override
    public BookShelfResponseDTO.BookShelfListDTO getBookShelfList(Long clubId, Long cursorId) {
        return null;
    }

    @Override
    public BookShelfResponseDTO.BookShelfDetailDTO getBookShelfDetail(Long meetingId) {
        return null;
    }

    @Override
    public BookShelfResponseDTO.BookReviewListDTO getBookReviewList(Long meetingId, Long lastReviewId, int size, String memberId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        List<BookReview> bookReviews = clubMeetingQueryService.findBookReviewsByMeeting(meetingId, lastReviewId, size + 1);

        boolean hasNext = bookReviews.size() > size;
        if (hasNext) {
            bookReviews = bookReviews.subList(0, size);
        }
        Long nextCursor = hasNext ? bookReviews.get(bookReviews.size() - 1).getId() : null;

        List<BookShelfResponseDTO.BookReviewDTO> bookReviewDTOs = bookReviews.stream()
                .map(review -> ClubConverter.fromBookReviewAndMemberSharedDTOToBookReviewDTO(
                        review,
                        memberQueryFacade.getMemberBasicInfoForShare(review.getClubMember().getMemberId())
                ))
                .toList();

        return ClubConverter.fromBookReviewDTOListToBookReviewListDTO(bookReviewDTOs, hasNext, nextCursor);
    }

    @Override
    public MeetingResponseDTO.MeetingListDTO findAllMeetingsByClub(Long clubId, Long cursorId) {
        return null;
    }

    @Override
    public MeetingResponseDTO.InProgressMeetingDetailDTO findMeetingById(Long meetingId) {
        return null;
    }

    @Override
    public MeetingResponseDTO.TopicListDTO findTopicsByMeeting(Long meetingId, Long cursorId) {
        return null;
    }

    @Override
    public MeetingResponseDTO.TeamDTO findTeamDetailsByMeeting(Long meetingId, Integer teamNumber) {
        return null;
    }

    @Override
    public Club findClubReferenceById(Long clubId) {
        return null;
    }
}
