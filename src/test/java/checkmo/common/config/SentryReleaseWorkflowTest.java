package checkmo.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SentryReleaseWorkflowTest {

    private static final Path RELEASE_WORKFLOW = Path.of(".github/workflows/release.yml");

    @Test
    void releaseWorkflowPublishesBackendReleaseAfterSuccessfulEc2Deploy() throws IOException {
        String workflow = Files.readString(RELEASE_WORKFLOW);

        assertThat(workflow).contains("uses: actions/checkout@v4");
        assertThat(workflow).contains("fetch-depth: 0");
        assertThat(workflow).contains("SENTRY_RELEASE=checkmo-backend@%s");
        assertThat(workflow).contains("uses: getsentry/action-release@v3");
        assertThat(workflow).contains("SENTRY_AUTH_TOKEN: ${{ secrets.SENTRY_AUTH_TOKEN }}");
        assertThat(workflow).contains("SENTRY_ORG: checkmo");
        assertThat(workflow).contains("SENTRY_PROJECT: checkmo-spring-boot");
        assertThat(workflow).contains("environment: prod");
        assertThat(workflow).contains("release: checkmo-backend@${{ github.sha }}");
        assertThat(workflow.indexOf("name: Deploy to EC2"))
                .isLessThan(workflow.indexOf("uses: getsentry/action-release@v3"));
    }

    @Test
    void releaseWorkflowDoesNotContainRawSentryCredentials() throws IOException {
        String workflow = Files.readString(RELEASE_WORKFLOW);

        assertThat(workflow).doesNotContain("sntrys_");
        assertThat(workflow).doesNotContain("Client Secret");
        assertThat(workflow).doesNotContain("SENTRY_AUTH_TOKEN=");
    }
}
