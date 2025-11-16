package checkmo.bookStory.internal.service.command;

import checkmo.bookStory.web.dto.BookStoryRequestDTO;

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
    Long createBookStory(String memberId, BookStoryRequestDTO.BookStoryCreate request);

    /**
     * 책이야기를 수정
     *
     * @param memberId    수정 요청 회원 ID
     * @param bookStoryId 수정할 책 이야기의 ID
     * @param request     수정할 책 이야기 정보 DTO
     */
    Long updateBookStory(String memberId, Long bookStoryId, BookStoryRequestDTO.BookStoryUpdate request);

    /**
     * 책이야기를 삭제
     *
     * @param bookStoryId 삭제할 책이야기의 ID
     */
    void deleteBookStory(String memberId, Long bookStoryId);
}
