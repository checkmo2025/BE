package checkmo.member.internal.converter;

import checkmo.member.internal.entity.MemberTerms;
import checkmo.member.internal.entity.Terms;
import checkmo.member.web.dto.TermsResponseDTO.MemberTermsInfo;
import checkmo.member.web.dto.TermsResponseDTO.MemberTermsStatus;
import checkmo.member.web.dto.TermsResponseDTO.PublicTermsList;
import checkmo.member.web.dto.TermsResponseDTO.TermsInfo;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TermsConverter {

    public static PublicTermsList toPublicTermsList(List<Terms> terms) {
        return PublicTermsList.builder()
                .terms(terms.stream()
                        .map(TermsConverter::toTermsInfo)
                        .toList())
                .build();
    }

    public static MemberTermsStatus toMemberTermsStatus(
            List<Terms> activeTerms,
            Map<Long, MemberTerms> latestTermsByTermsId
    ) {
        List<MemberTermsInfo> terms = activeTerms.stream()
                .map(activeTerm -> toMemberTermsInfo(
                        activeTerm,
                        latestTermsByTermsId.get(activeTerm.getId())
                ))
                .toList();

        boolean requiresRequiredAgreement = terms.stream()
                .anyMatch(term -> term.isRequired() && !term.isAgreed());

        return MemberTermsStatus.builder()
                .requiresRequiredAgreement(requiresRequiredAgreement)
                .terms(terms)
                .build();
    }

    private static TermsInfo toTermsInfo(Terms terms) {
        return TermsInfo.builder()
                .id(terms.getId())
                .termsType(terms.getTermsType())
                .title(terms.getTitle())
                .termUrl(terms.getTermUrl())
                .version(terms.getVersion())
                .required(terms.isRequired())
                .build();
    }

    private static MemberTermsInfo toMemberTermsInfo(Terms terms, MemberTerms memberTerms) {
        return MemberTermsInfo.builder()
                .id(terms.getId())
                .termsType(terms.getTermsType())
                .title(terms.getTitle())
                .termUrl(terms.getTermUrl())
                .version(terms.getVersion())
                .required(terms.isRequired())
                .agreed(memberTerms != null && memberTerms.isAgreed())
                .build();
    }
}
