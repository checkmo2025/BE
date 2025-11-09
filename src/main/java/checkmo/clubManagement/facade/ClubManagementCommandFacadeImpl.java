package checkmo.clubManagement.facade;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.converter.ClubManagementConverter;
import checkmo.clubManagement.entity.Club;
import checkmo.clubManagement.entity.ClubMember;
import checkmo.clubManagement.internal.service.command.ClubBookRecommendCommandService;
import checkmo.clubManagement.internal.service.command.ClubManagementCommandService;
import checkmo.clubManagement.internal.service.command.ClubMemberCommandService;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.member.MemberAPI;
import checkmo.member.MemberSharedDTO;
import checkmo.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubManagementCommandFacadeImpl implements ClubManagementCommandFacade {

    // Domain Level 1
    private final ClubManagementAPI clubManagementAPI;

    // Domain Level 2
    private final MemberAPI memberAPI;

    // 자신의 CommandService
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;
    private final ClubBookRecommendCommandService clubBookRecommendCommandService;
    private final ClubManagementCommandService clubManagementCommandService;
    private final ClubMemberCommandService clubMemberCommandService;

    @Override
    public Long createClub(String memberId, ClubRequestDTO.ClubDetailDTO request) {
        // 1. 운영진 멤버 엔티티 생성
        Member proxyMember = memberAPI.findMemberReferenceById(memberId);
        ClubMember clubMember = ClubManagementConverter.toClubMemberEntity(null, proxyMember,
                ClubMember.ClubMemberStatus.STAFF,
                null);

        // 2. 독서 모임 생성 및 ID 반환 (내부적으로 clubMember, clubCategory 처리)
        return clubManagementCommandService.createClub(clubMember, request);
    }

    @Override
    public void updateClub(Long clubId, String memberId, ClubRequestDTO.ClubDetailDTO request) {
        // 1. 유효성 검증(club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        clubManagementCommandService.updateClub(club, clubMember, request);
    }

    @Override
    public Long joinClub(Long clubId, String memberId, ClubRequestDTO.ClubMemberJoinDTO request) {
        // 1. 유효성 검증(club)
        Club club = clubQueryService.validateClub(clubId);

        // 2. 회원 프록시 객체 조회
        Member proxyMember = memberAPI.findMemberReferenceById(memberId);

        // 3. 독서 모임 가입 신청 및 가입된 클럽 멤버 ID 반환
        return clubMemberCommandService.joinClub(club, proxyMember, request).getId();
    }

    @Override
    public ClubResponseDTO.ClubMemberUpdateResponseDTO updateClubMemberStatus(Long clubId, String actorId,
                                                                              Long targetClubMemberId,
                                                                              String status) {
        // 1. 유효성 검증(club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember actor = clubMemberQueryService.validateClubMember(clubId, actorId);

        // 2. 독서 모임 회원 등급 수정
        ClubMember updatedClubMember = clubMemberCommandService.updateClubMemberStatus(club, actor,
                targetClubMemberId, status);

        // === 3. DTO 변환 및 반환 준비 === //

        // 외부 도메인 정보 조회 및 DTO 변환
        MemberSharedDTO.BasicInfo memberInfo = memberAPI.getMemberBasicInfoForShare(
                updatedClubMember.getMemberId());
        ClubResponseDTO.ClubMemberDTO updatedClubMemberDTO = ClubManagementConverter.toClubMemberDTO(updatedClubMember,
                memberInfo);

        // 운영진 여부를 포함해서 반환
        return ClubResponseDTO.ClubMemberUpdateResponseDTO.builder()
                .updatedMember(updatedClubMemberDTO)
                .isRequesterStaff(true) // 이 api 는 운영진만 호출할 수 있으므로 true 로 설정
                .build();
    }

    @Override
    public void leaveClub(Long clubId, String memberId) {
        // 1. 유효성 검증(club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 독서 모임 탈퇴
        clubMemberCommandService.leaveClub(club, clubMember);
    }

    @Override
    public ClubResponseDTO.BookRecommendDetailDTO recommendBook(Long clubId, String memberId,
                                                                ClubRequestDTO.CreateBookRecommendDTO request) {
        Long bookRecommendId = clubBookRecommendCommandService.recommendBook(clubId, memberId, request);
        return clubManagementAPI.getRecommendedBookDetail(clubId, bookRecommendId, memberId);
    }

    @Override
    public ClubResponseDTO.BookRecommendDetailDTO updateBookRecommend(Long clubId, String memberId,
                                                                      Long bookRecommendId,
                                                                      ClubRequestDTO.UpdateBookRecommendDTO request) {
        Long updateBookRecommendId = clubBookRecommendCommandService.updateBookRecommend(clubId, memberId,
                bookRecommendId, request);
        return clubManagementAPI.getRecommendedBookDetail(clubId, updateBookRecommendId, memberId);
    }

    @Override
    public void deleteRecommendedBook(Long clubId, String memberId, Long bookRecommendId) {
        clubBookRecommendCommandService.deleteRecommendedBook(clubId, memberId, bookRecommendId);
    }

}
