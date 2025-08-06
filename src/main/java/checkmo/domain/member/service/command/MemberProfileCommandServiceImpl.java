package checkmo.domain.member.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.category.facade.CategoryCommandFacade;
import checkmo.domain.category.facade.CategoryQueryFacade;
import checkmo.domain.member.converter.MemberConverter;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.repository.MemberRepository;
import checkmo.domain.member.web.dto.MemberRequestDTO;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import checkmo.global.dto.CategorySharedDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberProfileCommandServiceImpl implements MemberProfileCommandService {

    private final MemberRepository memberRepository;
    private final CategoryCommandFacade categoryCommandFacade;
    private final CategoryQueryFacade categoryQueryFacade;

    @Override
    public MemberResponseDTO.MemberProfileWithCategoryResponseDTO updateMemberProfile(
        String memberId, MemberRequestDTO.MemberProfileUpdateRequestDTO request
    ) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
                                        .orElseThrow(() -> new GeneralException(
                                            ErrorStatus.MEMBER_NOT_FOUND));

        // 프로필 정보 업데이트 (소개, 이미지)
        member.updateProfile(request.getDescription(), request.getImgUrl());

        // 관심 카테고리 수정
        if (request.getCategoryIds() != null) {
            categoryCommandFacade.modifyMemberCategories(memberId,
                CategorySharedDTO.CategoryIdListDTO.builder()
                                                   .categoryIdList(request.getCategoryIds())
                                                   .build());
        }

        List<CategorySharedDTO.CategoryInfo> categories = categoryQueryFacade.getCategoriesByMemberForShare(memberId).getCategoryList();

        return MemberConverter.toMemberProfileWithCategoryResponseDTO(member, categories);
    }

    @Override
    public void updatePassword(
        String memberId, MemberRequestDTO.PasswordUpdateRequestDTO request
    ) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public void deactivateMember(String memberId) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public void deleteMember(String memberId) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }
}