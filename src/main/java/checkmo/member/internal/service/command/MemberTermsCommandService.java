package checkmo.member.internal.service.command;

import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberTerms;
import checkmo.member.internal.entity.Terms;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.repository.MemberTermsRepository;
import checkmo.member.internal.service.query.MemberTermsQueryService;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
    private final MemberTermsRepository memberTermsRepository;

    private final MemberTermsQueryService memberTermsQueryService;

    public void updateAgreements(String memberId, List<TermsAgreementCommand> commands) {
        saveAgreements(memberId, commands, false);
    }

    public void saveSignupAgreements(
            String memberId,
            List<TermsAgreementCommand> commands,
            boolean requireRequiredAgreement
    ) {
        List<TermsAgreementCommand> normalizedCommands = commands == null ? List.of() : commands;
        if (normalizedCommands.isEmpty() && !requireRequiredAgreement) {
            return;
        }

        saveAgreements(memberId, normalizedCommands, requireRequiredAgreement);
    }

    private void saveAgreements(
            String memberId,
            List<TermsAgreementCommand> commands,
            boolean requireRequiredAgreement
    ) {
        validateCommands(commands);

        Member member = memberRepository.findByIdAndDeactivatedAtIsNull(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
        List<Terms> activeTerms = memberTermsQueryService.retrieveActiveTerms();
        Map<Long, Terms> activeTermsById = activeTerms.stream()
                .collect(Collectors.toMap(Terms::getId, Function.identity()));

        commands.forEach(command -> {
            Terms terms = activeTermsById.get(command.termsId());
            if (terms == null) {
                throw new MemberException(MemberErrorStatus.TERMS_NOT_FOUND);
            }
            terms.validateAgreementSubmission(command.agreed());
        });
        validateRequiredTermsSubmitted(activeTerms, commands, requireRequiredAgreement);

        Map<Long, MemberTerms> latestTermsByTermsId = retrieveLatestMemberTermsByTermsId(
                memberId,
                commands.stream()
                        .map(TermsAgreementCommand::termsId)
                        .toList()
        );
        commands.forEach(command -> saveAgreementIfChanged(
                member,
                activeTermsById.get(command.termsId()),
                command.agreed(),
                latestTermsByTermsId.get(command.termsId())
        ));
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

    private void validateRequiredTermsSubmitted(
            List<Terms> activeTerms,
            List<TermsAgreementCommand> commands,
            boolean requireRequiredAgreement
    ) {
        if (!requireRequiredAgreement) {
            return;
        }

        Set<Long> agreedTermsIds = commands.stream()
                .filter(TermsAgreementCommand::agreed)
                .map(TermsAgreementCommand::termsId)
                .collect(Collectors.toSet());
        boolean hasMissingRequiredTerms = activeTerms.stream()
                .filter(Terms::isRequired)
                .map(Terms::getId)
                .anyMatch(requiredTermsId -> !agreedTermsIds.contains(requiredTermsId));
        if (hasMissingRequiredTerms) {
            throw new MemberException(MemberErrorStatus.REQUIRED_TERMS_NOT_AGREED);
        }
    }

    private Map<Long, MemberTerms> retrieveLatestMemberTermsByTermsId(String memberId, List<Long> termsIds) {
        if (termsIds == null || termsIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, MemberTerms> latestTermsByTermsId = new LinkedHashMap<>();
        memberTermsRepository.findLatestByMemberIdAndTermsIdIn(memberId, termsIds)
                .forEach(memberTerms -> latestTermsByTermsId.putIfAbsent(
                        memberTerms.getTerms().getId(),
                        memberTerms
                ));
        return latestTermsByTermsId;
    }

    private void saveAgreementIfChanged(Member member, Terms terms, boolean agreed, MemberTerms latestMemberTerms) {
        if (latestMemberTerms != null && latestMemberTerms.isAgreed() == agreed) {
            return;
        }

        memberTermsRepository.save(MemberTerms.builder()
                .member(member)
                .terms(terms)
                .agreed(agreed)
                .build());
    }
}
