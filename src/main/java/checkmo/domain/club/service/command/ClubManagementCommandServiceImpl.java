package checkmo.domain.club.service.command;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.domain.category.facade.CategoryCommandFacade;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.ClubRepository;
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

    private final ClubRepository clubRepository;
    private final ClubQueryService clubQueryService;
    private final MemberQueryFacade memberQueryFacade;
    private final CategoryCommandFacade categoryCommandFacade;

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
        categoryCommandFacade.modifyClubCategories(club.getId(), request.toCategoryListRequestDTO());

        // 6. 생성된 클럽의 ID 반환
        return club.getId();
    }

}
