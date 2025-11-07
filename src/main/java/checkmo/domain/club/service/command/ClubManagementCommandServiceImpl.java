package checkmo.domain.club.service.command;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.ClubRepository;
import checkmo.domain.club.service.query.ClubQueryService;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubManagementCommandServiceImpl implements ClubManagementCommandService {

    // 자신의 CommandService
    private final ClubCategoryCommandService clubCategoryCommandService;

    // 자신의 QueryService
    private final ClubQueryService clubQueryService;

    // 자신의 Repository
    private final ClubRepository clubRepository;

    @Override
    @Transactional
    public Long createClub(ClubMember clubMember, ClubRequestDTO.ClubDetailDTO request) {
        // 1. 클럽 이름 중복 검사
        if (clubQueryService.isDuplicateClubName(request.getName())) {
            throw new GeneralException(ErrorStatus.CLUB_DUPLICATED_NAME);
        }

        // 2. 클럽 엔티티 생성
        Club club = ClubConverter.fromClubDetailDTOToClub(request);

        // 3. 클럽과 클럽 멤버 연관관계 설정
        club.addClubMember(clubMember);

        // 4. 클럽 저장
        clubRepository.save(club);

        // 5. 카테고리 연관관계 설정
        List<Long> categoryIds = request.getCategory();
        clubCategoryCommandService.createClubCategories(club, categoryIds);

        // 6. 생성된 클럽의 ID 반환
        return club.getId();
    }

    @Override
    @Transactional
    public void updateClub(Club club, ClubMember clubMember, ClubRequestDTO.ClubDetailDTO request) {
        // 1. 운영진 여부 검증
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 클럽 이름 중복 검사 (단, 기존 이름과 다를 때만)
        if (!club.getName().equals(request.getName()) &&
                clubQueryService.isDuplicateClubName(request.getName())) {
            throw new GeneralException(ErrorStatus.CLUB_DUPLICATED_NAME);
        }

        // 3. 엔티티 필드 수정
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
    }
}
