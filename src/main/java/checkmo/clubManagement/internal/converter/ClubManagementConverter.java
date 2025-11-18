package checkmo.clubManagement.internal.converter;

import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.clubManagement.internal.entity.BookRecommend;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.member.MemberExternalDTO;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubManagementConverter {

    public static ClubManagementExternalDTO.Membership toMembershipDTO(ClubMember clubMember) {
        return ClubManagementExternalDTO.Membership.builder()
                .memberId(clubMember.getMemberId())
                .clubMemberId(clubMember.getId())
                .active(clubMember.isActive())
                .staff(clubMember.isStaff())
                .build();
    }

    public static Map<Long, ClubManagementExternalDTO.Membership> toMembereshipDTOMap(List<ClubMember> clubMembers) {
        return clubMembers.stream()
                .collect(
                        Collectors.toMap(
                                ClubMember::getId,
                                ClubManagementConverter::toMembershipDTO
                        )
                );
    }

    public static List<ClubManagementExternalDTO.Membership> toMembershipDTOList(List<ClubMember> clubMembers) {
        return clubMembers.stream()
                .map(ClubManagementConverter::toMembershipDTO)
                .toList();
    }

    public static ClubResponseDTO.ClubInfo toClubInfoDTO(ClubManagementExternalDTO.MyClubInfo myClubInfo) {
        return ClubResponseDTO.ClubInfo.builder()
                .clubId(myClubInfo.getClubId())
                .clubName(myClubInfo.getClubName())
                .open(null)
                .build();
    }

    public static ClubResponseDTO.ClubMember toClubMemberDTO(ClubMember targetMember,
                                                             MemberExternalDTO.BasicInfo memberInfo) {
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

    public static BookRecommend toBookRecommend(
            ClubRequestDTO.CreateBookRecommend request,
            String bookId,
            ClubMember clubMember
    ) {
        return BookRecommend.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .rate(request.getRate())
                .tag(request.getTag())
                .clubMember(clubMember)
                .bookId(bookId)
                .build();
    }

    public static ClubResponseDTO.BookRecommendDetail toBookRecommendDetailDTO(
            BookRecommend bookRecommend,
            BookExternalDTO.BasicInfo bookInfo,
            MemberExternalDTO.BasicInfo authorInfo,
            String currentMemberNickname,
            boolean isStaff
    ) {
        return ClubResponseDTO.BookRecommendDetail.builder()
                .id(bookRecommend.getId())
                .title(bookRecommend.getTitle())
                .content(bookRecommend.getContent())
                .rate(bookRecommend.getRate())
                .tag(bookRecommend.getTag())
                .bookInfo(bookInfo)
                .authorInfo(authorInfo)
                .isAuthor(authorInfo.getNickname().equals(currentMemberNickname))
                .isStaff(isStaff)
                .build();
    }
}
