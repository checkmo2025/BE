package checkmo.domain.club.service.query;

import checkmo.domain.club.dto.bookshelf.BookShelfResponseDTO;

/**
 * 특정 독서 모임내부의 활동에 대한 조회 서비스
 *
 * 독서 모임 내에서의 활동(예: 독서 기록,추천 도서, 한줄평 등)에 대한 조회 기능을 담당
 */
public interface ClubBookShelfQueryService {

    /**
     * 모임의 책장 리스트로 조회
     *
     * 피그마 참고 페이지 : #독서모임(사용자) - 책장 홈화면
     *
     * @param clubId 독서 클럽 ID
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null 또는 0)
     * @return 모임의 책장 정보가 담긴 BookShelfListDTO
     */
    BookShelfResponseDTO.BookShelfListDTO getBookShelfList(Long clubId, Long cursorId);

    /**
     * 책장 상세 조회 (특정 Meeting의 상세 정보)
     *
     * 피그마 참고 페이지 : #독서모임(사용자) - 책장 [특정 책] 클릭시
     *
     * @param meetingId Meeting ID
     * @return 책장 상세 정보가 담긴 BookShelfDetailDTO
     */
    BookShelfResponseDTO.BookShelfDetailDTO getBookShelfDetail(Long meetingId);


}
