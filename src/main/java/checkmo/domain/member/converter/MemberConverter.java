package checkmo.domain.member.converter;

import checkmo.domain.member.entity.Member;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberConverter {

    // =====================================================
    // Entity → DTO 변환
    // =====================================================

    /**
     * Member 엔티티 → MemberProfileResponseDTO 변환
     */
    public static MemberResponseDTO.MemberProfileResponseDTO toMemberProfileResponseDTO(Member member) {
        return MemberResponseDTO.MemberProfileResponseDTO.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .build();
    }

    /**
     * MemberProfileResponseDTO → BasicInfoDTO 변환
     */
    public static MemberSharedDTO.BasicInfoDTO toBasicInfoDTO(MemberResponseDTO.MemberProfileResponseDTO profile) {
        return MemberSharedDTO.BasicInfoDTO.builder()
                .nickname(profile.getNickname())
                .profileImageUrl(profile.getProfileImageUrl())
                .build();
    }

}
