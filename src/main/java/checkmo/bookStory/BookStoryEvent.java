package checkmo.bookStory;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BookStoryEvent {

    @Builder
    public record BookStoryLiked(Long eventId, Long senderId, Long receiverId, Long bookStoryId) {
    }

    @Builder
    public record BookStoryComment(Long eventId, Long senderId, Long receiverId, Long bookStoryId) {
    }

    @Builder
    public record DeleteBookStoryImage(List<String> imageUrls) {
    }
}
