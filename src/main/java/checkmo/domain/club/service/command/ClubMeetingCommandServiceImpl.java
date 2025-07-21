package checkmo.domain.club.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.book.entity.Book;
import checkmo.domain.book.facade.BookCommandFacade;
import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.entity.announcement.Notice;
import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.domain.club.repository.meeting.MeetingRepository;
import checkmo.domain.club.service.query.ClubMemberQueryService;
import checkmo.domain.club.service.query.ClubQueryService;
import checkmo.domain.club.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;
import checkmo.global.dto.BookSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubMeetingCommandServiceImpl implements ClubMeetingCommandService {
    private final BookCommandFacade bookCommandFacade;
    private final BookQueryFacade bookQueryFacade;
    private final ClubMemberQueryService clubMemberQueryService;
    private final ClubQueryService clubQueryService;
    private final ClubCommunicationCommandService clubCommunicationCommandService;
    private final MeetingRepository meetingRepository;

    @Override
    public Long createMeeting(Long clubId, String memberId, MeetingRequestDTO.MeetingCreateRequestDTO request) {
        // 1. 검증
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 책 저장 후 프록시 가져오기
        ClubRequestDTO.BookDetailDTO bookDetail = request.getBookDetail();
        bookCommandFacade.saveBook(
                BookSharedDTO.BookCreateRequestDTO.builder()
                        .isbn(bookDetail.getIsbn())
                        .title(bookDetail.getTitle())
                        .author(bookDetail.getAuthor())
                        .imgUrl(bookDetail.getImgUrl())
                        .publisher(bookDetail.getPublisher())
                        .description(bookDetail.getDescription())
                        .build()
        );
        Book proxyBook = bookQueryFacade.findBookReferenceById(bookDetail.getIsbn());

        // 3. 미팅 생성 후 Book 연결
        Meeting meeting = ClubConverter.fromMeetingCreateRequestDTOToMeeting(request);
        meeting.setBook(proxyBook);

        // 4. 공지 생성
        Notice notice = ClubConverter.fromMeetingToNotice(meeting);

        // 5. 연관관계 설정
        club.addMeeting(meeting);
        meeting.addNotice(notice);

        // 6. 명시적 저장
        meetingRepository.save(meeting);

        return meeting.getId();
    }

    @Override
    public Long updateMeeting(Long meetingId, String memberId, MeetingRequestDTO.MeetingUpdateRequestDTO request) {
        return 0L;
    }

    @Override
    public Long createTopic(Long memberId, Long meetingId, MeetingRequestDTO.TopicDTO request) {
        return 0L;
    }

    @Override
    public Long toggleTopic(String memberId, Long meetingId, MeetingRequestDTO.TopicManageDTO request) {
        return 0L;
    }

    @Override
    public Long updateTopic(String memberId, Long meetingId, Long topicId, MeetingRequestDTO.TopicDTO request) {
        return 0L;
    }

    @Override
    public void deleteTopic(String memberId, Long meetingId, Long topicId) {

    }

    @Override
    public Long manageTeam(String memberId, Long meetingId, MeetingRequestDTO.TeamManageDTO request) {
        return 0L;
    }

    @Override
    public Long createBookReview(String memberId, Long meetingId, BookShelfRequestDTO.BookReviewDTO request) {
        return 0L;
    }

    @Override
    public Long updateBookReview(String memberId, Long meetingId, Long reviewId, BookShelfRequestDTO.BookReviewDTO request) {
        return 0L;
    }

    @Override
    public void deleteBookReview(String memberId, Long meetingId, Long reviewId) {

    }
}
