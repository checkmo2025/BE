package checkmo.clubManagement.internal.service;

import static checkmo.clubManagement.ClubManagementExternalDTO.BasicInfo;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.BookRecommend;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.entity.ClubMember.ClubMemberStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.service.query.ClubBookRecommendQueryService;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO.BookRecommendDetail;
import checkmo.clubManagement.web.dto.ClubResponseDTO.ClubDetail;
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubManagementQueryFacade {

    // 페이징 기본 크기 상수
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final MemberAPI memberAPI;
    private final BookAPI bookAPI;

    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;
    private final ClubBookRecommendQueryService clubBookRecommendQueryService;

    public ClubResponseDTO.ClubList getClubList(
            String memberId,
            ClubRequestDTO.ClubSearchFilter filter,
            ClubRequestDTO.CursorInfo pageRequest
    ) {
        CursorResult<Club> clubCursorResult = CursorPagingHelper.getPage(
                pageSize -> clubQueryService.getClubList(filter, pageRequest.cursorId(), pageSize),
                Club::getId,
                DEFAULT_PAGE_SIZE
        );

        List<Club> clubs = clubCursorResult.content();
        List<Long> clubIds = extractClubIds(clubs);

        // 클럽별 멤버 상태 배치 조회
        Map<Long, ClubMember.ClubMemberStatus> statusMap = clubMemberQueryService.getMemberStatuses(memberId, clubIds);

        List<ClubResponseDTO.ClubWithMyStatus> clubList = clubs.stream()
                .map(club -> toClubWithMyStatusDTO(club, statusMap))
                .toList();

        // 8. 최종 DTO 변환
        return ClubResponseDTO.ClubList.builder()
                .clubList(clubList)
                .hasNext(clubCursorResult.hasNext())
                .nextCursor(clubCursorResult.nextCursor())
                .pageSize(clubList.size())
                .build();
    }

    private ClubResponseDTO.ClubWithMyStatus toClubWithMyStatusDTO(
            Club club,
            Map<Long, ClubMember.ClubMemberStatus> statusMap
    ) {
        ClubMember.ClubMemberStatus status = statusMap.get(club.getId());
        boolean isStaff = (status == ClubMemberStatus.STAFF);
        boolean isMember = (status != null);

        ClubResponseDTO.ClubDetail clubDetail = ClubManagementConverter.toClubDetailDTO(club, isStaff);

        return ClubResponseDTO.ClubWithMyStatus.builder()
                .club(clubDetail)
                .isMember(isMember)
                .build();
    }

    private List<Long> extractClubIds(List<Club> clubs) {
        return clubs.stream()
                .map(Club::getId)
                .toList();
    }

    public ClubResponseDTO.MyClubList getMyClubList(String memberId) {
        // 1. 회원이 가입한 모임 목록 조회
        List<BasicInfo> myClubs = clubMemberQueryService.getMyClubList(memberId)
                .getClubList();

        // 2. 모임 정보 DTO로 변환
        List<ClubResponseDTO.ClubInfo> clubInfoList = myClubs.stream()
                .map(ClubManagementConverter::toClubInfoDTO)
                .toList();

        // 3. 최종 DTO 반환
        return ClubResponseDTO.MyClubList.builder()
                .clubList(clubInfoList)
                .build();
    }

    public ClubResponseDTO.MyPageClubList getMyPageClubList(String memberId, Long cursorId) {
        CursorResult<ClubMember> clubMemberCursorResult = CursorPagingHelper.getPage(
                pageSize -> clubMemberQueryService.getMyPageClubList(memberId, cursorId, pageSize),
                ClubMember::getId,
                DEFAULT_PAGE_SIZE
        );
        List<ClubMember> clubMembers = clubMemberCursorResult.content();

        List<ClubResponseDTO.ClubDetail> clubList = convertToClubResponseDTO(clubMembers);
        return ClubResponseDTO.MyPageClubList.builder()
                .clubList(clubList)
                .hasNext(clubMemberCursorResult.hasNext())
                .nextCursor(clubMemberCursorResult.nextCursor())
                .build();
    }

    private List<ClubDetail> convertToClubResponseDTO(List<ClubMember> clubMembers) {
        return clubMembers.stream()
                .map(cm -> ClubManagementConverter.toClubDetailDTO(
                        cm.getClub(),
                        cm.isStaff()
                ))
                .toList();
    }

    public ClubResponseDTO.ClubDetail getClubInfo(Long clubId, String memberId) {
        // 1. Service에서 순수 엔티티 조회
        Club club = clubQueryService.getClubInfo(clubId);

        // 2. 운영진 권한 확인
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        boolean isStaff = clubMember.isStaff();
        if (!isStaff) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. DTO 변환 후 반환
        return ClubManagementConverter.toClubDetailDTO(club, isStaff);
    }

    public ClubResponseDTO.ClubMemberList getClubMemberListByStatus(
            Long clubId,
            String memberId,
            String clubMemberStatus,
            Long cursorId
    ) {
        clubQueryService.validateClub(clubId);
        ClubMember requester = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!requester.isStaff()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }

        CursorResult<ClubMember> clubMemberCursorResult = CursorPagingHelper.getPage(
                size -> clubMemberQueryService.getClubMemberListByStatus(clubId, clubMemberStatus, cursorId, size),
                ClubMember::getId,
                DEFAULT_PAGE_SIZE
        );
        List<ClubMember> clubMembers = clubMemberCursorResult.content();
        List<String> memberIds = extractMemberIds(clubMembers);

        Map<String, MemberExternalDTO.BasicInfo> memberInfoMap = memberAPI.fetchMemberBasicInfoByMemberIds(memberIds);

        List<ClubResponseDTO.ClubMember> dtoList = clubMembers.stream()
                .map(cm -> {
                    MemberExternalDTO.BasicInfo memberInfo = memberInfoMap.get(cm.getMemberId());
                    return ClubManagementConverter.toClubMemberDTO(cm, memberInfo);
                })
                .toList();

        return ClubResponseDTO.ClubMemberList.builder()
                .clubMembers(dtoList)
                .hasNext(clubMemberCursorResult.hasNext())
                .nextCursor(clubMemberCursorResult.nextCursor())
                .pageSize(dtoList.size())
                .isStaff(true) // 항상 true
                .build();
    }

    private List<String> extractMemberIds(List<ClubMember> clubMembers) {
        if (clubMembers == null) {
            return List.of();
        }
        return clubMembers.stream()
                .map(checkmo.clubManagement.internal.entity.ClubMember::getMemberId)
                .distinct()
                .toList();
    }

    public boolean isDuplicateClubName(String clubName) {
        return clubQueryService.isDuplicateClubName(clubName);
    }

    public ClubResponseDTO.BookRecommendList getRecommendedBooks(Long clubId, Long cursorId, String memberId) {
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        String nickname = memberAPI.fetchMemberBasicInfo(memberId).getNickname();

        CursorResult<BookRecommend> bookRecommendCursorResult = CursorPagingHelper.getPage(
                pageSize -> clubBookRecommendQueryService.getRecommendedBooks(clubId, cursorId, pageSize),
                BookRecommend::getId,
                DEFAULT_PAGE_SIZE
        );
        List<BookRecommend> bookRecommends = bookRecommendCursorResult.content();

        List<BookRecommendDetail> bookRecommendDetails = bookRecommends.stream()
                .map(bookRecommend -> {
                    var bookInfo = bookAPI.fetchBookBasicInfo(bookRecommend.getBookId());
                    var authorInfo = memberAPI.fetchMemberBasicInfo(bookRecommend.getClubMember().getMemberId());
                    return ClubManagementConverter.toBookRecommendDetailDTO(bookRecommend, bookInfo, authorInfo,
                            nickname, clubMember.isStaff());
                }).toList();

        return ClubResponseDTO.BookRecommendList.builder()
                .bookRecommendList(bookRecommendDetails)
                .hasNext(bookRecommendCursorResult.hasNext())
                .nextCursor(bookRecommendCursorResult.nextCursor())
                .pageSize(bookRecommendDetails.size())
                .build();
    }

    public ClubResponseDTO.BookRecommendDetail getRecommendedBookDetail(
            Long clubId,
            Long bookRecommendId,
            String memberId
    ) {
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        BookRecommend bookRecommend = clubBookRecommendQueryService.getBookRecommend(clubId, bookRecommendId, memberId);

        // 외부 도메인 정보 조회 (Facade에서 처리)
        BookExternalDTO.BasicInfo bookInfo = bookAPI.fetchBookBasicInfo(bookRecommend.getBookId());
        MemberExternalDTO.BasicInfo authorInfo
                = memberAPI.fetchMemberBasicInfo(bookRecommend.getClubMember().getMemberId());
        MemberExternalDTO.BasicInfo currentMemberInfo = memberAPI.fetchMemberBasicInfo(memberId);

        // 4. DTO 변환 후 반환
        return ClubManagementConverter.toBookRecommendDetailDTO(
                bookRecommend,
                bookInfo,
                authorInfo,
                currentMemberInfo.getNickname(),
                clubMember.isStaff()
        );
    }

    public Boolean checkStaffStatus(Long clubId, String memberId) {
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        return clubMember.isStaff();
    }

}
