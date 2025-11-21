package checkmo.bookStory;

import checkmo.bookStory.web.dto.BookStoryRequestDTO;

/**
 * BookStory Domain Query Facade BookStory 도메인의 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface BookStoryAPI {

    /**
     * 특정 책 이야기의 상세 정보를 조회합니다.
     *
     * @param memberId    조회하는 회원의 ID
     * @param bookStoryId 조회할 책 이야기 ID
     * @return 조회된 책 이야기 상세 정보 DTO
     */
    BookStoryExternalDTO.BookStoryDetailWithComment getBookStory(String memberId, Long bookStoryId);

    /**
     * scope에 따라 책 이야기 목록을 조회합니다. 비즈니스 로직을 Facade에서 처리하여 컨트롤러는 단순히 호출만 담당
     *
     * @param memberId             조회하는 회원의 ID
     * @param scope                조회 범위 ("ALL", "MY", "FOLLOWING", "CLUB", "TARGET")
     * @param clubId               클럽 ID (scope가 "CLUB"일 때 필수)
     * @param targetMemberNickname 대상 회원 닉네임 (scope가 "TARGET"일 때 필수)
     * @param cursorId             페이지 번호 (1부터 시작)
     * @return scope에 따른 책 이야기 목록 DTO
     */
    BookStoryExternalDTO.BookStoryList getBookStoriesByScope(
            String memberId,
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId,
            String targetMemberNickname,
            Long cursorId
    );
}