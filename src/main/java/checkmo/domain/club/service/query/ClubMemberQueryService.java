package checkmo.domain.club.service.query;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.global.dto.ClubSharedDTO;

import java.util.List;
import java.util.Map;

/**
 * 독서클럽 회원에 대한 조회 서비스
 * <p>
 * 독서클럽 회원의 권한 확인 및 회원 존재 확인 기능을 담당합니다.
 */
public interface ClubMemberQueryService {

    /**
     * 독서클럽 회원인지 확인합니다.
     *
     * @param clubId 독서동아리 id
     * @param memberId 회원 id
     * @return ClubMember 객체
     * @throws GeneralException 클럽 회원이 존재하지 않을 경우
     */
    ClubMember validateClubMember(Long clubId, String memberId) throws GeneralException;

    /**
     * 특정 회원이 가입한 독서 클럽 목록을 조회합니다. (외부용)
     *
     * 다른 서비스 제공용
     *
     * @param memberId 회원 ID
     * @return 회원이 가입한 독서 클럽의 간략한 정보 목록 DTO
     */
    ClubSharedDTO.MyClubList getMyClubList(String memberId);

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (내부용)
     *
     * 피그마 참고 페이지 : #마이페이지
     *
     * @param memberId 회원 ID -> 로그인한 회원의 ID를 사용
     * @return 내가 가입한 독서 클럽 목록 DTO
     */
    ClubResponseDTO.MyPageClubListDTO getMyPageClubList(String memberId);

    /**
     * 특정 회원이 해당 클럽에서 어떤 상태(등급)인지 조회합니다.
     *
     * @param memberId 회원 ID
     * @param clubId 클럽 ID
     * @return ClubMemberStatus (MEMBER, STAFF 등) 또는 null (회원 아님)
     */
    ClubMember.ClubMemberStatus getMemberStatusInClub(String memberId, Long clubId);

    /**
     * 특정 회원이 여러 클럽에서의 상태를 한꺼번에 조회합니다.
     *
     * @param memberId 회원 ID
     * @param clubIds 클럽 ID 리스트
     * @return 클럽 ID별 회원 상태 맵
     */
    Map<Long, ClubMember.ClubMemberStatus> getMemberStatuses(String memberId, List<Long> clubIds);


    /**
     * 특정 상태의 모임 회원 목록을 조회합니다.
     *
     * @param clubId 모임 ID
     * @param status 조회할 상태 ("MEMBER", "STAFF", "PENDING", "BLOCKED", "ALL", *"ACTIVE"* 중 하나)
     * @param cursorId 페이징 커서 ID (null이면 처음부터 조회)
     * @param size 조회할 개수 (null이면 전체 조회)
     * @return ClubMember 엔티티 리스트
     */
    List<ClubMember> getClubMemberListByStatus(Long clubId, String status, Long cursorId, Integer size);

    /**
     * 멤버의 닉네임으로 clubMember를 매핑합니다.
     *
     * @param clubId 독서 모임 ID
     * @param nicknames 멤버 닉네임 리스트
     * @return 닉네임과 ClubMember 객체를 매핑한 Map
     * @throws GeneralException 닉네임에 해당하는 ClubMember가 존재하지 않을 경우, CLUB_MEMBER_NOT_FOUND 예외 발생
     */
    Map<String, ClubMember> getNicknameToClubMember(Long clubId, List<String> nicknames) throws GeneralException;

}
