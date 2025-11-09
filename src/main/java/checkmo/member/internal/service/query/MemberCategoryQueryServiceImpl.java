package checkmo.member.internal.service.query;

import checkmo.member.internal.entity.MemberCategory;
import checkmo.member.internal.repository.MemberCategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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