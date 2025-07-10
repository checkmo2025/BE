package checkmo.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FollowEvent {
    private final String followerId; // 팔로우를 한 회원의 ID
    private final String followedId;  // 팔로우를 당한 회원의 ID
}
