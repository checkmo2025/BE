package checkmo.domain.bookStory.service.query;

import checkmo.domain.bookStory.dto.BookStoryResponseDTO;

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
    BookStoryResponseDTO.BookStoryResponse getBookStory(String bookStoryId);

    /**
     * 책 이야기 목록 조회
     *
     * @param bookId 조회할 책의 ID
     * @return 조회된 책 이야기 목록의 DTO 리스트
     */
    BookStoryResponseDTO.BookStoryListResponse getBookStoriesByBookId(String bookId);
}
