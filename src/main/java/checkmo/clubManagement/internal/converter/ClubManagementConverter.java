package checkmo.clubManagement.internal.converter;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.member.MemberExternalDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static checkmo.clubManagement.ClubManagementExternalDTO.BasicInfo;
import static checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubManagementConverter {

    public static MembershipInfo toMembershipDTO(ClubMember clubMember) {
        return MembershipInfo.builder()
                .memberId(clubMember.getMemberId())
                .clubMemberId(clubMember.getId())
                .active(clubMember.isActive())
                .staff(clubMember.isStaff())
                .build();
    }

    public static Map<Long, MembershipInfo> toMembereshipDTOMap(
            List<ClubMember> clubMembers) {
        return clubMembers.stream()
                .collect(
                        Collectors.toMap(
                                ClubMember::getId,
                                ClubManagementConverter::toMembershipDTO
                        )
                );
    }

    public static List<MembershipInfo> toMembershipDTOList(List<ClubMember> clubMembers) {
        return clubMembers.stream()
                .map(ClubManagementConverter::toMembershipDTO)
                .toList();
    }

    public static ClubResponseDTO.ClubInfo toClubInfoDTO(BasicInfo basicInfo) {
        return ClubResponseDTO.ClubInfo.builder()
                .clubId(basicInfo.getClubId())
                .clubName(basicInfo.getClubName())
                .open(null)
                .build();
    }

    public static ClubResponseDTO.ClubMember toClubMemberDTO(
            ClubMember targetMember,
            MemberExternalDTO.BasicInfo memberInfo
    ) {
        return ClubResponseDTO.ClubMember.builder()
                .clubMemberId(targetMember.getId())
                .basicInfo(memberInfo)
                .joinMessage(targetMember.getJoinMessage())
                .clubMemberStatus(targetMember.getClubMemberStatus().name())
                .build();
    }

    public static Club toClub(ClubRequestDTO.ClubDetail dto) {
        return Club.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .profileImgUrl(dto.getProfileImageUrl())
                .open(dto.isOpen())
                .region(dto.getRegion())
                .insta(dto.getInsta())
                .kakao(dto.getKakao())
                .participantTypes(dto.getParticipantTypes())
                .build();
    }

    public static ClubResponseDTO.ClubDetail toClubDetailDTO(Club club, boolean isStaff) {
        return ClubResponseDTO.ClubDetail.builder()
                .clubId(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .profileImageUrl(club.getProfileImgUrl())
                .open(club.isOpen())
                .category(club.getInterestCategories().stream().toList())
                .region(club.getRegion())
                .participantTypes(club.getParticipantTypes())
                .insta(club.getInsta())
                .kakao(club.getKakao())
                .isStaff(isStaff)
                .build();
    }

}
