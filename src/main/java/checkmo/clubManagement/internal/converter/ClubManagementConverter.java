package checkmo.clubManagement.internal.converter;

import checkmo.book.BookSharedDTO;
import checkmo.book.internal.entity.Book;
import checkmo.category.CategorySharedDTO;
import checkmo.category.CategorySharedDTO.CategoryInfo;
import checkmo.clubManagement.ClubManagementSharedDTO;
import checkmo.clubManagement.internal.entity.BookRecommend;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubCategory;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO.ClubDetailResponseDTO;
import checkmo.clubManagement.web.dto.MembershipResponseDTO;
import checkmo.member.MemberSharedDTO;
import checkmo.member.internal.entity.Member;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubManagementConverter {

    // =====================================================
    // Entity ↔ DTO 변환
    // =====================================================

    /**
     * ClubMember 엔티티 -> MembershipResponseDTO.MembershipDTO 변환
     */
    public static MembershipResponseDTO.MembershipDTO fromClubMembertoMembershipDTO(ClubMember clubMember) {
        return MembershipResponseDTO.MembershipDTO.builder()
                .clubMemberId(clubMember.getId())
                .clubMemberStatus(clubMember.getClubMemberStatus().name())
                .updatedAt(clubMember.getUpdatedAt())
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
            Member member,
            ClubMember.ClubMemberStatus status,
            String joinMessage
    ) {
        return ClubMember.builder()
                .clubMemberStatus(status)
                .joinMessage(joinMessage)
                .club(club)
                .member(member)
                .build();
    }

    /**
     * Club -> ClubResponseDTO.ClubInfoDTO
     */
    public static ClubResponseDTO.ClubInfoDTO toClubInfoDTO(Club club) {
        return ClubResponseDTO.ClubInfoDTO.builder()
                .clubId(club.getId())
                .clubName(club.getName())
                .open(club.isOpen())
                .build();
    }

    /**
     * ClubManagementSharedDTO.MyClubInfo -> ClubResponseDTO.ClubInfoDTO
     */
    public static ClubResponseDTO.ClubInfoDTO toClubInfoDTOFromMyClubInfo(
            ClubManagementSharedDTO.MyClubInfo myClubInfo) {
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
     * ClubMember 엔티티 + MemberSharedDTO.BasicInfoDTO -> ClubResponseDTO.ClubMemberDTO 변환
     */
    public static ClubResponseDTO.ClubMemberDTO toClubMemberDTO(ClubMember targetMember,
                                                                MemberSharedDTO.BasicInfo memberInfo) {
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
     * ClubRequestDTO.ClubDetailDTO -> CategoryIdListDTO
     */
    public static CategorySharedDTO.CategoryIdList toCategoryListRequestDTO(ClubRequestDTO.ClubDetailDTO dto) {
        return CategorySharedDTO.CategoryIdList.builder()
                .categoryIdList(dto.getCategory())
                .build();
    }

    /**
     * Club 엔티티 -> ClubRequestDTO.ClubDetailDTO 변환
     */
    public static ClubResponseDTO.ClubDetailDTO fromClubToClubDetailDTO(Club club, List<Long> categoryIds,
                                                                        boolean isStaff) {
        return ClubResponseDTO.ClubDetailDTO.builder()
                .clubId(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .profileImageUrl(club.getProfileImgUrl())
                .open(club.isOpen())
                .category(categoryIds)
                .region(club.getRegion())
                .participantTypes(club.getParticipantTypes())
                .insta(club.getInsta())
                .kakao(club.getKakao())
                .isStaff(isStaff)
                .build();
    }

    /**
     * Club, CategorySharedDTO -> ClubResponseDTO.ClubDetailResponseDTO 변환
     */
    public static ClubResponseDTO.ClubDetailResponseDTO fromClubToResponseDTO(
            Club club, List<CategorySharedDTO.CategoryInfo> categories, boolean isStaff) {

        List<String> categoryNames = categories.stream()
                .map(CategorySharedDTO.CategoryInfo::getName)
                .toList();

        return ClubResponseDTO.ClubDetailResponseDTO.builder()
                .clubId(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .profileImageUrl(club.getProfileImgUrl())
                .open(club.isOpen())
                .category(categoryNames)
                .region(club.getRegion())
                .participantTypes(club.getParticipantTypes())
                .insta(club.getInsta())
                .kakao(club.getKakao())
                .isStaff(isStaff)
                .build();
    }


    /**
     * Club 엔티티 + 카테고리 이름 리스트 → ClubDetailResponseDTO 변환 (효율적 버전)
     */
    public static ClubResponseDTO.ClubDetailResponseDTO fromClubToResponseDTOWithCategoryNames(
            Club club, List<String> categoryNames, boolean isStaff) {

        return ClubResponseDTO.ClubDetailResponseDTO.builder()
                .clubId(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .profileImageUrl(club.getProfileImgUrl())
                .open(club.isOpen())
                .category(categoryNames)
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
            Book proxyBook,
            ClubMember clubMember
    ) {
        return BookRecommend.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .rate(request.getRate())
                .tag(request.getTag())
                .clubMember(clubMember)
                .book(proxyBook)
                .bookId(proxyBook.getId())
                .build();
    }

    /**
     * BookRecommend 엔티티 → BookRecommendDetailDTO
     */
    public static ClubResponseDTO.BookRecommendDetailDTO toBookRecommendDetailDTO(
            BookRecommend bookRecommend,
            BookSharedDTO.BasicInfo bookInfo,
            MemberSharedDTO.BasicInfo authorInfo,
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
     * List<ClubNoticeSharedDTO.MyClubInfo> -> ClubNoticeSharedDTO.MyClubList 변환
     */
    public static ClubManagementSharedDTO.MyClubList fromClubInfoListToMyClubList(
            List<ClubManagementSharedDTO.MyClubInfo> clubInfoList
    ) {
        return ClubManagementSharedDTO.MyClubList.builder()
                .clubList(clubInfoList)
                .build();
    }

    // =====================================================
    // ClubCategory 관련 변환
    // =====================================================

    /**
     * List<ClubCategory> → CategoryInfoList 변환
     */
    public static Map<Long, List<CategoryInfo>> fromClubCategoriesToCategoryInfoListMap(
            List<ClubCategory> allClubCategories
    ) {
        return allClubCategories.stream()
                .collect(Collectors.groupingBy(
                        ClubCategory::getClubId,
                        Collectors.mapping(cc -> CategorySharedDTO.CategoryInfo.builder()
                                        .id(cc.getCategory().getId())
                                        .name(cc.getCategory().getName())
                                        .build(),
                                Collectors.toList())
                ));
    }

    /**
     * List<ClubCategory> → 클럽별 카테고리 이름 Map 변환
     */
    public static Map<Long, List<String>> fromClubCategoriesToCategoryNamesMap(
            List<ClubCategory> allClubCategories
    ) {
        return allClubCategories.stream()
                .collect(Collectors.groupingBy(
                        ClubCategory::getClubId,
                        Collectors.mapping(cc -> cc.getCategory().getName(), Collectors.toList())
                ));
    }

    /**
     * List<ClubCategory> → 클럽별 카테고리 ID Map 변환
     */
    public static Map<Long, List<Long>> fromClubCategoriesToCategoryIdMap(
            List<ClubCategory> allClubCategories
    ) {
        return allClubCategories.stream()
                .collect(Collectors.groupingBy(
                        ClubCategory::getClubId,
                        Collectors.mapping(ClubCategory::getCategoryId, Collectors.toList())
                ));
    }

}
