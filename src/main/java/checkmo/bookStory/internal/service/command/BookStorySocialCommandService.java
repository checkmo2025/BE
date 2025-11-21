package checkmo.bookStory.internal.service.command;

/**
 * 좋아요 기능 처리
 */
public interface BookStorySocialCommandService {

    /**
     * 책이야기에 좋아요를 토글(추가/제거) 이미 좋아요가 있으면 제거, 없으면 추가
     *
     * @param memberId    토글하는 사용자의 ID
     * @param bookStoryId 토글할 책이야기의 ID
     * @return 좋아요가 추가됐는지/제거됐는지 여부
     */
    boolean toggleLikeOnBookStory(String memberId, Long bookStoryId);

}
