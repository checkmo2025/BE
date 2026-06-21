package checkmo.member;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import checkmo.support.ApiTestSupport;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class MemberApiTest extends ApiTestSupport {

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
}
