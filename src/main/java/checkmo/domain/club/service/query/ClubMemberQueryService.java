package checkmo.domain.club.service.query;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.entity.ClubMember;

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

}
