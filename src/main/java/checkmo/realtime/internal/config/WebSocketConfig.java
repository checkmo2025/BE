package checkmo.realtime.internal.config;

import checkmo.realtime.internal.config.properties.WebSocketProperties;
import checkmo.realtime.internal.interceptor.TeamAuthorizationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * 웹소켓 관련 설정(하트비트 스케줄러, stomp 엔드포인트, simple broker, 메시지 크기 제한 등)
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@EnableConfigurationProperties(WebSocketProperties.class)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketProperties webSocketProperties;
    private final TeamAuthorizationInterceptor teamAuthorizationInterceptor;
    private final CustomStompErrorHandler customStompErrorHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] allowedOrigins = webSocketProperties.allowedOrigins().toArray(String[]::new);
        // 클라이언트가 WebSocket 연결을 시도할 때 사용할 엔드포인트를 등록합니다.
        registry.setErrorHandler(customStompErrorHandler)
                .addEndpoint("/ws-stomp")
                .addInterceptors(httpSessionHandshakeInterceptor())
                .setAllowedOrigins(allowedOrigins);
        // TODO: SockJS 설정
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트 -> 서버로 들어오는 SEND 목적지 prefix
        registry.setApplicationDestinationPrefixes("/pub");
        // 서버 -> 클라이언트로 나가는 SUBSCRIBE 목적지 prefix (built-in simple broker 사용)
        registry.enableSimpleBroker("/sub", "/queue")
                // 하트비트 헤더
                // 서버 -> 클라이언트 : 20초마다, 클라이언트 -> 서버 : 25초마다
                .setTaskScheduler(heartbeatTaskScheduler())
                .setHeartbeatValue(new long[]{20000, 25000});
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(teamAuthorizationInterceptor);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // 메시지의 크기 제한과 타임아웃 설정
        registry.setMessageSizeLimit(64 * 1024); // 64KB
        registry.setSendBufferSizeLimit(512 * 1024); // 512KB
        registry.setTimeToFirstMessage(15_000); // 15초
    }

    @Bean
    public ThreadPoolTaskScheduler heartbeatTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-stomp-heartbeat-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // 애플리케이션 종료 시 스케줄러가 실행 중인 작업이 완료될 때까지 대기하도록 설정
        scheduler.setAwaitTerminationSeconds(10); // 최대 대기 시간 설정 (초 단위)
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    HttpSessionHandshakeInterceptor httpSessionHandshakeInterceptor() {
        return new HttpSessionHandshakeInterceptor();
    }
}
