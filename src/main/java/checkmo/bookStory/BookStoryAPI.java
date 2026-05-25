package checkmo.bookStory;

public interface BookStoryAPI {
    void validateBookStory(Long bookStoryId);

    Long fetchBookStoryIdByBookStoryCommentId(Long bookStoryCommentId);

    String fetchBookStoryAuthorId(Long bookStoryId);

    String fetchBookStoryCommentAuthorId(Long commentId);
}
