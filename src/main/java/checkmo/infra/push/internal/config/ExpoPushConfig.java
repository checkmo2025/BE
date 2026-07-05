package checkmo.infra.push.internal.config;

import checkmo.infra.push.internal.config.properties.ExpoPushProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class ExpoPushConfig {

    private final ExpoPushProperties properties;

    @Bean
    public RestClient expoPushRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());

        RestClient.Builder builder = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Accept-Encoding", "gzip, deflate");

        if (StringUtils.hasText(properties.getAccessToken())) {
            builder.defaultHeader("Authorization", "Bearer " + properties.getAccessToken());
        }

        return builder.build();
    }
}
