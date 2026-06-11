package checkmo.book;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AladinConfigurationTest {

    @Test
    void aladinApiBaseUrlUsesHttps() throws IOException {
        String configuration = Files.readString(Path.of("src/main/resources/application-aladin.yml"));

        assertThat(configuration)
                .contains("base: https://www.aladin.co.kr/ttb/api")
                .doesNotContain("base: http://www.aladin.co.kr/ttb/api");
    }
}
