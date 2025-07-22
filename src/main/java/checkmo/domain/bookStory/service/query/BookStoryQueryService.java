package checkmo.domain.bookStory.service.query;

import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.domain.bookStory.web.dto.BookStoryResponseDTO;

/**
 * 책 이야기 조회 서비스
 */
public interface BookStoryQueryService {
    /**
     * 책 이야기 조회
     *
     * @param bookStoryId 조회할 책 이야기의 ID
     * @return 조회된 책 이야기의 DTO
     */
    BookStoryResponseDTO.BookStoryResponse getBookStory(Long bookStoryId);

    /**
     * 특정 회원의 책 이야기 목록을 커서 기반으로 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @param cursorId 페이징을 위한 커서 ID (처음에는 null)
     * @return 조회된 책 이야기 목록 DTO
     */
    BookStoryResponseDTO.BookStoryListResponse getMyBookStories(String memberId, Long cursorId);

    /**
     * 특정 회원의 책 이야기 목록을 size 개수만큼 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @param size     조회할 개수
     * @return 조회된 책 이야기 목록 DTO
     */
    BookStoryResponseDTO.BookStoryListResponse getMyBookStories(String memberId, int size);

    /**
     * scope에 따라 책 이야기 목록을 조회합니다.
     *
     * @param memberId 조회하는 회원의 ID
     * @param scope    조회 범위 (ALL, MY, CLUB)
     * @param clubId   클럽 ID (scope가 CLUB일 때 필수)
     * @param cursorId 페이징을 위한 커서 ID (처음에는 null)
     * @return scope에 따른 책 이야기 목록 DTO
     */
    BookStoryResponseDTO.BookStoryListResponse getBookStoriesByScope(
            String memberId,
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId,
            Long cursorId
    );
}
