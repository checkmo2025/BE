package checkmo.chatbot.internal.config;

import checkmo.chatbot.internal.config.properties.ChatbotProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class ChatbotRestTemplateConfig {

    private final ChatbotProperties chatbotProperties;

    /**
     * book 모듈의 기본 {@code restTemplate} 빈(Aladin 전용 타임아웃/XML 컨버터 설정)과 분리된
     * 챗봇(Gemini) 전용 RestTemplate. 빈 이름을 파라미터/필드명과 일치시켜 이름 기준으로 주입되도록 한다.
     *
     * 주의: classpath에 jackson-dataformat-xml(Aladin 연동용)이 있어 RestTemplate 기본 컨버터 목록에
     * XML 컨버터가 JSON 컨버터보다 먼저 등록된다. 호출부(GeminiApiService)에서 Content-Type을
     * application/json으로 명시하지 않으면 요청 바디가 XML로 직렬화되어 버리니 반드시 명시할 것.
     */
    @Bean
    public RestTemplate chatbotRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(chatbotProperties.getTimeoutMs());
        factory.setReadTimeout(chatbotProperties.getTimeoutMs());

        return new RestTemplate(factory);
    }
}
