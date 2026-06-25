package checkmo.member.internal.service.command;

import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberTerms;
import checkmo.member.internal.entity.Terms;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.repository.MemberTermsRepository;
import checkmo.member.internal.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberTermsCommandService {

    private final MemberRepository memberRepository;
    private final TermsRepository termsRepository;
    private final MemberTermsRepository memberTermsRepository;

    public boolean saveAgreement(String memberId, TermsAgreementCommand command) {
        Member member = memberRepository.findByIdAndDeactivatedAtIsNull(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
        Terms terms = termsRepository.findByIdAndActiveTrue(command.termsId())
                .orElseThrow(() -> new MemberException(MemberErrorStatus.TERMS_NOT_FOUND));

        if (terms.isRequired() && !command.agreed()) {
            throw new MemberException(MemberErrorStatus.REQUIRED_TERMS_CANNOT_BE_DISAGREED);
        }

        boolean hasSameLatestState = memberTermsRepository.findLatestCandidates(memberId, terms.getId())
                .stream()
                .findFirst()
                .map(MemberTerms::isAgreed)
                .filter(agreed -> agreed == command.agreed())
                .isPresent();
        if (hasSameLatestState) {
            return false;
        }

        memberTermsRepository.save(MemberTerms.builder()
                .member(member)
                .terms(terms)
                .agreed(command.agreed())
                .build());
        return true;
    }
}
