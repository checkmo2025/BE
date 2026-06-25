package checkmo.member;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import checkmo.member.internal.entity.Terms;
import checkmo.member.internal.entity.TermsType;
import checkmo.member.internal.repository.TermsRepository;
import checkmo.support.ApiTestSupport;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "checkmo.terms.enforcement-enabled=true")
class MemberProfileTermsStrictApiTest extends ApiTestSupport {

    @Autowired
    TermsRepository termsRepository;

    @Test
    void 프로필_미완성_회원도_약관_api는_호출할_수_있다() {
        TestUser user = createIncompleteUser();
        Terms service = saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(given()
                    .when()
                    .get("/api/v1/terms")
                    .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getList("result.terms.id", Long.class)).containsExactly(service.getId());

            softly.assertThat(given()
                    .cookie(accessTokenCookie(user))
                    .when()
                    .get("/api/v1/members/me/terms")
                    .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getBoolean("result.requiresRequiredAgreement")).isTrue();
        });
    }

    @Test
    void strict_모드에서는_필수_약관_동의_전_프로필_완성을_거절한다() {
        TestUser user = createIncompleteUser();
        saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(additionalInfoBody(user, "blocked"))
                .when()
                .post("/api/v1/members/additional-info")
                .then()
                .statusCode(400)
                .body("code", equalTo("TERMS_403"));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(authRepository.findById(user.id()).orElseThrow().isProfileCompleted()).isFalse();
            softly.assertThat(memberRepository.findById(user.id()).orElseThrow().getNickName()).isNull();
        });
    }

    @Test
    void strict_모드에서는_필수_약관_동의_후_프로필을_완성할_수_있다() {
        TestUser user = createIncompleteUser();
        Terms service = saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of("agreements", List.of(Map.of(
                        "termsId", service.getId(),
                        "agreed", true
                ))))
                .when()
                .post("/api/v1/members/me/terms")
                .then()
                .statusCode(200);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(additionalInfoBody(user, "allowed"))
                .when()
                .post("/api/v1/members/additional-info")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(authRepository.findById(user.id()).orElseThrow().isProfileCompleted()).isTrue();
            softly.assertThat(memberRepository.findById(user.id()).orElseThrow().getNickName()).startsWith("allowed");
        });
    }

    private Map<String, Object> additionalInfoBody(TestUser user, String prefix) {
        return Map.of(
                "nickname", prefix + user.id().substring(user.id().length() - 4).toLowerCase(),
                "name", "완료",
                "phoneNumber", "010-1234-5678",
                "description", "소개",
                "categories", List.of("COMPUTER_IT")
        );
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
