package checkmo.domain.club.service.query;

import checkmo.domain.club.entity.ClubCategory;
import checkmo.domain.club.repository.ClubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubCategoryQueryServiceImpl implements ClubCategoryQueryService {

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