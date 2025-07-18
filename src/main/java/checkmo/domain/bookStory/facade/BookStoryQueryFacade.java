package checkmo.domain.bookStory.facade;

import checkmo.domain.bookStory.web.dto.BookStoryResponseDTO;
import checkmo.global.dto.BookStorySharedDTO;

/**
 * BookStory Domain Query Facade
 * BookStory 도메인의 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface BookStoryQueryFacade {

    /**
     * 특정 책 이야기의 상세 정보를 조회합니다. (내부용)
     *
     * @param bookStoryId 조회할 책 이야기 ID
     * @return 조회된 책 이야기 상세 정보 DTO
     */
    BookStoryResponseDTO.BookStoryResponse getBookStory(Long bookStoryId);

    /**
     * 특정 회원의 책 이야기 목록을 커서 기반으로 조회합니다. (내부용)
     *
     * @param memberId 조회하는 회원의 ID
     * @param cursorId 페이징을 위한 커서 ID (처음에는 null)
     * @return 조회된 책 이야기 목록 DTO
     */
    BookStoryResponseDTO.BookStoryListResponse getMyBookStories(String memberId, Long cursorId);

    /**
     * 특정 회원의 책 이야기 미리보기 목록을 조회합니다. (외부용)
     * 홈 화면, 마이페이지 등 다른 도메인/서비스에서 사용합니다.
     *
     * @param memberId 조회하는 회원의 ID
     * @param size     조회할 개수
     * @return 책 이야기 미리보기 정보가 담긴 공유 DTO
     */
    BookStorySharedDTO.BookStoryPreviewListDTO getBookStoryPreviews(String memberId, int size);
}