package checkmo.chatbot.internal.service;

import checkmo.chatbot.internal.config.properties.ChatbotProperties;
import checkmo.chatbot.internal.exception.ChatbotErrorStatus;
import checkmo.chatbot.internal.exception.ChatbotException;
import checkmo.chatbot.internal.service.dto.GeminiApiDTO.Candidate;
import checkmo.chatbot.internal.service.dto.GeminiApiDTO.Content;
import checkmo.chatbot.internal.service.dto.GeminiApiDTO.GenerateContentRequest;
import checkmo.chatbot.internal.service.dto.GeminiApiDTO.GenerateContentResponse;
import checkmo.chatbot.internal.service.dto.GeminiApiDTO.Part;
import checkmo.chatbot.internal.service.dto.GeminiApiDTO.SystemInstruction;
import checkmo.common.monitoring.SentryCaptureClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Gemini generateContent REST API 호출을 담당한다.
 * 시스템 프롬프트는 고정 블록으로 매 요청 systemInstruction에 그대로 실어 보내고,
 * 가변 정보(대화 이력, 이번 턴 질문)는 contents로 별도 전달한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiApiService {

    private final RestTemplate chatbotRestTemplate;
    private final ChatbotProperties chatbotProperties;
    private final SentryCaptureClient sentryCaptureClient;

    public String generateReply(String systemPromptText, List<Content> contents, String modelName) {
        String url = buildGenerateContentUrl(modelName);

        GenerateContentRequest request = GenerateContentRequest.builder()
                .systemInstruction(SystemInstruction.builder()
                        .parts(List.of(Part.builder().text(systemPromptText).build()))
                        .build())
                .contents(contents)
                .build();

        // Content-Type을 명시하지 않으면 RestTemplate이 classpath의 jackson-dataformat-xml(Aladin 연동용)을
        // 먼저 골라 요청 바디를 XML로 직렬화해버린다. application/json을 명시해 JSON 컨버터를 강제한다.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", chatbotProperties.getApiKey());
        HttpEntity<GenerateContentRequest> httpEntity = new HttpEntity<>(request, headers);

        GenerateContentResponse response;
        try {
            response = chatbotRestTemplate.postForObject(url, httpEntity, GenerateContentResponse.class);
        } catch (RestClientException e) {
            log.warn("Gemini API 호출 실패 (model={})", modelName, e);
            sentryCaptureClient.captureException(e);
            throw new ChatbotException(ChatbotErrorStatus.GEMINI_API_CALL_FAILED, e);
        }

        return extractText(response);
    }

    private String buildGenerateContentUrl(String modelName) {
        // 인증은 쿼리 파라미터가 아니라 x-goog-api-key 헤더로 전달한다(실제 호출로 검증된 방식).
        return UriComponentsBuilder
                .fromUriString(chatbotProperties.getBaseUrl())
                .pathSegment(chatbotProperties.getApiVersion(), "models", modelName + ":generateContent")
                .toUriString();
    }

    private String extractText(GenerateContentResponse response) {
        if (response == null || CollectionUtils.isEmpty(response.getCandidates())) {
            throw new ChatbotException(ChatbotErrorStatus.GEMINI_EMPTY_RESPONSE);
        }

        Candidate candidate = response.getCandidates().get(0);
        if (candidate.getContent() == null || CollectionUtils.isEmpty(candidate.getContent().getParts())) {
            throw new ChatbotException(ChatbotErrorStatus.GEMINI_EMPTY_RESPONSE);
        }

        return candidate.getContent().getParts().get(0).getText();
    }
}
