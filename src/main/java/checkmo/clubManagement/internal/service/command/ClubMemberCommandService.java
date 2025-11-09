package checkmo.clubManagement.internal.service.command;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.member.internal.entity.Member;

/**
 * 독서모임의 가입 신청, 승인, 탈퇴, 권한 변경 등등 독서모임 내부의 멤버십 관련 작업을 처리하는 Service Service는 순수 엔티티만 반환, DTO 변환은 Facade에서 처리
 */
public interface ClubMemberCommandService {

    /**
     * 독서모임에 가입 신청을 합니다.
     *
     * @param club        독서모임
     * @param proxyMember 회원 -> 로그인한 회원
     * @param request     가입 신청 메시지 DTO
     * @return 가입 신청 후의 ClubMember
     */
    ClubMember joinClub(Club club, Member proxyMember, ClubRequestDTO.ClubMemberJoinDTO request);

    /**
     * 독서 모임 회원의 등급(상태/역할)을 수정합니다.
     *
     * @param club               독서 모임
     * @param actor              요청자(운영진) 회원
     * @param targetClubMemberId 수정 대상 회원 ID
     * @param status             수정할 등급 (MEMBER, STAFF, PENDING, BLOCKED 중 선택)
     * @return 수정된 ClubMember 엔티티
     */
    ClubMember updateClubMemberStatus(Club club, ClubMember actor, Long targetClubMemberId, String status);

    /**
     * 독서 모임에서 탈퇴합니다.
     *
     * @param club       독서 모임
     * @param clubMember 탈퇴할 회원
     */
    void leaveClub(Club club, ClubMember clubMember);

}
