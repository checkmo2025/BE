package checkmo.clubManagement.internal.service.query;

import checkmo.clubManagement.internal.entity.ClubCategory;
import checkmo.clubManagement.internal.repository.ClubCategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubCategoryQueryServiceImpl implements ClubCategoryQueryService {

    // 자신의 Repository
    private final ClubCategoryRepository clubCategoryRepository;

    @Override
    public List<ClubCategory> findCategoriesByClub(Long clubId) {
        return clubCategoryRepository.findByClubId(clubId);
    }

    @Override
    public List<ClubCategory> findCategoriesByClubIds(List<Long> clubIds) {
        return clubCategoryRepository.findByClubIdIn(clubIds);
    }
}