package checkmo.domain.club.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
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
    public ClubMember validateClubMember(Long clubId, String memberId) throws GeneralException {
        return clubMemberRepository.findByClubIdAndMemberId(clubId, memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_MEMBER_ONLY));
    }

}
