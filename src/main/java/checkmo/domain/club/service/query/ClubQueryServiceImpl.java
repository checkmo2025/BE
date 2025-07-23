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

    @Override
    public boolean isDuplicateClubName(String clubName) {
        return false;
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
