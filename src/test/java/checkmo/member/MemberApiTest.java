package checkmo.member;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import checkmo.member.internal.entity.MemberTerms;
import checkmo.member.internal.entity.Terms;
import checkmo.member.internal.entity.TermsType;
import checkmo.member.internal.repository.MemberTermsRepository;
import checkmo.member.internal.repository.TermsRepository;
import checkmo.support.ApiTestSupport;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class MemberApiTest extends ApiTestSupport {

    @Autowired
    TermsRepository termsRepository;

    @Autowired
    MemberTermsRepository memberTermsRepository;

    @Test
    void 활성_약관_목록은_공개로_표시_순서대로_조회된다() {
        Terms marketing = saveTerms(TermsType.MARKETING, "마케팅", true, false);
        Terms privacy = saveTerms(TermsType.PRIVACY_COLLECTION, "개인정보", true, true);
        Terms service = saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);
        Terms thirdParty = saveTerms(TermsType.THIRD_PARTY_PROVISION, "제3자", true, false);
        saveTerms(TermsType.SERVICE_TERMS, "비활성 서비스", false, true);

        ExtractableResponse<Response> response = given()
                .when()
                .get("/api/v1/terms")
                .then()
                .statusCode(200)
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.jsonPath().getList("result.terms.termsType", String.class))
                    .containsExactly(
                            "SERVICE_TERMS",
                            "PRIVACY_COLLECTION",
                            "THIRD_PARTY_PROVISION",
                            "MARKETING"
                    );
            softly.assertThat(response.jsonPath().getList("result.terms.id", Long.class))
                    .containsExactly(service.getId(), privacy.getId(), thirdParty.getId(), marketing.getId());
            softly.assertThat(response.jsonPath().getList("result.terms.required", Boolean.class))
                    .containsExactly(true, true, false, false);
            softly.assertThat(response.jsonPath().getList("result.terms.termUrl", String.class))
                    .allSatisfy(termUrl -> assertThat(termUrl).startsWith("https://example.com/"));
        });
    }

    @Test
    void 활성_약관_종류가_중복되면_공개_조회는_서버_오류로_실패한다() {
        saveTerms(TermsType.SERVICE_TERMS, "서비스 v1", true, true);
        saveTerms(TermsType.SERVICE_TERMS, "서비스 v2", true, true);

        given()
                .when()
                .get("/api/v1/terms")
                .then()
                .statusCode(500)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("TERMS_500"));
    }

    @Test
    void 회원_약관_상태는_프로필_미완성_회원도_조회할_수_있다() {
        TestUser user = createIncompleteUser();
        Terms service = saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);
        Terms marketing = saveTerms(TermsType.MARKETING, "마케팅", true, false);
        saveMemberTerms(user.id(), marketing, true);

        ExtractableResponse<Response> response = given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/members/me/terms")
                .then()
                .statusCode(200)
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.jsonPath().getBoolean("result.requiresRequiredAgreement")).isTrue();
            softly.assertThat(response.jsonPath().getList("result.terms.id", Long.class))
                    .containsExactly(service.getId(), marketing.getId());
            softly.assertThat(response.jsonPath().getList("result.terms.agreed", Boolean.class))
                    .containsExactly(false, true);
        });
    }

    @Test
    void 약관_동의_저장은_성공하고_최신_상태로_조회된다() {
        TestUser user = createUser();
        Terms service = saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);
        Terms privacy = saveTerms(TermsType.PRIVACY_COLLECTION, "개인정보", true, true);
        Terms marketing = saveTerms(TermsType.MARKETING, "마케팅", true, false);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of("agreements", List.of(
                        Map.of("termsId", service.getId(), "agreed", true),
                        Map.of("termsId", privacy.getId(), "agreed", true),
                        Map.of("termsId", marketing.getId(), "agreed", true)
                )))
                .when()
                .post("/api/v1/members/me/terms")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));

        ExtractableResponse<Response> response = given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/members/me/terms")
                .then()
                .statusCode(200)
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.jsonPath().getBoolean("result.requiresRequiredAgreement")).isFalse();
            softly.assertThat(response.jsonPath().getList("result.terms.agreed", Boolean.class))
                    .containsExactly(true, true, true);
            softly.assertThat(memberTermsRepository.count()).isEqualTo(3);
        });
    }

    @Test
    void 중복된_약관_id_제출은_거절된다() {
        TestUser user = createUser();
        Terms service = saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of("agreements", List.of(
                        Map.of("termsId", service.getId(), "agreed", true),
                        Map.of("termsId", service.getId(), "agreed", true)
                )))
                .when()
                .post("/api/v1/members/me/terms")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("TERMS_402"));

        assertThat(memberTermsRepository.count()).isZero();
    }

    @Test
    void 비활성_약관과_존재하지_않는_약관_제출은_거절된다() {
        TestUser user = createUser();
        Terms inactive = saveTerms(TermsType.SERVICE_TERMS, "비활성 서비스", false, true);
        saveTerms(TermsType.SERVICE_TERMS, "활성 서비스", true, true);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of("agreements", List.of(Map.of("termsId", inactive.getId(), "agreed", true))))
                .when()
                .post("/api/v1/members/me/terms")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("TERMS_400"));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of("agreements", List.of(Map.of("termsId", 999_999L, "agreed", true))))
                .when()
                .post("/api/v1/members/me/terms")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("TERMS_400"));

        assertThat(memberTermsRepository.count()).isZero();
    }

    @Test
    void 필수_약관_비동의와_빈_제출은_거절된다() {
        TestUser user = createUser();
        Terms service = saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of("agreements", List.of(Map.of("termsId", service.getId(), "agreed", false))))
                .when()
                .post("/api/v1/members/me/terms")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("TERMS_401"));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of("agreements", List.of()))
                .when()
                .post("/api/v1/members/me/terms")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body("{}")
                .when()
                .post("/api/v1/members/me/terms")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body("{\"agreements\":[null]}")
                .when()
                .post("/api/v1/members/me/terms")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of("agreements", List.of(Map.of("termsId", service.getId()))))
                .when()
                .post("/api/v1/members/me/terms")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));

        assertThat(memberTermsRepository.count()).isZero();
    }

    @Test
    void 선택_약관은_철회하고_다시_동의할_수_있다() {
        TestUser user = createUser();
        Terms marketing = saveTerms(TermsType.MARKETING, "마케팅", true, false);

        postAgreement(user, marketing, true);
        postAgreement(user, marketing, false);
        postAgreement(user, marketing, true);

        ExtractableResponse<Response> response = given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/members/me/terms")
                .then()
                .statusCode(200)
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.jsonPath().getList("result.terms.agreed", Boolean.class))
                    .containsExactly(true);
            softly.assertThat(memberTermsRepository.countByMemberIdAndTermsId(user.id(), marketing.getId()))
                    .isEqualTo(3);
        });
    }

    @Test
    void additionalInfoCompletesIncompleteProfile() {
        TestUser user = createIncompleteUser();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of(
                        "nickname", "complete" + user.id().substring(user.id().length() - 4).toLowerCase(),
                        "name", "완료",
                        "phoneNumber", "010-1234-5678",
                        "description", "소개",
                        "categories", List.of("COMPUTER_IT")
                ))
                .when()
                .post("/api/v1/members/additional-info")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    void additionalInfoPersistsNicknameForSignupCreatedIncompleteUser() {
        String email = "additional-info-null-nickname@example.com";
        String nickname = "completeinfo";
        when(redisHashOperations.get("verification:" + email, "verified")).thenReturn(true);

        ExtractableResponse<Response> signUpResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email", email, "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .statusCode(200)
                .extract();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie("accessToken", signUpResponse.cookie("accessToken"))
                .body(Map.of(
                        "nickname", nickname,
                        "name", "완료",
                        "phoneNumber", "010-1234-5678",
                        "description", "소개",
                        "categories", List.of("COMPUTER_IT")
                ))
                .when()
                .post("/api/v1/members/additional-info")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));

        var authUser = authRepository.findByEmail(email).orElseThrow();
        var member = memberRepository.findById(authUser.getId()).orElseThrow();

        assertThat(authUser.getNickName()).isEqualTo(nickname);
        assertThat(member.getNickName()).isEqualTo(nickname);
        assertThat(authUser.isProfileCompleted()).isTrue();
    }

    @Test
    void profileReadAndUpdateSucceedForCompletedUser() {
        TestUser user = createUser();

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/members/me")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of(
                        "description", "수정소개",
                        "phoneNumber", "010-1111-2222",
                        "categories", List.of("COMPUTER_IT", "ESSAY")
                ))
                .when()
                .patch("/api/v1/members/me")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    void removedMemberRefreshEndpointIsUnavailableForAuthenticatedUser() {
        TestUser user = createUser();

        ExtractableResponse<Response> response = given()
                .cookie(accessTokenCookie(user))
                .when()
                .post("/api/v1/members/me/refresh")
                .then()
                .extract();

        assertThat(response.statusCode()).isIn(400, 404, 405);
        if (response.statusCode() == 400) {
            assertThat(response.jsonPath().getString("message"))
                    .contains("No static resource api/v1/members/me/refresh");
        }
        assertThat(response.jsonPath().getString("result.refreshToken")).isNull();
    }

    @Test
    void incompleteProfileCannotReadProtectedProfile() {
        TestUser user = createIncompleteUser();

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/members/me")
                .then()
                .statusCode(403)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void followListAndUnfollowFlowSucceeds() {
        TestUser me = createUser();
        TestUser target = createUser();

        given()
                .cookie(accessTokenCookie(me))
                .when()
                .post("/api/v1/members/{memberNickname}/following", target.nickName())
                .then()
                .statusCode(200);

        given()
                .cookie(accessTokenCookie(me))
                .when()
                .get("/api/v1/members/me/following")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));

        given()
                .cookie(accessTokenCookie(me))
                .when()
                .delete("/api/v1/members/{memberNickname}/following", target.nickName())
                .then()
                .statusCode(200);
    }

    @Test
    void selfFollowIsRejected() {
        TestUser user = createUser();

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .post("/api/v1/members/{memberNickname}/following", user.nickName())
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void followerDeleteFlowSucceeds() {
        TestUser me = createUser();
        TestUser follower = createUser();

        given()
                .cookie(accessTokenCookie(follower))
                .when()
                .post("/api/v1/members/{memberNickname}/following", me.nickName())
                .then()
                .statusCode(200);

        given()
                .cookie(accessTokenCookie(me))
                .when()
                .delete("/api/v1/members/{memberNickname}/follower", follower.nickName())
                .then()
                .statusCode(200);
    }

    @Test
    void blockListAndUnblockFlowSucceeds() {
        TestUser me = createUser();
        TestUser target = createUser();

        given()
                .cookie(accessTokenCookie(me))
                .when()
                .post("/api/v1/members/{memberNickname}/block", target.nickName())
                .then()
                .statusCode(200);

        given()
                .cookie(accessTokenCookie(me))
                .when()
                .get("/api/v1/members/me/blocks")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));

        given()
                .cookie(accessTokenCookie(me))
                .when()
                .delete("/api/v1/members/{memberNickname}/block", target.nickName())
                .then()
                .statusCode(200);
    }

    @Test
    void selfBlockIsRejected() {
        TestUser user = createUser();

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .post("/api/v1/members/{memberNickname}/block", user.nickName())
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void otherProfileAndFollowListsAreReadable() {
        TestUser me = createUser();
        TestUser target = createUser();

        given()
                .cookie(accessTokenCookie(me))
                .when()
                .get("/api/v1/members/{memberNickname}", target.nickName())
                .then()
                .statusCode(200)
                .body("result.nickname", equalTo(target.nickName()))
                .body("result.following", equalTo(false))
                .body("result.followerCount", equalTo(0))
                .body("result.followingCount", equalTo(0));

        given()
                .cookie(accessTokenCookie(me))
                .when()
                .get("/api/v1/members/{memberNickname}/followings", target.nickName())
                .then()
                .statusCode(200)
                .body("result.followList.size()", equalTo(0))
                .body("result.hasNext", equalTo(false));

        given()
                .cookie(accessTokenCookie(me))
                .when()
                .get("/api/v1/members/{memberNickname}/followers", target.nickName())
                .then()
                .statusCode(200)
                .body("result.followList.size()", equalTo(0))
                .body("result.hasNext", equalTo(false));
    }

    @Test
    void updatePasswordSucceedsAndRejectsWrongCurrentPassword() {
        TestUser user = createUserWithPassword("Pass123!");

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of(
                        "currentPassword", "Pass123!",
                        "newPassword", "Next123!",
                        "confirmPassword", "Next123!"
                ))
                .when()
                .patch("/api/v1/members/me/update-password")
                .then()
                .statusCode(200);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of(
                        "currentPassword", "bad123!",
                        "newPassword", "Other123!",
                        "confirmPassword", "Other123!"
                ))
                .when()
                .patch("/api/v1/members/me/update-password")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void updateEmailSucceedsAfterVerification() {
        TestUser user = createUserWithPassword("Pass123!");
        String newEmail = "changed-" + user.id().substring(user.id().length() - 4).toLowerCase() + "@example.com";
        when(redisHashOperations.get("verification:" + newEmail, "code")).thenReturn("123456");
        when(redisHashOperations.get("verification:" + newEmail, "verified")).thenReturn(false);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of(
                        "currentEmail", user.email(),
                        "newEmail", newEmail,
                        "verificationCode", "123456"
                ))
                .when()
                .patch("/api/v1/members/me/update-email")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    void loginStatusRecommendationFollowCountAndWithdrawalSucceed() {
        TestUser user = createUser();

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/members/me/login-status")
                .then()
                .statusCode(200)
                .body("result.provider", equalTo("LOCAL"));

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/members/me/recommend")
                .then()
                .statusCode(200)
                .body("result.friends.size()", equalTo(0));

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/members/me/follow-count")
                .then()
                .statusCode(200)
                .body("result.followerCount", equalTo(0))
                .body("result.followingCount", equalTo(0));

        given()
                .cookie(accessTokenCookie(user))
                .cookie(refreshTokenCookie(user))
                .when()
                .post("/api/v1/members/withdrawal")
                .then()
                .statusCode(200);
    }

    private void postAgreement(TestUser user, Terms terms, boolean agreed) {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie(accessTokenCookie(user))
                .body(Map.of("agreements", List.of(Map.of(
                        "termsId", terms.getId(),
                        "agreed", agreed
                ))))
                .when()
                .post("/api/v1/members/me/terms")
                .then()
                .statusCode(200);
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

    private void saveMemberTerms(String memberId, Terms terms, boolean agreed) {
        memberRepository.findById(memberId)
                .map(member -> MemberTerms.builder()
                        .member(member)
                        .terms(terms)
                        .agreed(agreed)
                        .build())
                .ifPresent(memberTermsRepository::save);
    }
}
