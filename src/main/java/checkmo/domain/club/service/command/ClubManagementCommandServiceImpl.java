package checkmo.domain.club.service.command;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.domain.category.entity.Category;
import checkmo.domain.category.entity.ClubCategory;
import checkmo.domain.category.facade.CategoryQueryFacade;
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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubManagementCommandServiceImpl implements ClubManagementCommandService {

    private final ClubRepository clubRepository;
    private final ClubQueryService clubQueryService;
    private final CategoryQueryFacade categoryQueryFacade;
    private final MemberQueryFacade memberQueryFacade;

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

        // 2. 클럽 엔티티 우선 생성 (카테고리는 이후 설정하므로 비워둠)
        Club club = ClubConverter.fromClubDetailDTOToClub(request);

        // 3. ClubCategory 생성
        List<ClubCategory> categories = new ArrayList<>();
        for (Long categoryId : request.getCategory()) {

            // 카테고리 ID 유효성 검사 -> 1 ~ 15 사이의 값이어야 함 (변경이 거의 없으므로 하드 코딩)
            if (categoryId < 1 || categoryId > 15) {
                throw new GeneralException(ErrorStatus.CLUB_CATEGORY_NOT_FOUND, "카테고리 ID " + categoryId + " 는 유효하지 않습니다.");
            }

            // Category 프록시 객체 조회
            Category categoryProxy = categoryQueryFacade.findCategoryReferenceById(categoryId);

            // ClubCategory 엔티티 생성
            ClubCategory clubCategory = ClubCategory.builder()
                    .club(club)
                    .category(categoryProxy)
                    .build();

            categories.add(clubCategory);
        }

        // 4. 연관관계 설정 (Club -> ClubCategory)
        club.addCategories(categories);

        // 5. 클럽 생성자 - ClubMember 생성 및 연관관계 설정
        Member memberProxy = memberQueryFacade.findMemberReferenceById(memberId); // Member 엔티티 프록시 조회

        ClubMember clubMember = ClubMember.builder()
                .club(club)
                .member(memberProxy)
                .clubMemberStatus(ClubMember.ClubMemberStatus.STAFF)
                .build();

        club.addClubMember(clubMember); // Club -> ClubMember 양방향 연관관계 설정

        // 6. 클럽 저장
        clubRepository.save(club);

        // 7. 생성된 클럽의 ID 반환
        return club.getId();
    }

}