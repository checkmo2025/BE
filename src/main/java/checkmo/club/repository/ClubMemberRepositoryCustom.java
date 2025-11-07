package checkmo.club.repository;

import checkmo.club.entity.ClubMember;

import java.util.List;

public interface ClubMemberRepositoryCustom {
    List<ClubMember> findClubMembersByClubIdInClubMemberStatusOrderByIdDesc(
            Long clubId,
            List<ClubMember.ClubMemberStatus> statuses,
            Long cursorId,
            Integer size
    );
}
