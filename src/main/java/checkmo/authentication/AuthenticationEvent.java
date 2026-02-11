package checkmo.authentication;

import java.util.List;
import lombok.Builder;

public class AuthenticationEvent {

    @Builder
    public record CreateMember(String id, String email, List<Long> agreedTermsIds) {
    }

    @Builder
    public record SendVerificationEmail(String email, String verificationCode, VerificationType type) {
    }

    @Builder
    public record SendTempPassword(String email, String tempPassword){
    }

    public enum VerificationType {
        SIGN_UP, UPDATE_EMAIL
    }
}
