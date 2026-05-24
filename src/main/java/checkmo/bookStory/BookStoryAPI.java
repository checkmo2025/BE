package checkmo.bookStory;

public interface BookStoryAPI {
    void validateBookStory(Long bookStoryId);

    Long fetchBookStoryIdByBookStoryCommentId(Long bookStoryCommentId);
}
