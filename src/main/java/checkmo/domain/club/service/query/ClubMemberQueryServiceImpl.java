package checkmo.domain.club.service.query;

import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.ClubMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMemberQueryServiceImpl implements ClubMemberQueryService {

    private final ClubMemberRepository clubMemberRepository;

    @Override
    public boolean isClubMember(Long clubId, String memberId) {
        return clubMemberRepository.existsByClubIdAndMemberId(clubId, memberId);
    }

    @Override
    public boolean isClubStaff(Long clubId, String memberId) {
        return clubMemberRepository.existsByClubIdAndMemberIdAndClubMemberStatus(clubId, memberId, ClubMember.ClubMemberStatus.STAFF);
    }

}
