package checkmo.domain.member.service.query;

import checkmo.domain.member.entity.MemberCategory;
import checkmo.domain.member.repository.MemberCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberCategoryQueryServiceImpl implements MemberCategoryQueryService {

    // 자신의 Repository
    private final MemberCategoryRepository memberCategoryRepository;

    @Override
    public List<MemberCategory> findCategoriesByMember(String memberId) {
        return memberCategoryRepository.findByMemberId(memberId);
    }
}