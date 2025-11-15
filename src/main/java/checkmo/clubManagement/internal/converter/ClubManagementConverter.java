package checkmo.clubManagement.internal.converter;

import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.clubManagement.internal.entity.BookRecommend;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO.ClubDetailResponseDTO;
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

    public static Map<Long, ClubManagementExternalDTO.MembershipDTO> fromClubMembertoMembereshipDTO(
            List<ClubMember> clubMembers) {
        return clubMembers.stream()
                .collect(
                        java.util.stream.Collectors.toMap(
                                ClubMember::getId,
                                ClubManagementConverter::fromClubMembertoMembershipDTO
                        )
                );
    }

    /**
     * ClubMember 엔티티 -> MembershipResponseDTO.MembershipDTO 변환
     */
    public static ClubManagementExternalDTO.MembershipDTO fromClubMembertoMembershipDTO(ClubMember clubMember) {
        return ClubManagementExternalDTO.MembershipDTO.builder()
                .memberId(clubMember.getMemberId())
                .clubMemberId(clubMember.getId())
                .active(clubMember.isActive())
                .staff(clubMember.isStaff())
                .build();
    }

    /**
     * ClubResponseDTO.ClubDetailResponseDTO -> ClubResponseDTO.MyPageClubListDTO 변환
     */
    public static ClubResponseDTO.MyPageClubListDTO toMyPageClubListDTO(
            List<ClubDetailResponseDTO> clubList,
            boolean hasNext,
            Long nextCursor
    ) {
        return ClubResponseDTO.MyPageClubListDTO.builder()
                .clubList(clubList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * Club 리스트 → ClubResponseDTO.ClubListDTO 변환
     */
    public static ClubResponseDTO.ClubListDTO toClubListDTO(
            List<ClubResponseDTO.ClubWithMyStatusDTO> clubList,
            boolean hasNext,
            Long nextCursor) {

        List<ClubResponseDTO.ClubWithMyStatusDTO> safeList =
                (clubList == null) ? List.of() : List.copyOf(clubList);

        return ClubResponseDTO.ClubListDTO.builder()
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
        return ClubMember.builder()
                .clubMemberStatus(status)
                .joinMessage(joinMessage)
                .club(club)
                .memberId(memberId)
                .build();
    }

    /**
     * ClubManagementExternalDTO.MyClubInfo -> ClubResponseDTO.ClubInfoDTO
     */
    public static ClubResponseDTO.ClubInfoDTO toClubInfoDTOFromMyClubInfo(
            ClubManagementExternalDTO.MyClubInfo myClubInfo) {
        return ClubResponseDTO.ClubInfoDTO.builder()
                .clubId(myClubInfo.getClubId())
                .clubName(myClubInfo.getClubName())
                .open(null)
                .build();
    }


    /**
     * ClubMemberDTO 리스트 → ClubResponseDTO.ClubMemberListDTO 변환
     */
    public static ClubResponseDTO.ClubMemberListDTO toClubMemberListDTO(List<ClubResponseDTO.ClubMemberDTO> dtoList,
                                                                        boolean hasNext, Long lastId) {
        return ClubResponseDTO.ClubMemberListDTO.builder()
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
    public static ClubResponseDTO.ClubMemberDTO toClubMemberDTO(ClubMember targetMember,
                                                                MemberExternalDTO.BasicInfo memberInfo) {
        return ClubResponseDTO.ClubMemberDTO.builder()
                .clubMemberId(targetMember.getId())
                .basicInfo(memberInfo)
                .joinMessage(targetMember.getJoinMessage())
                .clubMemberStatus(targetMember.getClubMemberStatus().name())
                .build();
    }


    /**
     * ClubRequestDTO.ClubDetailDTO -> Club 엔티티 변환
     */
    public static Club fromClubDetailDTOToClub(ClubRequestDTO.ClubDetailDTO dto) {
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
    public static ClubResponseDTO.ClubDetailDTO fromClubToClubDetailDTO(Club club, boolean isStaff) {
        return ClubResponseDTO.ClubDetailDTO.builder()
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
    public static ClubResponseDTO.ClubDetailResponseDTO fromClubToResponseDTOWithCategoryNames(
            Club club,
            boolean isStaff
    ) {
        return ClubResponseDTO.ClubDetailResponseDTO.builder()
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
            ClubRequestDTO.CreateBookRecommendDTO request,
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
    public static ClubResponseDTO.BookRecommendDetailDTO toBookRecommendDetailDTO(
            BookRecommend bookRecommend,
            BookExternalDTO.BasicInfo bookInfo,
            MemberExternalDTO.BasicInfo authorInfo,
            String currentMemberNickname,
            boolean isStaff
    ) {
        return ClubResponseDTO.BookRecommendDetailDTO.builder()
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
    public static ClubResponseDTO.BookRecommendListDTO toBookRecommendListDTO(
            List<ClubResponseDTO.BookRecommendDetailDTO> dtoList,
            boolean hasNext,
            Long lastCursorId
    ) {
        return ClubResponseDTO.BookRecommendListDTO.builder()
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
