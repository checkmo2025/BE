package checkmo.clubManagement.internal.service;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO.BasicInfo;
import checkmo.clubManagement.ClubManagementExternalDTO.MyClubInfo;
import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.BookRecommend;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.service.query.ClubBookRecommendQueryService;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO.BookRecommendDetail;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
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

        // 1. 커서 초기화
        Long cursorId = (pageRequest.cursorId() == null || pageRequest.cursorId() == 0L) ? Long.MAX_VALUE
                : pageRequest.cursorId();

        // 2. 페이지 크기 결정 (size가 null 또는 0 이하이면 기본값 사용)
        int pageSize = (pageRequest.size() == null || pageRequest.size() <= 0) ? DEFAULT_PAGE_SIZE : pageRequest.size();

        // 3. Service에서 순수 엔티티 조회
        List<Club> clubs = clubQueryService.getClubList(filter, cursorId, pageSize);

        // 4. 클럽 ID 리스트 추출
        List<Long> clubIds = clubs.stream()
                .map(Club::getId)
                .toList();

        // 5. 클럽별 멤버 상태 배치 조회
        Map<Long, ClubMember.ClubMemberStatus> statusMap = clubMemberQueryService.getMemberStatuses(memberId, clubIds);

        // 6. DTO 변환
        List<ClubResponseDTO.ClubWithMyStatus> clubList = clubs.stream()
                .map(club -> toClubWithMyStatusDTO(club, statusMap))
                .toList();

        // 7. 페이징 처리 (마지막 ID를 기반으로 다음 페이지 존재 여부 확인)
        Long lastId = clubs.isEmpty() ? null : clubs.getLast().getId();
        boolean hasNext = !clubs.isEmpty() && clubs.size() == pageSize;

        // 8. 최종 DTO 변환
        return ClubResponseDTO.ClubList.builder()
                .clubList(clubList)
                .hasNext(hasNext)
                .nextCursor(lastId)
                .pageSize(clubList.size())
                .build();
    }

    private ClubResponseDTO.ClubWithMyStatus toClubWithMyStatusDTO(
            Club club,
            Map<Long, ClubMember.ClubMemberStatus> statusMap
    ) {
        ClubMember.ClubMemberStatus status = statusMap.get(club.getId());
        boolean isStaff = status == checkmo.clubManagement.internal.entity.ClubMember.ClubMemberStatus.STAFF;
        boolean isMember = status != null;

        ClubResponseDTO.ClubDetail clubDetail = ClubManagementConverter.toClubDetailDTO(club, isStaff);

        return ClubResponseDTO.ClubWithMyStatus.builder()
                .club(clubDetail)
                .isMember(isMember)
                .build();
    }

    public ClubResponseDTO.MyClubList getMyClubList(String memberId) {
        // 1. 회원이 가입한 모임 목록 조회
        List<MyClubInfo> myClubs = clubMemberQueryService.getMyClubList(memberId)
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

    public ClubResponseDTO.MyPageClubList getMyPageClubList(String memberId, Long cursorId, Integer size) {
        // 1. 기본 사이즈 처리
        if (size == null) {
            size = DEFAULT_PAGE_SIZE;
        }

        // 2. 서비스 호출 (size+1로 조회 → hasNext 판단)
        List<ClubMember> clubMembers = clubMemberQueryService.getMyPageClubList(memberId, cursorId, size + 1);

        // 3. 페이징 처리
        boolean hasNext = clubMembers.size() > size;
        if (hasNext) {
            clubMembers = clubMembers.subList(0, size);
        }
        Long nextCursor = hasNext ? clubMembers.getLast().getId() : null;

        // 4. DTO 변환
        List<ClubResponseDTO.ClubDetail> clubList = clubMembers.stream()
                .map(cm -> ClubManagementConverter.toClubDetailDTO(
                        cm.getClub(),
                        cm.isStaff()
                ))
                .toList();

        // 5. DTO 감싸서 반환
        return ClubResponseDTO.MyPageClubList.builder()
                .clubList(clubList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    public ClubResponseDTO.ClubDetail getClubInfo(Long clubId, String memberId) {
        // 1. Service에서 순수 엔티티 조회
        Club club = clubQueryService.getClubInfo(clubId);

        // 2. 운영진 권한 확인
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        boolean isStaff = clubMember.isStaff();
        if (!isStaff) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. DTO 변환 후 반환
        return ClubManagementConverter.toClubDetailDTO(club, isStaff);
    }

    public ClubResponseDTO.ClubMemberList getClubMemberListByStatus(
            Long clubId,
            String memberId,
            String clubMemberStatus,
            Long cursorId,
            Integer size
    ) {
        clubQueryService.validateClub(clubId);
        ClubMember requester = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!requester.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        if (size == null) {
            size = DEFAULT_PAGE_SIZE;
        }
        List<ClubMember> members
                = clubMemberQueryService.getClubMemberListByStatus(clubId, clubMemberStatus, cursorId, size + 1);
        boolean hasNext = members.size() > size;
        if (hasNext) {
            members = members.subList(0, size);
        }
        Long nextCursor = hasNext ? members.getLast().getId() : null;

        List<String> memberIds = extractMemberIds(members);
        Map<String, MemberExternalDTO.BasicInfo> memberInfoMap = memberAPI.getMemberBasicInfoMapForShare(memberIds);

        // DTO 변환
        List<ClubResponseDTO.ClubMember> dtoList = members.stream()
                .map(cm -> {
                    MemberExternalDTO.BasicInfo memberInfo = memberInfoMap.get(cm.getMemberId());
                    return ClubManagementConverter.toClubMemberDTO(cm, memberInfo);
                })
                .toList();

        return ClubResponseDTO.ClubMemberList.builder()
                .clubMembers(dtoList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(dtoList.size())
                .isStaff(true) // 항상 true
                .build();
    }

    private List<String> extractMemberIds(List<ClubMember> members) {
        if (members == null) {
            return List.of();
        }
        return members.stream()
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
        String nickname = memberAPI.getMemberBasicInfoForShare(memberId).getNickname();

        // 추천 도서 조회 및 페이징 처리
        Long cursor = (cursorId == null || cursorId == 0L) ? Long.MAX_VALUE : cursorId;
        List<BookRecommend> bookRecommends
                = clubBookRecommendQueryService.getRecommendedBooks(clubId, cursor, memberId);
        Long nextCursor = bookRecommends.isEmpty() ? null : bookRecommends.getLast().getId();
        boolean hasNext = clubBookRecommendQueryService.hasNextPage(clubId, nextCursor);

        List<BookRecommendDetail> bookRecommendDetails = bookRecommends.stream()
                .map(bookRecommend -> {
                    var bookInfo = bookAPI.getBookBasicInfoForShare(bookRecommend.getBookId());
                    var authorInfo = memberAPI.getMemberBasicInfoForShare(bookRecommend.getClubMember().getMemberId());
                    return ClubManagementConverter.toBookRecommendDetailDTO(bookRecommend, bookInfo, authorInfo,
                            nickname, clubMember.isStaff());
                }).toList();

        return ClubResponseDTO.BookRecommendList.builder()
                .bookRecommendList(bookRecommendDetails)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
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
        BasicInfo bookInfo = bookAPI.getBookBasicInfoForShare(bookRecommend.getBookId());
        MemberExternalDTO.BasicInfo authorInfo
                = memberAPI.getMemberBasicInfoForShare(bookRecommend.getClubMember().getMemberId());
        MemberExternalDTO.BasicInfo currentMemberInfo = memberAPI.getMemberBasicInfoForShare(memberId);

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
