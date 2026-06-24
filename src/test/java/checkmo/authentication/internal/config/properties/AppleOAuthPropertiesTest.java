package checkmo.authentication.internal.config.properties;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class AppleOAuthPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(loadOAuth2Yaml())
            .withPropertyValues(
                    "APPLE_TEAM_ID=test-team-id",
                    "APPLE_KEY_ID=test-key-id",
                    "APPLE_WEB_CLIENT_ID=test-web-client-id",
                    "APPLE_IOS_CLIENT_ID=test-ios-client-id",
                    "APPLE_PRIVATE_KEY_BASE64=test-key"
            )
            .withUserConfiguration(AppleOAuthPropertiesConfiguration.class);

    @Test
    void bindsAppleOAuthPropertiesFromOAuth2Profile() {
        contextRunner.run(context -> {
            AppleOAuthProperties properties = context.getBean(AppleOAuthProperties.class);

            assertSoftly(softly -> {
                softly.assertThat(properties.getTeamId()).isEqualTo("test-team-id");
                softly.assertThat(properties.getKeyId()).isEqualTo("test-key-id");
                softly.assertThat(properties.getWebClientId()).isEqualTo("test-web-client-id");
                softly.assertThat(properties.getIosClientId()).isEqualTo("test-ios-client-id");
                softly.assertThat(properties.getPrivateKeyBase64()).isEqualTo("test-key");
                softly.assertThat(properties.getWebRedirectUri())
                        .isEqualTo("https://api.checkmo.co.kr/login/oauth2/code/apple");
            });
        });
    }

    private ApplicationContextInitializer<ConfigurableApplicationContext> loadOAuth2Yaml() {
        return context -> {
            try {
                YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
                List<PropertySource<?>> propertySources = loader.load(
                        "application-oauth2",
                        new ClassPathResource("application-oauth2.yml")
                );
                propertySources.forEach(propertySource ->
                        context.getEnvironment().getPropertySources().addLast(propertySource));
            } catch (IOException e) {
                throw new IllegalStateException("application-oauth2.yml could not be loaded", e);
            }
        };
    }

    @Configuration
    @EnableConfigurationProperties(AppleOAuthProperties.class)
    static class AppleOAuthPropertiesConfiguration {
    }
}
