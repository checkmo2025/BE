package checkmo.clubManagement.internal.converter;

import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.clubManagement.internal.entity.BookRecommend;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO.ClubDetail;
import checkmo.member.MemberExternalDTO;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubManagementConverter {

    // =====================================================
    // Entity ↔ DTO 변환
    // =====================================================

    public static Map<Long, ClubManagementExternalDTO.Membership> fromClubMembertoMembereshipDTO(
            List<ClubMember> clubMembers) {
        return clubMembers.stream()
                .collect(
                        java.util.stream.Collectors.toMap(
                                checkmo.clubManagement.internal.entity.ClubMember::getId,
                                ClubManagementConverter::fromClubMembertoMembershipDTO
                        )
                );
    }

    /**
     * ClubMember 엔티티 -> MembershipResponseDTO.MembershipDTO 변환
     */
    public static ClubManagementExternalDTO.Membership fromClubMembertoMembershipDTO(ClubMember clubMember) {
        return ClubManagementExternalDTO.Membership.builder()
                .memberId(clubMember.getMemberId())
                .clubMemberId(clubMember.getId())
                .active(clubMember.isActive())
                .staff(clubMember.isStaff())
                .build();
    }

    /**
     * ClubMember 엔티티 리스트 -> List<MembershipResponseDTO.MembershipDTO> 변환
     */
    public static List<ClubManagementExternalDTO.Membership> fromClubMemberToMembershipDTO(
            List<ClubMember> clubMembers) {
        return clubMembers.stream()
                .map(ClubManagementConverter::fromClubMembertoMembershipDTO)
                .toList();
    }

    /**
     * ClubResponseDTO.ClubDetailResponseDTO -> ClubResponseDTO.MyPageClubListDTO 변환
     */
    public static ClubResponseDTO.MyPageClubList toMyPageClubListDTO(
            List<ClubDetail> clubList,
            boolean hasNext,
            Long nextCursor
    ) {
        return ClubResponseDTO.MyPageClubList.builder()
                .clubList(clubList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * Club 리스트 → ClubResponseDTO.ClubListDTO 변환
     */
    public static ClubResponseDTO.ClubList toClubListDTO(
            List<ClubResponseDTO.ClubWithMyStatus> clubList,
            boolean hasNext,
            Long nextCursor) {

        List<ClubResponseDTO.ClubWithMyStatus> safeList =
                (clubList == null) ? List.of() : List.copyOf(clubList);

        return ClubResponseDTO.ClubList.builder()
                .clubList(safeList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(safeList.size())
                .build();
    }

    /**
     * Club 가입 정보 -> ClubMember 엔티티 변환
     */
    public static ClubMember toClubMemberEntity(
            Club club,
            String memberId,
            ClubMember.ClubMemberStatus status,
            String joinMessage
    ) {
        return checkmo.clubManagement.internal.entity.ClubMember.builder()
                .clubMemberStatus(status)
                .joinMessage(joinMessage)
                .club(club)
                .memberId(memberId)
                .build();
    }

    /**
     * ClubManagementExternalDTO.MyClubInfo -> ClubResponseDTO.ClubInfoDTO
     */
    public static ClubResponseDTO.ClubInfo toClubInfoDTOFromMyClubInfo(
            ClubManagementExternalDTO.MyClubInfo myClubInfo) {
        return ClubResponseDTO.ClubInfo.builder()
                .clubId(myClubInfo.getClubId())
                .clubName(myClubInfo.getClubName())
                .open(null)
                .build();
    }


    /**
     * ClubMemberDTO 리스트 → ClubResponseDTO.ClubMemberListDTO 변환
     */
    public static ClubResponseDTO.ClubMemberList toClubMemberListDTO(List<ClubResponseDTO.ClubMember> dtoList,
                                                                     boolean hasNext, Long lastId) {
        return ClubResponseDTO.ClubMemberList.builder()
                .clubMembers(dtoList)
                .hasNext(hasNext)
                .nextCursor(lastId)
                .pageSize(dtoList.size())
                .isStaff(true) // 항상 true
                .build();
    }

    /**
     * ClubMember 엔티티 + MemberExternalDTO.BasicInfoDTO -> ClubResponseDTO.ClubMemberDTO 변환
     */
    public static ClubResponseDTO.ClubMember toClubMemberDTO(ClubMember targetMember,
                                                             MemberExternalDTO.BasicInfo memberInfo) {
        return ClubResponseDTO.ClubMember.builder()
                .clubMemberId(targetMember.getId())
                .basicInfo(memberInfo)
                .joinMessage(targetMember.getJoinMessage())
                .clubMemberStatus(targetMember.getClubMemberStatus().name())
                .build();
    }


    /**
     * ClubRequestDTO.ClubDetailDTO -> Club 엔티티 변환
     */
    public static Club fromClubDetailDTOToClub(ClubRequestDTO.ClubDetail dto) {
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

    /**
     * Club 엔티티 -> ClubRequestDTO.ClubDetailDTO 변환
     */
    public static ClubResponseDTO.ClubDetail fromClubToClubDetailDTO(Club club, boolean isStaff) {
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

    /**
     * Club 엔티티 → ClubDetailResponseDTO 변환 (효율적 버전)
     */
    public static ClubDetail fromClubToResponseDTOWithCategoryNames(
            Club club,
            boolean isStaff
    ) {
        return ClubDetail.builder()
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

    /**
     * CreateBookRecommendDTO -> BookRecommend 엔티티
     */
    public static BookRecommend fromCreateBookRecommendDTOToEntity(
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

    /**
     * BookRecommend 엔티티 → BookRecommendDetailDTO
     */
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


    /**
     * BookRecommendDTO 리스트 → BookRecommendListDTO 변환
     */
    public static ClubResponseDTO.BookRecommendList toBookRecommendListDTO(
            List<ClubResponseDTO.BookRecommendDetail> dtoList,
            boolean hasNext,
            Long lastCursorId
    ) {
        return ClubResponseDTO.BookRecommendList.builder()
                .bookRecommendList(dtoList)
                .hasNext(hasNext)
                .nextCursor(lastCursorId)
                .pageSize(dtoList.size())
                .build();
    }

    // =====================================================
    // DTO -> DTO 변환
    // =====================================================

    /**
     * List<ClubNoticeExternalDTO.MyClubInfo> -> ClubNoticeExternalDTO.MyClubList 변환
     */
    public static ClubManagementExternalDTO.MyClubList fromClubInfoListToMyClubList(
            List<ClubManagementExternalDTO.MyClubInfo> clubInfoList
    ) {
        return ClubManagementExternalDTO.MyClubList.builder()
                .clubList(clubInfoList)
                .build();
    }

}
