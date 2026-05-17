package checkmo.member.internal.repository;

import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberInterestCategory;
import java.util.List;

public interface MemberRepositoryCustom {
    List<Member> findRecommendMembers(
            String currentMemberId,
            List<MemberInterestCategory> myInterests,
            List<String> excludedMemberIds,
            int limit
    );
}
