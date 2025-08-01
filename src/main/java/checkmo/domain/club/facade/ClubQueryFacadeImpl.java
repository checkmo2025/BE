package checkmo.domain.club.facade;

import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.entity.meeting.BookReview;
import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.domain.club.entity.meeting.Topic;
import checkmo.domain.club.repository.ClubRepository;
import checkmo.domain.club.service.query.*;
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
    private final ClubQueryService clubQueryService;
    private final ClubBookRecommendQueryService clubBookRecommendQueryService;
    private final ClubCommunicationQueryService clubCommunicationQueryService;

    private final ClubRepository clubRepository; // 프록시용

    private final MemberQueryFacade memberQueryFacade;
    private final BookQueryFacade bookQueryFacade;

    @Override
    public ClubSharedDTO.MyClubList getMyClubListForShare(String memberId) {
        return clubMemberQueryService.getMyClubList(memberId);
    }

    @Override
    public ClubResponseDTO.ClubListDTO getClubList(String keyword, int region, int participants, Long cursorId) {
        return null;
    }

    /**
     * ClubQueryService
     * 독서 모임의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId 조회할 모임 ID
     * @param memberId 조회자 회원 ID
     * @return 모임 상세 정보 DTO
     */
    @Override
    public ClubResponseDTO.ClubDetailDTO getClubInfo(Long clubId, String memberId) {
        return clubQueryService.getClubInfo(clubId, memberId);
    }

    @Override
    public ClubResponseDTO.ClubMemberListDTO getClubMemberListByStatus(Long clubId, String memberId, String clubMemberStatus, Long cursorId) {
        return null;
    }

    /**
     * ClubQueryService
     * 모임 이름의 중복 여부를 확인합니다. (내부용)
     *
     * @param clubName 확인할 모임 이름
     * @return 중복 시 true
     */
    @Override
    public boolean isDuplicateClubName(String clubName) {
        return clubQueryService.isDuplicateClubName(clubName);
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

    /**
     * ClubQueryService
     * 공지사항(투표 포함)의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param noticeId 조회할 공지사항 ID
     * @return 공지사항 상세 정보 DTO
     */
    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO getNoticeDetail(Long clubId, Long noticeId, String tag, String memberId) {
        return clubCommunicationQueryService.getNoticeOrVoteDetail(clubId, noticeId, tag, memberId);
    }

    /**
     * ClubBookRecommendQueryService
     * 모임의 추천 책 목록을 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param cursorId 페이징 커서 ID
     * @return 추천 책 목록 DTO
     */
    @Override
    public ClubResponseDTO.BookRecommendListDTO getRecommendedBooks(Long clubId, Long cursorId, String memberId) {

        // 1. 클럽 검증
        clubQueryService.validateClub(clubId);

        // 2. 클럽 멤버 검증
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 3. 커서 초기화 (페이징 로직)
        Long cursor = (cursorId == null || cursorId == 0L) ? Long.MAX_VALUE : cursorId;

        // 4. ServiceImpl에서 순수 엔티티 조회
        var bookRecommends = clubBookRecommendQueryService.getRecommendedBooks(clubId, cursor, memberId);

        // 5. 외부 도메인 정보 조합 (Facade에서 처리)
        var currentMemberNickname = memberQueryFacade.getMemberBasicInfoForShare(memberId).getNickname();

        var dtoList = bookRecommends.stream()
                .map(bookRecommend -> {
                    var bookInfo = bookQueryFacade.getBookBasicInfoForShare(bookRecommend.getBookId());
                    var authorInfo = memberQueryFacade.getMemberBasicInfoForShare(bookRecommend.getClubMember().getMemberId());
                    return ClubConverter.toBookRecommendDetailDTO(bookRecommend, bookInfo, authorInfo, currentMemberNickname, clubMember.isStaff());
                }).toList();

        // 6. 페이징 처리 (Facade에서)
        Long lastId = bookRecommends.isEmpty() ? null : bookRecommends.get(bookRecommends.size() - 1).getId();
        boolean hasNext = clubBookRecommendQueryService.hasNextPage(clubId, lastId);

        return ClubConverter.toBookRecommendListDTO(dtoList, hasNext, lastId);
    }

    /**
     * ClubBookRecommendQueryService
     * 추천 책의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param bookRecommendId 추천 책 ID
     * @return 추천 책 상세 정보 DTO
     */
    @Override
    public ClubResponseDTO.BookRecommendDetailDTO getRecommendedBookDetail(Long clubId, Long bookRecommendId, String memberId) {
        return clubBookRecommendQueryService.getRecommendedBookDetail(clubId, memberId, bookRecommendId);
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
    public MeetingResponseDTO.MeetingListDTO getMeetingsByClub(Long clubId, Long cursorId, Integer size, String memberId) {
        clubQueryService.validateClub(clubId);
        clubMemberQueryService.validateClubMember(clubId, memberId);

        List<Meeting> meetings = clubMeetingQueryService.findMeetingsByClubAndCursor(clubId, cursorId, size);
        boolean hasNext = meetings.size() > size;
        if (hasNext) {
            meetings = meetings.subList(0, size);
        }
        Long nextCursor = hasNext ? meetings.getLast().getId() : null;

        List<MeetingResponseDTO.MeetingInfoDTO> meetingInfoDTOList = meetings.stream()
                .map(meeting -> ClubConverter.fromMeetingAndBookSharedDTOToMeetingInfoDTO(
                        meeting,
                        bookQueryFacade.getBookBasicInfoForShare(meeting.getBookId())
                ))
                .toList();
        return ClubConverter.fromMeetingInfoDTOListToMeetingListDTO(meetingInfoDTOList, hasNext, nextCursor);
    }

    @Override
    public MeetingResponseDTO.InProgressMeetingDetailDTO findMeetingById(Long meetingId) {
        return null;
    }

    @Override
    public BookShelfResponseDTO.TopicListDTO findTopicsByMeeting(Long meetingId, Long cursorId, Integer size, String memberId) {
        List<Topic> topics = clubMeetingQueryService.findTopicsByMeeting(meetingId, cursorId, size, memberId);

        boolean hasNext = topics.size() > size;
        if (hasNext) {
            topics = topics.subList(0, size);
        }
        Long nextCursor = hasNext ? topics.getLast().getId() : null;

        List<BookShelfResponseDTO.TopicDTO> topicListDTOs = topics.stream()
                .map(topic -> ClubConverter.fromTopicAndMemberSharedDTOToTopicDTO(
                        topic,
                        memberQueryFacade.getMemberBasicInfoForShare(topic.getClubMember().getMemberId()),
                        memberId
                ))
                .toList();

        return ClubConverter.fromTopicListToTopicListDTO(topicListDTOs, hasNext, nextCursor);
    }

    @Override
    public MeetingResponseDTO.TeamDTO findTeamDetailsByMeeting(Long meetingId, Integer teamNumber) {
        return null;
    }

    @Override
    public Club findClubReferenceById(Long clubId) {
        return clubRepository.getReferenceById(clubId);
    }
}
