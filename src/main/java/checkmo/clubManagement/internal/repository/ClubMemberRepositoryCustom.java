package checkmo.clubManagement.internal.repository;

import checkmo.clubManagement.internal.entity.ClubMember;
import java.util.List;

public interface ClubMemberRepositoryCustom {
    List<ClubMember> findClubMembersByClubIdInClubMemberStatusOrderByIdDesc(
            Long clubId,
            List<ClubMember.ClubMemberStatus> statuses,
            Long cursorId,
            Integer size
    );

    boolean isMemberInClub(String memberId, Long clubId);

    List<String> findActiveMemberIdsByClubId(Long clubId);
}
