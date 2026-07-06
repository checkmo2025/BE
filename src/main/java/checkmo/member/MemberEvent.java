package checkmo.member;

import lombok.Builder;

public class MemberEvent {

    @Builder
    public record Follow(Long eventId, Long followerId, Long followingId) {
    }

    @Builder
    public record DeleteProfileImage(String imageUrl) {
    }

    @Builder
    public record MemberRegistrationCompleted(Long memberId) {
    }
}
