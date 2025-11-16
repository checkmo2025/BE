package checkmo.member;

import lombok.Builder;

public class MemberEvent {

    @Builder
    public record Follow(Long eventId, String followerId, String followingId) {
    }
}
