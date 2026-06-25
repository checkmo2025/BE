package checkmo.member.internal.service.command;

import java.util.Objects;

public record TermsAgreementCommand(Long termsId, boolean agreed) {
    public TermsAgreementCommand {
        Objects.requireNonNull(termsId, "termsId must not be null");
    }
}
