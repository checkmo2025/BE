package checkmo.domain.club.service.query;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.entity.ClubMember;
import checkmo.global.dto.ClubSharedDTO;

/**
 * 독서클럽 회원에 대한 조회 서비스
 * <p>
 * 독서클럽 회원의 권한 확인 및 회원 존재 확인 기능을 담당합니다.
 */
public interface ClubMemberQueryService {

    /**
     * 독서클럽 회원인지 확인합니다.
     *
     * @param clubId   독서동아리 id
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
     * 특정 회원이 해당 클럽에서 어떤 상태(등급)인지 조회합니다.
     *
     * @param memberId 회원 ID
     * @param clubId   클럽 ID
     * @return ClubMemberStatus (MEMBER, STAFF 등) 또는 null (회원 아님)
     */
    ClubMember.ClubMemberStatus getMemberStatusInClub(String memberId, Long clubId);
}
