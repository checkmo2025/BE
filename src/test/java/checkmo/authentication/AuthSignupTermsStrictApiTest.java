package checkmo.authentication;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.member.internal.entity.Terms;
import checkmo.member.internal.entity.TermsType;
import checkmo.member.internal.repository.MemberTermsRepository;
import checkmo.member.internal.repository.TermsRepository;
import checkmo.support.ApiTest;
import checkmo.support.ApiTestSupport;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@ApiTest
@TestPropertySource(properties = "checkmo.terms.enforcement-enabled=true")
class AuthSignupTermsStrictApiTest extends ApiTestSupport {

    @Autowired
    TermsRepository termsRepository;

    @Autowired
    MemberTermsRepository memberTermsRepository;

    @Test
    void strict_모드에서는_필수_약관_없는_기존_회원가입_payload를_거절한다() {
        saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);
        String email = "strict-missing-terms@example.com";
        when(redisHashOperations.get("verification:" + email, "verified")).thenReturn(true);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email", email, "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .statusCode(400)
                .body("code", equalTo("TERMS_403"));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(authRepository.findByEmail(email)).isEmpty();
            softly.assertThat(memberTermsRepository.count()).isZero();
        });
        verify(redisTemplate, never()).delete("verification:" + email);
    }

    @Test
    void strict_모드에서는_활성_필수_약관에_동의하면_회원가입된다() {
        Terms service = saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);
        Terms marketing = saveTerms(TermsType.MARKETING, "마케팅", true, false);
        String email = "strict-signup-terms@example.com";
        when(redisHashOperations.get("verification:" + email, "verified")).thenReturn(true);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "email", email,
                        "password", "Pass123!",
                        "agreements", List.of(
                                Map.of("termsId", service.getId(), "agreed", true),
                                Map.of("termsId", marketing.getId(), "agreed", false)
                        )
                ))
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .statusCode(200)
                .body("result.email", equalTo(email));

        String memberId = authRepository.findByEmail(email).orElseThrow().getId();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(memberRepository.existsById(memberId)).isTrue();
            softly.assertThat(memberTermsRepository.countByMember_IdAndTerms_Id(memberId, service.getId())).isOne();
            softly.assertThat(memberTermsRepository.countByMember_IdAndTerms_Id(memberId, marketing.getId())).isOne();
        });
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
