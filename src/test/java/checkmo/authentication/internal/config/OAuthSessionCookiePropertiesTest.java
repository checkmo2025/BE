package checkmo.authentication.internal.config;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class OAuthSessionCookiePropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(loadProdYaml());

    @Test
    void configuresSecureCrossSiteSessionCookieForAppleFormPostCallback() {
        contextRunner.run(context -> assertSoftly(softly -> {
            softly.assertThat(context.getEnvironment().getProperty("server.servlet.session.cookie.same-site"))
                    .isEqualTo("none");
            softly.assertThat(context.getEnvironment().getProperty("server.servlet.session.cookie.secure"))
                    .isEqualTo("true");
        }));
    }

    private ApplicationContextInitializer<ConfigurableApplicationContext> loadProdYaml() {
        return context -> {
            try {
                YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
                List<PropertySource<?>> propertySources = loader.load(
                        "application-prod",
                        new ClassPathResource("application-prod.yml")
                );
                propertySources.forEach(propertySource ->
                        context.getEnvironment().getPropertySources().addLast(propertySource));
            } catch (IOException e) {
                throw new IllegalStateException("application-prod.yml could not be loaded", e);
            }
        };
    }
}
