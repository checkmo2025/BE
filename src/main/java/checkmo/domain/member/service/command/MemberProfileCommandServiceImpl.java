package checkmo.domain.member.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.member.entity.MemberCategory;
import checkmo.domain.member.converter.MemberConverter;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.repository.MemberRepository;
import checkmo.domain.member.service.query.MemberCategoryQueryService;
import checkmo.domain.member.web.dto.MemberRequestDTO;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import checkmo.global.s3.service.S3Service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberProfileCommandServiceImpl implements MemberProfileCommandService {

    private final MemberRepository memberRepository;
    private final MemberCategoryCommandService memberCategoryCommandService;
    private final MemberCategoryQueryService memberCategoryQueryService;
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
            // 기존에 저장된 url에서 이미지 키 추출
            final String imageKey = s3Service.extractKeyFromUrl(existingImageUrl);
            if (imageKey != null) {
                // 이 메소드의 트랜잭션이 커밋 되면 기존 이미지 삭제
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        s3Service.deleteImage(imageKey);
                    }
                });
            }
        }

        // 프로필 정보 업데이트 (소개, 이미지)
        member.updateProfile(request.getDescription(), newImageUrl);

        // 관심 카테고리 수정
        if (request.getCategoryIds() != null) {
            memberCategoryCommandService.modifyMemberCategories(memberId, request.getCategoryIds());
        }

        // 수정된 회원의 카테고리 정보 조회
        List<MemberCategory> categoryList = memberCategoryQueryService.findCategoriesByMember(memberId);

        // 카테고리 정보를 DTO로 변환
        var categories = MemberConverter.fromMemberCategoriesToCategoryInfoList(categoryList);

        // 회원 프로필과 카테고리 정보를 포함한 DTO 반환
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