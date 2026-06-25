package checkmo.member.internal.service.command;

import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberTerms;
import checkmo.member.internal.entity.Terms;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.repository.MemberTermsRepository;
import checkmo.member.internal.repository.TermsRepository;
import checkmo.member.internal.service.query.MemberTermsQueryService;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final MemberTermsQueryService memberTermsQueryService;

    public void saveAgreements(String memberId, List<TermsAgreementCommand> commands) {
        validateCommands(commands);

        Member member = memberRepository.findByIdAndDeactivatedAtIsNull(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
        Map<Long, Terms> activeTermsById = memberTermsQueryService.retrieveActiveTerms()
                .stream()
                .collect(Collectors.toMap(Terms::getId, Function.identity()));

        commands.forEach(command -> {
            Terms terms = activeTermsById.get(command.termsId());
            if (terms == null) {
                throw new MemberException(MemberErrorStatus.TERMS_NOT_FOUND);
            }
            validateRequiredAgreement(terms, command.agreed());
        });

        commands.forEach(command -> saveAgreement(
                member,
                activeTermsById.get(command.termsId()),
                command.agreed()
        ));
    }

    public boolean saveAgreement(String memberId, TermsAgreementCommand command) {
        Member member = memberRepository.findByIdAndDeactivatedAtIsNull(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
        Terms terms = termsRepository.findByIdAndActiveTrue(command.termsId())
                .orElseThrow(() -> new MemberException(MemberErrorStatus.TERMS_NOT_FOUND));

        validateRequiredAgreement(terms, command.agreed());

        return saveAgreement(member, terms, command.agreed());
    }

    private void validateCommands(List<TermsAgreementCommand> commands) {
        Set<Long> termsIds = new HashSet<>();
        boolean hasDuplicate = commands.stream()
                .map(TermsAgreementCommand::termsId)
                .anyMatch(termsId -> !termsIds.add(termsId));
        if (hasDuplicate) {
            throw new MemberException(MemberErrorStatus.DUPLICATE_TERMS_AGREEMENT);
        }
    }

    private void validateRequiredAgreement(Terms terms, boolean agreed) {
        if (terms.isRequired() && !agreed) {
            throw new MemberException(MemberErrorStatus.REQUIRED_TERMS_CANNOT_BE_DISAGREED);
        }
    }

    private boolean saveAgreement(Member member, Terms terms, boolean agreed) {
        boolean hasSameLatestState = memberTermsRepository.findLatestCandidates(member.getId(), terms.getId())
                .stream()
                .findFirst()
                .map(MemberTerms::isAgreed)
                .filter(latestAgreed -> latestAgreed == agreed)
                .isPresent();
        if (hasSameLatestState) {
            return false;
        }

        memberTermsRepository.save(MemberTerms.builder()
                .member(member)
                .terms(terms)
                .agreed(agreed)
                .build());
        return true;
    }
}
