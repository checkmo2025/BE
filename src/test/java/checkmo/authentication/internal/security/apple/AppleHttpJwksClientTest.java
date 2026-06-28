package checkmo.authentication.internal.security.apple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import org.junit.jupiter.api.Test;

class AppleHttpJwksClientTest {

    @Test
    void sendsJwksRequestWithTimeout() {
        CapturingHttpClient httpClient = new CapturingHttpClient(
                TestHttpResponse.ok("{\"keys\":[]}")
        );
        AppleHttpJwksClient jwksClient = new AppleHttpJwksClient(httpClient, new ObjectMapper());

        AppleJwks jwks = jwksClient.fetch();

        assertSoftly(softly -> {
            softly.assertThat(jwks.keys()).isEmpty();
            softly.assertThat(httpClient.requests()).hasSize(1);
            softly.assertThat(httpClient.requests().getFirst().uri())
                    .isEqualTo(URI.create("https://appleid.apple.com/auth/keys"));
            softly.assertThat(httpClient.requests().getFirst().timeout())
                    .contains(Duration.ofSeconds(3));
        });
    }

    @Test
    void retriesOnceAfterJwksIoFailure() {
        CapturingHttpClient httpClient = new CapturingHttpClient(
                new IOException("temporary failure"),
                TestHttpResponse.ok("{\"keys\":[]}")
        );
        AppleHttpJwksClient jwksClient = new AppleHttpJwksClient(httpClient, new ObjectMapper());

        AppleJwks jwks = jwksClient.fetch();

        assertSoftly(softly -> {
            softly.assertThat(jwks.keys()).isEmpty();
            softly.assertThat(httpClient.requests()).hasSize(2);
        });
    }

    private static class CapturingHttpClient extends HttpClient {

        private final Queue<Object> responses = new ArrayDeque<>();
        private final List<HttpRequest> requests = new ArrayList<>();

        CapturingHttpClient(Object... responses) {
            this.responses.addAll(List.of(responses));
        }

        List<HttpRequest> requests() {
            return requests;
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.of(Duration.ofSeconds(3));
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, null, new SecureRandom());
                return context;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public SSLParameters sslParameters() {
            return new SSLParameters();
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_2;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> HttpResponse<T> send(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler
        ) throws IOException {
            requests.add(request);
            Object response = responses.remove();
            if (response instanceof IOException exception) {
                throw exception;
            }
            return (HttpResponse<T>) response;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler
        ) {
            throw new UnsupportedOperationException();
        }
    }

    private record TestHttpResponse<T>(int statusCode, T body) implements HttpResponse<T> {

        static TestHttpResponse<String> ok(String body) {
            return new TestHttpResponse<>(200, body);
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional<HttpResponse<T>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(Map.of(), (name, value) -> true);
        }

        @Override
        public URI uri() {
            return URI.create("https://appleid.apple.com/auth/keys");
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_2;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }
    }
}
