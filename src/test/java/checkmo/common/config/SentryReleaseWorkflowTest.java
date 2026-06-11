package checkmo.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class SentryReleaseWorkflowTest {

    private static final Path RELEASE_WORKFLOW = Path.of(".github/workflows/release.yml");
    private static final Pattern PINNED_GITHUB_ACTION = Pattern.compile(
            "^\\s*uses: [\\w.-]+/[\\w.-]+@[0-9a-f]{40}(?:\\s+# .*)?$",
            Pattern.MULTILINE
    );

    @Test
    void releaseWorkflowPublishesBackendReleaseAfterSuccessfulEc2Deploy() throws IOException {
        String workflow = Files.readString(RELEASE_WORKFLOW);

        assertThat(workflow).contains("uses: actions/checkout@34e114876b0b11c390a56381ad16ebd13914f8d5");
        assertThat(workflow).contains("fetch-depth: 0");
        assertThat(workflow).contains("SENTRY_RELEASE=checkmo-backend@%s");
        assertThat(workflow).contains("uses: getsentry/action-release@ff07929a6537bac57790c3451cf4d364aca38528");
        assertThat(workflow).contains("SENTRY_AUTH_TOKEN: ${{ secrets.SENTRY_AUTH_TOKEN }}");
        assertThat(workflow).contains("SENTRY_ORG: checkmo");
        assertThat(workflow).contains("SENTRY_PROJECT: checkmo-spring-boot");
        assertThat(workflow).contains("environment: prod");
        assertThat(workflow).contains("release: checkmo-backend@${{ github.sha }}");

        int deployIndex = workflow.indexOf("- name: Deploy to EC2");
        int sentryIndex = workflow.indexOf("uses: getsentry/action-release@ff07929a6537bac57790c3451cf4d364aca38528");

        assertThat(deployIndex).isGreaterThanOrEqualTo(0);
        assertThat(sentryIndex).isGreaterThanOrEqualTo(0);
        assertThat(deployIndex).isLessThan(sentryIndex);
    }

    @Test
    void releaseWorkflowPinsAllGithubActionsToFullCommitSha() throws IOException {
        String workflow = Files.readString(RELEASE_WORKFLOW);

        assertThat(workflow.lines()
                .filter(line -> line.trim().startsWith("uses: "))
                .toList())
                .allSatisfy(line -> assertThat(line).matches(PINNED_GITHUB_ACTION));
    }

    @Test
    void releaseWorkflowDoesNotContainRawSentryCredentials() throws IOException {
        String workflow = Files.readString(RELEASE_WORKFLOW);

        assertThat(workflow).doesNotContain("sntrys_");
        assertThat(workflow).doesNotContain("Client Secret");
        assertThat(workflow).doesNotContain("SENTRY_AUTH_TOKEN=");
    }
}
