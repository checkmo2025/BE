package checkmo.domain.bookStory.service.command;

import checkmo.domain.bookStory.dto.BookStoryRequestDTO;

/**
 * 책이야기 작성, 수정, 삭제
 */
public interface BookStoryCommandService {
    /**
     * 책이야기를 작성
     *
     * @param memberId 책이야기를 작성하는 회원의 ID
     * @param request 책이야기 요청 DTO
     */
    Long createBookStory(String memberId, BookStoryRequestDTO.BookStoryCreateRequestDTO request);

    /**
     * 책이야기를 수정
     *
     * @param request 수정할 책이야기 요청 DTO
     */
    Long updateBookStory(String memberId, BookStoryRequestDTO.BookStoryUpdateRequestDTO request);

    /**
     * 책이야기를 삭제
     *
     * @param bookStoryId 삭제할 책이야기의 ID
     */
    Long deleteBookStory(String memberId, Long bookStoryId);
}
