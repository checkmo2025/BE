package checkmo.authentication;

import java.util.List;
import lombok.Builder;

public class AuthenticationEvent {

    @Builder
    public record CreateMember(String id, String email, List<TermsAgreement> agreements) {
    }

    public record TermsAgreement(Long termsId, boolean agreed) {
    }

    @Builder
    public record SendVerificationEmail(String email, String verificationCode, VerificationType type) {
    }

    @Builder
    public record SendTempPassword(String email, String tempPassword){
    }

    @Builder
    public record ReactivateMember(String id) {
    }

    public enum VerificationType {
        SIGN_UP, UPDATE_EMAIL
    }
}
