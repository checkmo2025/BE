package checkmo.domain.club.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.repository.ClubRepository;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubQueryServiceImpl implements ClubQueryService {

    private final ClubRepository clubRepository;

    @Override
    public ClubResponseDTO.ClubListDTO getClubList(String keyword, int region, int participants, Long cursorId) {
        return null;
    }

    @Override
    public ClubResponseDTO.MyClubListDTO getMyClubList(String memberId) {
        return null;
    }

    @Override
    public ClubResponseDTO.MyClubListDTO getMyClubList(String memberId, int size) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubMemberListDTO getClubMemberListByStatus(Long clubId, String memberId, String clubMemberStatus, Long cursorId) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubDetailDTO getClubInfo(Long clubId, String memberId) {
        return null;
    }

    /**
     * 독서모임의 이름 중복 여부를 확인 합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 생성하기 첫화면 첫스크롤
     *
     * @param clubName 독서모임 이름
     * @return 중복 여부 (true: 중복, false: 중복 아님)
     */
    @Override
    public boolean isDuplicateClubName(String clubName) {
        return clubRepository.existsByName(clubName);
    }

    @Override
    public ClubResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, int size) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, Long cursorId) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO getNoticeDetail(Long clubId, Long noticeId) {
        return null;
    }

    @Override
    public Club validateClub(Long clubId) throws GeneralException {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_NOT_FOUND));
    }
}
