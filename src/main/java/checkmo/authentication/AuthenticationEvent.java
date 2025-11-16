package checkmo.authentication;

import lombok.Builder;

public class AuthenticationEvent {

    @Builder
    public record CreateMember(String id, String email) {}
}
