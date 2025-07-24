package checkmo.domain.bookStory.facade;

import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.global.dto.BookStorySharedDTO;

/**
 * BookStory Domain Query Facade
 * BookStory 도메인의 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface BookStoryQueryFacade {

    /**
     * 특정 책 이야기의 상세 정보를 조회합니다. (내부용)
     *
     * @param memberId 조회하는 회원의 ID
     * @param bookStoryId 조회할 책 이야기 ID
     * @return 조회된 책 이야기 상세 정보 DTO
     */
    BookStorySharedDTO.BookStoryResponse getBookStory(String memberId, Long bookStoryId);

    /**
     * 특정 회원의 책 이야기 목록을 커서 기반으로 조회합니다. (내부용)
     * 여기서 특정 회원은 나 일수도, 다른 회원일 수도 있습니다.
     *
     * @param memberId 조회하는 회원의 ID
     * @param targetMemberNickname 조회할 회원의 닉네임
     * @param cursorId 페이징을 위한 커서 ID (처음에는 null)
     * @return 조회된 책 이야기 목록 DTO
     */
    BookStorySharedDTO.BookStoryListResponse getMyBookStories(String memberId, String targetMemberNickname, Long cursorId);

    /**
     * scope에 따라 책 이야기 목록을 조회합니다. (내부용)
     * 비즈니스 로직을 Facade에서 처리하여 컨트롤러는 단순히 호출만 담당
     *
     * @param memberId 조회하는 회원의 ID
     * @param scope    조회 범위 ("all", "my", "club")
     * @param clubId   클럽 ID (scope가 "club"일 때 필수)
     * @param cursorId     페이지 번호 (1부터 시작)
     * @return scope에 따른 책 이야기 목록 DTO
     */
    BookStorySharedDTO.BookStoryListResponse getBookStoriesByScope(String memberId, BookStoryRequestDTO.BookStoryScope scope, Long clubId, Long cursorId);
}