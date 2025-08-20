package checkmo.domain.club.service.command;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.ClubRepository;
import checkmo.domain.club.service.query.ClubMemberQueryService;
import checkmo.domain.club.service.query.ClubQueryService;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.facade.MemberQueryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubManagementCommandServiceImpl implements ClubManagementCommandService {

    // Domain level 2
    private final MemberQueryFacade memberQueryFacade;

    // 자신의 CommandService
    private final ClubCategoryCommandService clubCategoryCommandService;

    // 자신의 QueryService
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    // 자신의 Repository
    private final ClubRepository clubRepository;

    /**
     * 독서모임을 생성합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 생성하기 첫화면 첫스크롤
     *
     * @param request 모임 생성 요청 DTO
     * @return 생성된 독서모임 ID
     */
    @Override
    @Transactional
    public Long createClub(String memberId, ClubRequestDTO.ClubDetailDTO request) {

        // 1. 클럽 이름 중복 검사
        if (clubQueryService.isDuplicateClubName(request.getName())) {
            throw new GeneralException(ErrorStatus.CLUB_DUPLICATED_NAME);
        }

        // 2. 클럽 엔티티 생성
        Club club = ClubConverter.fromClubDetailDTOToClub(request);

        // 3. 클럽 생성자 - ClubMember 생성 및 연관관계 설정
        Member memberProxy = memberQueryFacade.findMemberReferenceById(memberId);

        ClubMember clubMember = ClubMember.builder()
                .club(club)
                .member(memberProxy)
                .clubMemberStatus(ClubMember.ClubMemberStatus.STAFF)
                .build();

        club.addClubMember(clubMember);

        // 4. 클럽 저장
        clubRepository.save(club);

        // 5. 카테고리 연관관계 설정
        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            clubCategoryCommandService.modifyClubCategories(club.getId(), request.getCategory());
        }

        // 6. 생성된 클럽의 ID 반환
        return club.getId();
    }

    /**
     * 독서모임 정보를 수정합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 수정 화면
     *
     * @param clubId   수정할 독서모임 ID
     * @param memberId 수정 요청 회원 ID
     * @param request  수정할 모임 정보 DTO
     */
    @Override
    @Transactional
    public void updateClub(Long clubId, String memberId, ClubRequestDTO.ClubDetailDTO request) {

        // 1. 클럽 유효성 검증
        Club club = clubQueryService.validateClub(clubId);

        // 2. 운영진 권한 확인
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        boolean isStaff = clubMember.isStaff();
        if (!isStaff) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. 클럽 이름 중복 검사 (단, 기존 이름과 다를 때만)
        if (!club.getName().equals(request.getName()) &&
                clubQueryService.isDuplicateClubName(request.getName())) {
            throw new GeneralException(ErrorStatus.CLUB_DUPLICATED_NAME);
        }

        // 4. 엔티티 필드 수정
        club.updateFromDetailDTO(request);

        // 5. 카테고리 연관관계 수정
        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            clubCategoryCommandService.modifyClubCategories(clubId, request.getCategory());
        }
    }
}
