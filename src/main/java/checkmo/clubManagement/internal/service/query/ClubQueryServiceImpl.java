package checkmo.clubManagement.internal.service.query;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.repository.ClubRepository;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubQueryServiceImpl implements ClubQueryService {

    private final ClubRepository clubRepository;

    @Override
    public List<Club> getClubList(ClubRequestDTO.ClubSearchFilter filter, Long cursorId, int pageSize) {
        return clubRepository.searchClubs(filter, cursorId, pageSize);
    }

    @Override
    public Club getClubInfo(Long clubId) {
        return validateClub(clubId);
    }

    @Override
    public boolean isDuplicateClubName(String clubName) {
        return clubRepository.existsByName(clubName);
    }

    @Override
    public Club validateClub(Long clubId) throws ClubManagementException {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubManagementException(ClubManagementErrorStatus.CLUB_NOT_FOUND));
    }
}
