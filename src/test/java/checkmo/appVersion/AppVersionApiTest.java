package checkmo.appVersion;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import checkmo.appVersion.internal.entity.AppPlatform;
import checkmo.appVersion.internal.entity.AppVersionPolicy;
import checkmo.appVersion.internal.repository.AppVersionPolicyRepository;
import checkmo.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AppVersionApiTest extends ApiTestSupport {

    @Autowired
    AppVersionPolicyRepository appVersionPolicyRepository;

    @Test
    void 앱_버전_정책은_로그인_없이_플랫폼별로_조회된다() {
        savePolicy(AppPlatform.IOS, "1.0.2", "1.1.0", "https://apps.apple.com/app/id000000000");

        given()
                .queryParam("platform", "ios")
                .when()
                .get("/api/v1/app/version")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("result.minSupportedVersion", equalTo("1.0.2"))
                .body("result.latestVersion", equalTo("1.1.0"))
                .body("result.storeUrl", equalTo("https://apps.apple.com/app/id000000000"));
    }

    @Test
    void 앱_버전_정책은_android_플랫폼도_조회된다() {
        savePolicy(
                AppPlatform.ANDROID,
                "1.0.0",
                "1.0.3",
                "https://play.google.com/store/apps/details?id=kr.co.checkmo.app"
        );

        given()
                .queryParam("platform", "android")
                .when()
                .get("/api/v1/app/version")
                .then()
                .statusCode(200)
                .body("result.minSupportedVersion", equalTo("1.0.0"))
                .body("result.latestVersion", equalTo("1.0.3"))
                .body("result.storeUrl", equalTo("https://play.google.com/store/apps/details?id=kr.co.checkmo.app"));
    }

    @Test
    void 지원하지_않는_platform은_거절된다() {
        given()
                .queryParam("platform", "web")
                .when()
                .get("/api/v1/app/version")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("APP_VERSION_400"));
    }

    @Test
    void 활성_버전_정책이_없으면_404를_반환한다() {
        given()
                .queryParam("platform", "ios")
                .when()
                .get("/api/v1/app/version")
                .then()
                .statusCode(404)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("APP_VERSION_404"));
    }

    private AppVersionPolicy savePolicy(
            AppPlatform platform,
            String minSupportedVersion,
            String latestVersion,
            String storeUrl
    ) {
        return appVersionPolicyRepository.save(AppVersionPolicy.builder()
                .platform(platform)
                .minSupportedVersion(minSupportedVersion)
                .latestVersion(latestVersion)
                .storeUrl(storeUrl)
                .active(true)
                .build());
    }
}
