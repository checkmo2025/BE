package checkmo.clubManagement.internal.service.query;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubInterestCategory;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.repository.ClubRepository;
import checkmo.clubManagement.internal.repository.projection.ClubIdAndName;
import checkmo.clubManagement.internal.repository.projection.ClubRecommendation;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubManagementQueryService {

    private final ClubRepository clubRepository;

    public List<Club> retrieveClubs(ClubRequestDTO.ClubSearchFilter filter, Long cursorId, int pageSize) {
        return clubRepository.searchClubs(filter, cursorId, pageSize);
    }

    public boolean isDuplicateClubName(String clubName) {
        return clubRepository.existsByName(clubName.trim());
    }

    public Club validateClub(Long clubId) throws ClubManagementException {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubManagementException(ClubManagementErrorStatus.CLUB_NOT_FOUND));
    }

    public Map<Long, String> retrieveClubNamesByIds(List<Long> clubIds) {
        List<ClubIdAndName> results = clubRepository.findIdAndNameByIdIn(clubIds);
        return results.stream()
                .collect(Collectors.toMap(
                        ClubIdAndName::getId,
                        ClubIdAndName::getName
                ));
    }

    public List<ClubRecommendation> recommend(
            EnumSet<ClubInterestCategory> interestCategories,
            LocalDateTime lastActivityAt,
            String memberId
    ) {
        return clubRepository.findRecommendations(interestCategories, lastActivityAt, memberId, 3);
    }
}
