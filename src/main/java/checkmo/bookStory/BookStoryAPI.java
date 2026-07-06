package checkmo.bookStory;

public interface BookStoryAPI {
    void validateBookStory(Long bookStoryId);

    Long fetchBookStoryIdByBookStoryCommentId(Long bookStoryCommentId);

    Long fetchBookStoryAuthorId(Long bookStoryId);

    Long fetchBookStoryCommentAuthorId(Long commentId);
}
