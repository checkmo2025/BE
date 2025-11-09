package checkmo.bookStory.internal.service.query;

import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import java.util.List;
import java.util.Map;

/**
 * 책 이야기 조회 서비스
 */
public interface BookStoryQueryService {

    /**
     * 조건에 맞는 책 이야기 엔티티 목록을 조회 (페이지네이션을 위해 +1개 더 조회)
     *
     * @param memberId       조회하는 회원의 ID
     * @param scope          조회 범위 ("ALL", "MY", "FOLLOWING", "CLUB", "TARGET")
     * @param clubId         클럽 ID (scope가 "CLUB"일 때 필수)
     * @param targetMemberId 대상 회원 ID (scope가 "TARGET"일 때 필수)
     * @param cursorId       페이지네이션을 위한 커서 ID (처음에는 null)
     * @param pageSize       페이지 크기
     * @return 조회된 책 이야기 엔티티 목록
     */
    List<BookStory> findBookStories(String memberId, BookStoryRequestDTO.BookStoryScope scope, Long clubId,
                                    String targetMemberId, Long cursorId, int pageSize);

    /**
     * 조회된 책 이야기 목록에 대한 '좋아요' 여부를 확인
     *
     * @param memberId    조회하는 회원의 ID
     * @param bookStories 조회된 책 이야기 목록
     * @return 책 이야기 ID와 좋아요 여부를 매핑한 Map
     */
    Map<Long, Boolean> checkLikesForBookStories(String memberId, List<BookStory> bookStories);


    /**
     * 책 이야기 엔티티 조회
     *
     * @param bookStoryId 조회할 책 이야기의 ID
     * @return 조회된 책 이야기 엔티티
     */
    BookStory findBookStoryById(Long bookStoryId);

    /**
     * 책 이야기의 부모 댓글 조회 (대댓글 포함안됨!!!!)
     *
     * @param bookStoryId 조회할 책 이야기 ID
     * @return 조회된 댓글 목록
     */
    List<Comment> findCommentsByBookStoryId(Long bookStoryId);
}
