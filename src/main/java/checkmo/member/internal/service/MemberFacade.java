package checkmo.member.internal.service;

import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.service.command.MemberCommandService;
import checkmo.member.internal.service.command.MemberTermsCommandService;
import checkmo.member.internal.service.command.TermsAgreementCommand;
import checkmo.member.internal.service.query.MemberTermsQueryService;
import checkmo.member.web.dto.MemberRequestDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberFacade {

    private final MemberCommandService memberCommandService;
    private final MemberTermsCommandService memberTermsCommandService;
    private final MemberTermsQueryService memberTermsQueryService;

    @Value("${checkmo.terms.enforcement-enabled:false}")
    private boolean termsEnforcementEnabled;

    public void createMember(
            Long memberId,
            String legacyId,
            String email,
            List<TermsAgreementCommand> termsAgreements,
            boolean requireRequiredTermsAgreement
    ) {
        memberCommandService.createMember(memberId, legacyId, email);
        memberTermsCommandService.saveSignupAgreements(
                memberId,
                termsAgreements,
                termsEnforcementEnabled && requireRequiredTermsAgreement
        );
    }

    public void addAdditionalInfo(Long memberId, MemberRequestDTO.AdditionalInfo request) {
        validateRequiredTermsBeforeProfileCompletion(memberId);
        memberCommandService.addAdditionalInfo(memberId, request);
    }

    private void validateRequiredTermsBeforeProfileCompletion(Long memberId) {
        if (!termsEnforcementEnabled) {
            return;
        }

        if (!memberTermsQueryService.hasAgreedAllRequiredActiveTerms(memberId)) {
            throw new MemberException(MemberErrorStatus.REQUIRED_TERMS_NOT_AGREED);
        }
    }
}
