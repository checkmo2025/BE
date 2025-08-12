package checkmo.domain.club.repository;

import checkmo.domain.club.entity.ClubMember;

import java.util.List;

public interface ClubMemberRepositoryCustom {
    List<ClubMember> findClubMembersByClubIdInClubMemberStatusOrderByIdDesc(
            Long clubId,
            List<ClubMember.ClubMemberStatus> statuses,
            Long cursorId,
            Integer size
    );
}
