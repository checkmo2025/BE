package checkmo.authentication;

import java.util.List;
import lombok.Builder;

public class AuthenticationEvent {

    @Builder
    public record CreateMember(Long id, String legacyId, String email, List<TermsAgreement> agreements) {
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
    public record ReactivateMember(Long id) {
    }

    public enum VerificationType {
        SIGN_UP, UPDATE_EMAIL
    }
}
