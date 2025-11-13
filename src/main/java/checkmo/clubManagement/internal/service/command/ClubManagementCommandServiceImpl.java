package checkmo.clubManagement.internal.service.command;

import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.repository.ClubRepository;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO.ClubDetailDTO;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubManagementCommandServiceImpl implements ClubManagementCommandService {

    // 자신의 CommandService
    private final ClubCategoryCommandService clubCategoryCommandService;

    // 자신의 QueryService
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    // 자신의 Repository
    private final ClubRepository clubRepository;

    @Override
    @Transactional
    public Long createClub(String memberId, ClubDetailDTO request) {
        // 1. 운영진 멤버 엔티티 생성
        ClubMember clubMember = ClubManagementConverter
                .toClubMemberEntity(null, memberId, ClubMember.ClubMemberStatus.STAFF, null);

        // 2. 클럽 이름 중복 검사
        if (clubQueryService.isDuplicateClubName(request.getName())) {
            throw new GeneralException(ErrorStatus.CLUB_DUPLICATED_NAME);
        }

        // 3. 클럽 엔티티 생성
        Club club = ClubManagementConverter.fromClubDetailDTOToClub(request);

        // 4. 클럽과 클럽 멤버 연관관계 설정
        club.addClubMember(clubMember);

        // 5. 클럽 저장
        clubRepository.save(club);

        // 6. 카테고리 연관관계 설정
        List<Long> categoryIds = request.getCategory();
        clubCategoryCommandService.createClubCategories(club, categoryIds);

        // 7. 생성된 클럽의 ID 반환
        return club.getId();
    }

    @Override
    @Transactional
    public Long updateClub(Long clubId, String memberId, ClubDetailDTO request) {
        // 1. 유효성 검증(club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 운영진 여부 검증
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. 클럽 이름 중복 검사 (단, 기존 이름과 다를 때만)
        if (!club.getName().equals(request.getName()) &&
                clubQueryService.isDuplicateClubName(request.getName())) {
            throw new GeneralException(ErrorStatus.CLUB_DUPLICATED_NAME);
        }

        // 4. 엔티티 필드 수정
        club.updateField(request.getName(),
                request.getDescription(),
                request.getProfileImageUrl(),
                request.getParticipantTypes(),
                request.getRegion(),
                request.getInsta(),
                request.getKakao());

        // 4. 카테고리 연관관계 수정
        List<Long> categoryIds = request.getCategory();
        clubCategoryCommandService.modifyClubCategories(club, categoryIds);

        return club.getId();
    }
}
