package checkmo.member;

import lombok.Builder;

public class MemberEvent {

    @Builder
    public record Follow(Long eventId, String followerId, String followingId) {
    }

    @Builder
    public record DeleteProfileImage(String imageUrl) {
    }

    @Builder
    public record MemberRegistrationCompleted(String memberId) {
    }
}
