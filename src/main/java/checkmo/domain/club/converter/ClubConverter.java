package checkmo.domain.club.converter;

import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubConverter {

    // =====================================================
    // DTO → Entity 변환
    // =====================================================

    /**
     * ClubDetailDTO → Club 엔티티 변환
     */
    public static Club toEntity(ClubRequestDTO.ClubDetailDTO dto, String memberId) {
        return Club.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .profileImgUrl(dto.getProfileImageUrl())
                .isOpen(dto.isOpen())
                .region(dto.getRegion())
                .purpose(dto.getPurpose())
                .participants(dto.getParticipants())
                .insta(dto.getInsta())
                .kakao(dto.getKakao())
                .build();
    }

    /**
     * ClubMember 엔티티 생성
     */
    public static ClubMember toMemberEntity(Club club, String memberId, ClubMember.ClubMemberStatus status) {
        return ClubMember.builder()
                .club(club)
                .memberId(memberId)
                .clubMemberStatus(status)
                .build();
    }

}
