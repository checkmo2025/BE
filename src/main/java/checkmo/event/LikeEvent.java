package checkmo.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LikeEvent {
    private final String senderId; // 좋아요를 누른 회원의 ID
    private final String receiverId; // 좋아요를 받은 회원의 ID
    private final Long bookStoryId;    // 좋아요가 눌린 책의 ID
}
