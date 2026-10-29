package checkmo.book.internal.config;

import checkmo.book.internal.config.properties.Yes24Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final Yes24Properties yes24Properties;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(yes24Properties.getSearch().getTimeoutMs());
        factory.setReadTimeout(yes24Properties.getSearch().getTimeoutMs());

        return new RestTemplate(factory);
    }
}
