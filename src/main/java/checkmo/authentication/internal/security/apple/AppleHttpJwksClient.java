package checkmo.authentication.internal.security.apple;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppleHttpJwksClient implements AppleJwksClient {

    private static final URI APPLE_JWKS_URI = URI.create("https://appleid.apple.com/auth/keys");
    private static final Duration TIMEOUT = Duration.ofSeconds(3);
    private static final int MAX_ATTEMPTS = 2;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public AppleHttpJwksClient(ObjectMapper objectMapper) {
        this(HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build(), objectMapper);
    }

    AppleHttpJwksClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public AppleJwks fetch() {
        HttpRequest request = HttpRequest.newBuilder(APPLE_JWKS_URI)
                .timeout(TIMEOUT)
                .GET()
                .build();
        AppleJwksFetchException lastFailure = new AppleJwksFetchException("Apple JWKS could not be fetched");

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
                );
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return objectMapper.readValue(response.body(), AppleJwks.class);
                }
                lastFailure = new AppleJwksFetchException("Apple JWKS response was not successful");
            } catch (IOException e) {
                lastFailure = new AppleJwksFetchException("Apple JWKS could not be fetched", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AppleJwksFetchException("Apple JWKS fetch was interrupted", e);
            }
        }

        throw lastFailure;
    }
}
