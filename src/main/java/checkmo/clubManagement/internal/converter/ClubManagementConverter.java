package checkmo.clubManagement.internal.converter;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubContact;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.entity.ClubMemberStatus;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubRequestDTO.ClubDetail;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO.ClubCategoryItem;
import checkmo.clubManagement.web.dto.ClubResponseDTO.ClubDetail.ClubDetailBuilder;
import checkmo.clubManagement.web.dto.ClubResponseDTO.ClubMember.ClubMemberBuilder;
import checkmo.clubManagement.web.dto.ClubResponseDTO.ClubParticipantTypeItem;
import checkmo.member.MemberExternalDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubManagementConverter {

    public static Club toClub(ClubDetail dto) {
        return Club.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .profileImgUrl(dto.getProfileImageUrl())
                .isOpen(dto.isOpen())
                .region(dto.getRegion())
                .participantTypes(dto.getParticipantTypes())
                .links(toClubContacts(dto.getLinks()))
                .build();
    }

    public static List<ClubContact> toClubContacts(List<ClubRequestDTO.Contact> links) {
        if (links == null) {
            return List.of();
        }
        return links.stream()
                .map(l -> new ClubContact(
                        l.getLink() == null ? null : l.getLink().trim(),
                        l.getLabel() == null ? null : l.getLabel().trim()
                ))
                .toList();
    }

    public static MembershipInfo toMembershipDTO(ClubMember clubMember) {
        return MembershipInfo.builder()
                .memberId(clubMember.getMemberId())
                .clubMemberId(clubMember.getId())
                .active(clubMember.isActive())
                .staff(clubMember.isStaff())
                .build();
    }

    public static Map<Long, MembershipInfo> toMembershipDTOMap(
            List<ClubMember> clubMembers
    ) {
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

    public static ClubResponseDTO.ClubMember toClubMemberDTO(
            ClubMember clubMember,
            MemberExternalDTO.DetailInfo memberInfo
    ) {
        ClubMemberStatus status = clubMember.getClubMemberStatus();
        ClubMemberBuilder builder = ClubResponseDTO.ClubMember.builder()
                .clubMemberId(clubMember.getId())
                .detailInfo(memberInfo)
                .clubMemberStatus(status.name());
        if (status.isJoinInProgress()) {
            builder.joinMessage(clubMember.getJoinMessage())
                    .appliedAt(clubMember.getAppliedAt());
        } else if (status.isActive()) {
            builder.joinedAt(clubMember.getJoinedAt());
        }
        return builder.build();
    }

    public static ClubResponseDTO.ClubDetail toClubDetailDTO(Club club, boolean isDetail) {
        ClubDetailBuilder builder = ClubResponseDTO.ClubDetail.builder()
                .clubId(club.getId())
                .name(club.getName())
                .profileImageUrl(club.getProfileImgUrl())
                .isOpen(club.isOpen())
                .region(club.getRegion())
                .category(mapToItems(club.getInterestCategories(), ClubCategoryItem::from))
                .participantTypes(mapToItems(club.getParticipantTypes(), ClubParticipantTypeItem::from));
        if (isDetail) {
            builder.description(club.getDescription())
                    .links(mapToItems(club.getLinks(), ClubResponseDTO.ClubContactItem::from));
        }
        return builder.build();
    }

    private static <S, T> List<T> mapToItems(Collection<S> source, Function<S, T> mapper) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return source.stream().map(mapper).toList();
    }
}
