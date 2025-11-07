package checkmo.domain.club.service.query;

import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;

import java.util.List;

/**
 * 독서 클럽 조회 서비스
 *
 * 독서 클럽 자체에 대한 조회 기능을 담당
 * ex) 독서 클럽 목록 조회, 검색 기능, 특정 독서 클럽 상세 정보 조회 등을 처리
 */
public interface ClubQueryService {

    /**
     * 독서 클럽 목록을 조회합니다. (순수 엔티티 반환)
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 검색하기
     *
     * @param filter 검색 필터 (keyword, name, region, participants)
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null 또는 0)
     * @param pageSize 페이지 크기
     * @return 독서 클럽 목록 (순수 엔티티)
     */
    List<Club> getClubList(ClubRequestDTO.ClubSearchFilter filter, Long cursorId, int pageSize);

    /**
     * 독서모임의 상세 정보를 조회합니다. (순수 엔티티 반환)
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 검색하기 - 특정 모임 클릭시
     *
     * @param clubId 독서모임 ID
     * @return 독서 클럽 엔티티
     */
    Club getClubInfo(Long clubId);

    /**
     * 독서모임의 이름 중복 여부를 확인 합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 생성하기 첫화면 첫스크롤
     *
     * @param clubName 독서모임 이름
     * @return 중복 여부 (true: 중복, false: 중복 아님)
     */
    boolean isDuplicateClubName(String clubName);

    /**
     * 독서 클럽이 존재하는지 검증합니다.
     *
     * @param clubId 독서 클럽 ID
     * @return Club 검증된 독서 클럽 객체
     * @throws GeneralException 클럽이 존재하지 않는 경우
     */
    Club validateClub(Long clubId) throws GeneralException;
}
