package checkmo.domain.bookStory.service.query;

import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.BookStorySharedDTO;
import checkmo.global.dto.ClubSharedDTO;
import checkmo.global.dto.MemberSharedDTO;

import java.util.List;
import java.util.Map;

/**
 * 책 이야기 조회 서비스
 */
public interface BookStoryQueryService {

    /**
     * 조건에 맞는 책 이야기 엔티티 목록을 조회 (페이지네이션을 위해 +1개 더 조회)
     *
     * @param memberId 조회하는 회원의 ID
     * @param scope 조회 범위 ("ALL", "MY", "FOLLOWING", "CLUB", "TARGET")
     * @param clubId 클럽 ID (scope가 "CLUB"일 때 필수)
     * @param targetMemberId 대상 회원 ID (scope가 "TARGET"일 때 필수)
     * @param cursorId 페이지네이션을 위한 커서 ID (처음에는 null)
     * @param pageSize 페이지 크기
     *
     * @return 조회된 책 이야기 엔티티 목록
     */
    List<BookStory> findBookStories(String memberId, BookStoryRequestDTO.BookStoryScope scope, Long clubId, String targetMemberId, Long cursorId, int pageSize);

    /**
     * 조회된 책 이야기 목록에 대한 '좋아요' 여부를 확인
     *
     * @param memberId 조회하는 회원의 ID
     * @param bookStories 조회된 책 이야기 목록
     *
     * @return 책 이야기 ID와 좋아요 여부를 매핑한 Map
     */
    Map<Long, Boolean> checkLikesForBookStories(String memberId, List<BookStory> bookStories);

    /**
     * 사용자가 가입한 클럽 목록을 조회
     *
     * @param memberId 조회하는 회원의 ID
     *
     * @return 사용자가 가입한 클럽 목록 DTO
     */
    ClubSharedDTO.MyClubList findMyClubs(String memberId);

    /**
     * 책 이야기 목록에 포함된 책들의 기본 정보를 조회
     *
     * @param bookStories 조회된 책 이야기 목록
     *
     * @return 책 ID와 책 기본 정보를 매핑한 Map
     */
    Map<String, BookSharedDTO.BasicInfoDTO> findBookInfos(List<BookStory> bookStories);

    /**
     * 책 이야기 목록에 포함된 작성자들의 정보를 '팔로우' 상태와 함께 조회
     *
     * @param currentMemberId 현재 회원의 ID (팔로우 상태 확인용)
     * @param bookStories 조회된 책 이야기 목록
     *
     * @return 작성자 ID와 작성자 정보(팔로우 상태 포함)를 매핑한 Map
     */
    Map<String, MemberSharedDTO.WithFollowStatusDTO> findAuthorInfos(String currentMemberId, List<BookStory> bookStories);

    /**
     * 책 이야기 조회
     *
     * @param memberId 조회하는 회원의 ID
     * @param bookStoryId 조회할 책 이야기의 ID
     *
     * @return 조회된 책 이야기의 DTO
     */
    BookStorySharedDTO.BookStoryResponse getBookStory(String memberId, Long bookStoryId);
}
