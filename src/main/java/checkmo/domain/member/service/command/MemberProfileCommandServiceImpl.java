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
import checkmo.global.s3.service.S3Service;
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
    private final S3Service s3Service;

    @Override
    public MemberResponseDTO.MemberProfileWithCategoryResponseDTO updateMemberProfile(
        String memberId, MemberRequestDTO.MemberProfileUpdateRequestDTO request
    ) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
                                        .orElseThrow(() -> new GeneralException(
                                            ErrorStatus.MEMBER_NOT_FOUND));

        // 기존에 저장된 이미지 url 가져오기
        String existingImageUrl = member.getImgUrl();

        // 새로 입력받은 request의 이미지 url 가져오기
        String newImageUrl = request.getImgUrl();

        // 기존 이미지와 새로운 이미지가 다를 경우 S3에서 기존 이미지 삭제
        // 새로운 이미지 url이 null이면 기존 이미지 삭제
        if (existingImageUrl != null && !existingImageUrl.equals(newImageUrl)) {
            String imageKey = s3Service.extractKeyFromUrl(existingImageUrl);
            s3Service.deleteImage(imageKey);
        }

        // 프로필 정보 업데이트 (소개, 이미지)
        member.updateProfile(request.getDescription(), newImageUrl);

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