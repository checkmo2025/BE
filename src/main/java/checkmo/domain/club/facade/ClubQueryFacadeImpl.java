package checkmo.domain.club.facade;

import checkmo.domain.club.entity.Club;
import checkmo.domain.club.repository.ClubRepository;
import checkmo.domain.club.service.query.ClubBookRecommendQueryService;
import checkmo.domain.club.service.query.ClubQueryService;
import checkmo.domain.club.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.club.web.dto.meeting.MeetingResponseDTO;
import checkmo.global.dto.ClubSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubQueryFacadeImpl implements ClubQueryFacade {

    private final ClubQueryService clubQueryService;
    private final ClubBookRecommendQueryService clubBookRecommendQueryService;
    private final ClubRepository clubRepository; // 프록시용

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

    /**
     * ClubQueryService
     * 독서 모임의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId   조회할 모임 ID
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

    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO getNoticeDetail(Long clubId, Long noticeId) {
        return null;
    }

    /**
     * ClubBookRecommendQueryService
     * 모임의 추천 책 목록을 조회합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param cursorId 페이징 커서 ID
     * @return 추천 책 목록 DTO
     */
    @Override
    public ClubResponseDTO.BookRecommendListDTO getRecommendedBooks(Long clubId, Long cursorId, String memberId) {
        return clubBookRecommendQueryService.getRecommendedBooks(clubId, cursorId, memberId);
    }

    /**
     * ClubBookRecommendQueryService
     * 추천 책의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId          모임 ID
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
        return clubRepository.getReferenceById(clubId);
    }
}
