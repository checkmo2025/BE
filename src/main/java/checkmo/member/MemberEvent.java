package checkmo.member;

import lombok.Builder;

public class MemberEvent {

    @Builder
    public record Follow(String followerId, String followingId) {}
}
