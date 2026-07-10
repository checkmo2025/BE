package checkmo.notification;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import checkmo.notification.internal.entity.PushDevice;
import checkmo.notification.internal.repository.PushDeviceRepository;
import checkmo.support.ApiTestSupport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class PushDeviceApiTest extends ApiTestSupport {

    private static final String INSTALLATION_A = "11111111-1111-4111-8111-111111111111";
    private static final String INSTALLATION_B = "22222222-2222-4222-8222-222222222222";
    private static final String TOKEN_A = "ExponentPushToken[test-reactivation-a]";
    private static final String TOKEN_B = "ExponentPushToken[test-reactivation-b]";

    @Autowired
    PushDeviceRepository pushDeviceRepository;

    @Test
    void 비활성화한_동일_설치_ID를_다시_등록하면_기기가_재활성화된다() {
        TestUser user = createUser();
        long originalDeviceId = registerPushDevice(user, INSTALLATION_A, TOKEN_A);

        deregisterPushDevice(user, INSTALLATION_A);

        PushDevice inactiveDevice = findDevice(INSTALLATION_A);
        assertThat(inactiveDevice.isActive()).isFalse();
        assertThat(inactiveDevice.getExpoPushToken()).isNull();
        assertThat(inactiveDevice.getDeactivatedAt()).isNotNull();

        long reactivatedDeviceId = registerPushDevice(user, INSTALLATION_A, TOKEN_A);

        PushDevice reactivatedDevice = findDevice(INSTALLATION_A);
        assertThat(reactivatedDeviceId).isEqualTo(originalDeviceId);
        assertThat(pushDeviceRepository.count()).isOne();
        assertThat(reactivatedDevice.isActive()).isTrue();
        assertThat(reactivatedDevice.getExpoPushToken()).isEqualTo(TOKEN_A);
        assertThat(reactivatedDevice.getDeactivatedAt()).isNull();
        assertThat(reactivatedDevice.getMemberId()).isEqualTo(user.memberId());
    }

    @Test
    void 비활성_설치가_다른_설치의_토큰으로_재등록되면_토큰_소유권이_이동한다() {
        TestUser user = createUser();
        long originalDeviceId = registerPushDevice(user, INSTALLATION_A, TOKEN_A);
        deregisterPushDevice(user, INSTALLATION_A);
        registerPushDevice(user, INSTALLATION_B, TOKEN_B);

        long reactivatedDeviceId = registerPushDevice(user, INSTALLATION_A, TOKEN_B);

        PushDevice reactivatedDevice = findDevice(INSTALLATION_A);
        PushDevice conflictingDevice = findDevice(INSTALLATION_B);
        assertThat(reactivatedDeviceId).isEqualTo(originalDeviceId);
        assertThat(reactivatedDevice.isActive()).isTrue();
        assertThat(reactivatedDevice.getExpoPushToken()).isEqualTo(TOKEN_B);
        assertThat(conflictingDevice.isActive()).isFalse();
        assertThat(conflictingDevice.getExpoPushToken()).isNull();
    }

    private long registerPushDevice(TestUser user, String installationId, String expoPushToken) {
        return given()
                .cookie(accessTokenCookie(user))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "installationId", installationId,
                        "expoPushToken", expoPushToken,
                        "platform", "ANDROID",
                        "appVersion", "1.1.8",
                        "buildNumber", "1"
                ))
                .when()
                .put("/api/v1/notifications/push-devices")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("result.installationId", equalTo(installationId))
                .body("result.active", equalTo(true))
                .extract()
                .jsonPath()
                .getLong("result.deviceId");
    }

    private void deregisterPushDevice(TestUser user, String installationId) {
        given()
                .cookie(accessTokenCookie(user))
                .when()
                .delete("/api/v1/notifications/push-devices/{installationId}", installationId)
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    private PushDevice findDevice(String installationId) {
        return pushDeviceRepository.findByInstallationId(installationId).orElseThrow();
    }
}
