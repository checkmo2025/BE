package checkmo.domain.club.service.query;

import checkmo.domain.club.dto.bookshelf.BookShelfResponseDTO;

/**
 * 특정 독서 모임내부의 활동에 대한 조회 서비스
 *
 * 독서 모임 내에서의 활동(예: 독서 기록,추천 도서, 한줄평 등)에 대한 조회 기능을 담당
 */
public interface ClubBookShelfQueryService {
    /**
     * 모임의 모든 활동 조회
     *
     * @param clubId 모임 ID
     * @param generation 필터링할 기수 번호 (null이면 가장 최근 기수 조회)
     * @return 모임의 활동 정보가 담긴 List
     *
     * 서비스 내부에서 조회할 때 ActivityListDTO에 담길 Meeting은 MeetingStatus가 COMPLETE인 것만 조회
     */
     BookShelfResponseDTO.ActivityListDTO getMeetingListByGeneration(Long clubId, Integer generation);
}
