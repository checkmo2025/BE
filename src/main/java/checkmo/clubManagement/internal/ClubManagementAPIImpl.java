package checkmo.clubManagement.internal;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubManagementAPIImpl implements ClubManagementAPI {

    private final ClubMemberQueryService clubMemberQueryService;

    @Override
    public ClubManagementExternalDTO.MyClubList getMyClubListForShare(String memberId) {
        return clubMemberQueryService.getMyClubList(memberId);
    }

}
