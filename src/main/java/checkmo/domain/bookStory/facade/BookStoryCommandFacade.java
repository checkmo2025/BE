package checkmo.domain.bookStory.facade;

import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;

/**
 * BookStory Domain Command Facade
 * BookStory 도메인의 Command(생성, 수정, 삭제) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface BookStoryCommandFacade {

    /**
     * 새로운 책 이야기를 작성합니다. (내부용)
     *
     * @param memberId 작성자 회원 ID
     * @param request  작성할 책 이야기 정보 DTO
     * @return 생성된 책 이야기의 ID
     */
    Long createBookStory(String memberId, BookStoryRequestDTO.BookStoryCreateRequestDTO request);

    /**
     * 기존 책 이야기를 수정합니다. (내부용)
     *
     * @param memberId    수정 요청 회원 ID
     * @param request     수정할 책 이야기 정보 DTO
     * @return 수정된 책 이야기의 ID
     */
    Long updateBookStory(String memberId, BookStoryRequestDTO.BookStoryUpdateRequestDTO request);

    /**
     * 책 이야기를 삭제합니다. (내부용)
     *
     * @param memberId    삭제 요청 회원 ID
     * @param bookStoryId 삭제할 책 이야기의 ID
     * @return 삭제된 책 이야기의 ID
     */
    Long deleteBookStory(String memberId, Long bookStoryId);

    /**
     * 책 이야기의 '좋아요'를 토글(추가/삭제)합니다. (내부용)
     *
     * @param userId      요청 사용자 ID
     * @param bookStoryId 대상 책 이야기 ID
     * @return 처리된 책 이야기의 ID
     */
    Long toggleLikeOnBookStory(Long userId, Long bookStoryId);
}