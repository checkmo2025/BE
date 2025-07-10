package checkmo.domain.bookStory.service.command;

/**
 * 좋아요, 댓글 작성 등의 기능 처리
 */
public interface BookStorySocialCommandService {

    /**
     * 책이야기에 좋아요를 토글(추가/제거)
     * 이미 좋아요가 있으면 제거, 없으면 추가
     *
     * @param userId 토글하는 사용자의 ID
     * @param bookStoryId 토글할 책이야기의 ID
     * @return 좋아요가 토글된 책이야기의 ID
     */
    Long toggleLikeOnBookStory(Long userId, Long bookStoryId);

/*
    아래 두개 메소드는 private으로 구현해서 사용하기
    */
/**
     * 책이야기에 좋아요를 추가
     *
     * @param userId 좋아요를 추가하는 사용자의 ID
     * @param bookStoryId 좋아요를 추가할 책이야기의 ID
     * @return 좋아요가 추가된 책이야기의 ID
     *//*

    Long addLikeToBookStory(Long userId, Long bookStoryId);

    */
/**
     * 책이야기에서 좋아요를 제거
     *
     * @param userId 좋아요를 제거하는 사용자의 ID
     * @param bookStoryId 좋아요를 제거할 책이야기의 ID
     * @return 좋아요가 제거된 책이야기의 ID
     *//*

    Long removeLikeFromBookStory(Long userId, Long bookStoryId);

*/
}
