package checkmo.bookStory;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BookStoryEvent {

    @Builder
    public record BookStoryLiked(String senderId, String receiverId, Long bookStoryId) {}
}
