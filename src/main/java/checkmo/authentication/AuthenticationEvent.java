package checkmo.authentication;

import lombok.Builder;

public class AuthenticationEvent {

    @Builder
    public record CreateMember(String id, String email) {
    }

    @Builder
    public record SendVerificationEmail(String email, String verificationCode) {
    }

    @Builder
    public record SendTempPassword(String email, String tempPassword){
    }
}
