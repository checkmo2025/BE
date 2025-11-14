package checkmo.member;

import lombok.Builder;

public class MemberEvent {

    @Builder
    public record MemberProfileCompleted(String memberId) {}

    @Builder
    public record Follow(String followerId, String followingId) {}
}
