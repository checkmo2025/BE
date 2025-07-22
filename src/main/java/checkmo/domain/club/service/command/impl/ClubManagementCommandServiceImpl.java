package checkmo.domain.club.service.command.impl;

import checkmo.domain.category.facade.CategoryCommandFacade;
import checkmo.domain.category.web.dto.CategoryRequestDTO;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.ClubMemberRepository;
import checkmo.domain.club.repository.ClubRepository;
import checkmo.domain.club.service.command.ClubManagementCommandService;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubManagementCommandServiceImpl implements ClubManagementCommandService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;

    // 외부 도메인 Facade
    private final MemberQueryFacade memberQueryFacade;
    private final CategoryCommandFacade categoryCommandFacade;

    @Override
    public Long createClub(String memberId, ClubRequestDTO.ClubDetailDTO request) {
        // 1. Member 존재 확인
       memberQueryFacade.getMemberBasicInfoForShare(memberId);

        // 2. Club 생성 및 저장
        Club club = ClubConverter.toEntity(request, memberId);
        Club savedClub = clubRepository.save(club);

        // 3. 카테고리 연관관계 설정
        List<Long> categoryIds = request.getCategory();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            CategoryRequestDTO.CategoryListRequestDTO dto = new CategoryRequestDTO.CategoryListRequestDTO(categoryIds);
            categoryCommandFacade.modifyClubCategories(savedClub.getId(), dto);
        }

        // 4. 생성자를 운영진으로 등록
        ClubMember clubMember = ClubConverter.toMemberEntity(savedClub, memberId, ClubMember.ClubMemberStatus.STAFF);
        clubMemberRepository.save(clubMember);

        return savedClub.getId();
    }

}