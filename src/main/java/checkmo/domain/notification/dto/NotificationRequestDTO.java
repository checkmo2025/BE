package checkmo.domain.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class NotificationRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class LikeRequestDTO {
        private String senderId; // 좋아요를 누른 회원의 ID
        private String receiverId; // 좋아요를 받은 회원의 ID
        private Long bookStoryId; // 좋아요가 눌린 책의 ID
    }

    @Getter
    @NoArgsConstructor
    public static class FollowRequestDTO {
        private String followerId; // 팔로우를 한 회원의 ID -> Notification에서 얘가 senderId, redirectId로 사용됨
        private String followedId;  // 팔로우를 당한 회원의 ID
    }
}
