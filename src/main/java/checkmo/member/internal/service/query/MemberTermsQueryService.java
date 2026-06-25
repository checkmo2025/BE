package checkmo.member.internal.service.query;

import checkmo.member.internal.entity.MemberTerms;
import checkmo.member.internal.entity.Terms;
import checkmo.member.internal.entity.TermsType;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.MemberTermsRepository;
import checkmo.member.internal.repository.TermsRepository;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberTermsQueryService {

    private final TermsRepository termsRepository;
    private final MemberTermsRepository memberTermsRepository;

    public List<Terms> retrieveActiveTerms() {
        List<Terms> activeTerms = termsRepository.findAllByActiveTrue();
        validateActiveTermsTypeUnique(activeTerms);

        return activeTerms.stream()
                .sorted(Comparator.comparing(Terms::getTermsType, TermsType.DISPLAY_ORDER))
                .toList();
    }

    public Map<Long, MemberTerms> retrieveLatestMemberTermsByTermsId(String memberId, List<Long> termsIds) {
        if (termsIds == null || termsIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, MemberTerms> latestTermsByTermsId = new LinkedHashMap<>();
        memberTermsRepository.findLatestCandidates(memberId, termsIds)
                .forEach(memberTerms -> latestTermsByTermsId.putIfAbsent(
                        memberTerms.getTerms().getId(),
                        memberTerms
                ));
        return latestTermsByTermsId;
    }

    public boolean hasAgreedAllRequiredActiveTerms(String memberId) {
        List<Terms> requiredTerms = retrieveActiveTerms().stream()
                .filter(Terms::isRequired)
                .toList();
        Map<Long, MemberTerms> latestTermsByTermsId = retrieveLatestMemberTermsByTermsId(
                memberId,
                requiredTerms.stream()
                        .map(Terms::getId)
                        .toList()
        );

        return requiredTerms.stream()
                .allMatch(requiredTerm -> {
                    MemberTerms latestTerms = latestTermsByTermsId.get(requiredTerm.getId());
                    return latestTerms != null && latestTerms.isAgreed();
                });
    }

    private void validateActiveTermsTypeUnique(List<Terms> activeTerms) {
        Map<TermsType, Long> countByTermsType = new EnumMap<>(TermsType.class);
        activeTerms.forEach(terms -> countByTermsType.merge(terms.getTermsType(), 1L, Long::sum));

        boolean hasDuplicate = countByTermsType.values().stream()
                .anyMatch(count -> count > 1);
        if (hasDuplicate) {
            throw new MemberException(MemberErrorStatus.DUPLICATE_ACTIVE_TERMS_TYPE);
        }
    }
}
