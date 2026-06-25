package checkmo.authentication;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.member.internal.entity.MemberTerms;
import checkmo.member.internal.entity.Terms;
import checkmo.member.internal.entity.TermsType;
import checkmo.member.internal.repository.MemberTermsRepository;
import checkmo.member.internal.repository.TermsRepository;
import checkmo.support.ApiTestSupport;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class AuthSignupTermsApiTest extends ApiTestSupport {

    @Autowired
    TermsRepository termsRepository;

    @Autowired
    MemberTermsRepository memberTermsRepository;

    @Test
    void 회원가입은_제출한_약관_동의_이력을_회원과_함께_저장한다() {
        Terms service = saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);
        Terms privacy = saveTerms(TermsType.PRIVACY_COLLECTION, "개인정보", true, true);
        Terms marketing = saveTerms(TermsType.MARKETING, "마케팅", true, false);
        String email = "signup-terms@example.com";
        when(redisHashOperations.get("verification:" + email, "verified")).thenReturn(true);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "email", email,
                        "password", "Pass123!",
                        "agreements", List.of(
                                Map.of("termsId", service.getId(), "agreed", true),
                                Map.of("termsId", privacy.getId(), "agreed", true),
                                Map.of("termsId", marketing.getId(), "agreed", false)
                        )
                ))
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .statusCode(200);

        String memberId = authRepository.findByEmail(email).orElseThrow().getId();
        Map<Long, MemberTerms> latestTerms = memberTermsRepository.findLatestCandidates(
                        memberId,
                        List.of(service.getId(), privacy.getId(), marketing.getId())
                )
                .stream()
                .collect(Collectors.toMap(
                        memberTerms -> memberTerms.getTerms().getId(),
                        Function.identity()
                ));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(memberRepository.existsById(memberId)).isTrue();
            softly.assertThat(latestTerms).containsOnlyKeys(service.getId(), privacy.getId(), marketing.getId());
            softly.assertThat(latestTerms.get(service.getId()).isAgreed()).isTrue();
            softly.assertThat(latestTerms.get(privacy.getId()).isAgreed()).isTrue();
            softly.assertThat(latestTerms.get(marketing.getId()).isAgreed()).isFalse();
        });
        verify(redisTemplate).delete("verification:" + email);
    }

    @Test
    void 회원가입_약관_id가_중복되면_롤백하고_이메일_인증정보를_삭제하지_않는다() {
        Terms service = saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);
        String email = "signup-duplicate-terms@example.com";
        when(redisHashOperations.get("verification:" + email, "verified")).thenReturn(true);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "email", email,
                        "password", "Pass123!",
                        "agreements", List.of(
                                Map.of("termsId", service.getId(), "agreed", true),
                                Map.of("termsId", service.getId(), "agreed", true)
                        )
                ))
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .statusCode(400)
                .body("code", equalTo("TERMS_402"));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(authRepository.findByEmail(email)).isEmpty();
            softly.assertThat(memberTermsRepository.count()).isZero();
        });
        verify(redisTemplate, never()).delete("verification:" + email);
    }

    private Terms saveTerms(TermsType termsType, String title, boolean active, boolean required) {
        return termsRepository.save(Terms.builder()
                .termsType(termsType)
                .title(title)
                .termUrl("https://example.com/" + UUID.randomUUID())
                .version((int) termsRepository.count() + 1)
                .active(active)
                .required(required)
                .build());
    }
}
