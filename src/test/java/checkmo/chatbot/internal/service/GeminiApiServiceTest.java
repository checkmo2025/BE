package checkmo.chatbot.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import checkmo.chatbot.internal.config.properties.ChatbotProperties;
import checkmo.chatbot.internal.exception.ChatbotException;
import checkmo.chatbot.internal.service.dto.GeminiApiDTO.Content;
import checkmo.chatbot.internal.service.dto.GeminiApiDTO.GenerateContentResponse;
import checkmo.chatbot.internal.service.dto.GeminiApiDTO.Part;
import checkmo.common.monitoring.SentryCaptureClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class GeminiApiServiceTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final SentryCaptureClient sentryCaptureClient = mock(SentryCaptureClient.class);
    private final GeminiApiService geminiApiService =
            new GeminiApiService(restTemplate, chatbotProperties(), sentryCaptureClient);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void returnsFirstCandidateText() throws Exception {
        GenerateContentResponse response = objectMapper.readValue(
                """
                {
                  "candidates": [
                    {
                      "content": { "role": "model", "parts": [{ "text": "책 이야기는 하단 탭에서 작성할 수 있어요." }] },
                      "finishReason": "STOP"
                    }
                  ],
                  "usageMetadata": { "promptTokenCount": 10, "candidatesTokenCount": 5, "totalTokenCount": 15 }
                }
                """,
                GenerateContentResponse.class
        );
        when(restTemplate.postForObject(anyString(), any(), eq(GenerateContentResponse.class)))
                .thenReturn(response);

        String reply = geminiApiService.generateReply(
                "system prompt",
                List.of(userContent("책이야기 쓰려면?")),
                "gemini-3.1-flash-lite"
        );

        assertThat(reply).isEqualTo("책 이야기는 하단 탭에서 작성할 수 있어요.");
    }

    @Test
    void throwsChatbotExceptionWhenRestTemplateFails() {
        when(restTemplate.postForObject(anyString(), any(), eq(GenerateContentResponse.class)))
                .thenThrow(new RestClientException("connection reset"));

        assertThatThrownBy(() -> geminiApiService.generateReply(
                "system prompt",
                List.of(userContent("질문")),
                "gemini-3.1-flash-lite"
        )).isInstanceOf(ChatbotException.class);
    }

    @Test
    void throwsChatbotExceptionWhenResponseHasNoCandidates() throws Exception {
        GenerateContentResponse response = objectMapper.readValue(
                "{\"candidates\": []}", GenerateContentResponse.class);
        when(restTemplate.postForObject(anyString(), any(), eq(GenerateContentResponse.class)))
                .thenReturn(response);

        assertThatThrownBy(() -> geminiApiService.generateReply(
                "system prompt",
                List.of(userContent("질문")),
                "gemini-3.1-flash-lite"
        )).isInstanceOf(ChatbotException.class);
    }

    private Content userContent(String text) {
        return Content.builder()
                .role("user")
                .parts(List.of(Part.builder().text(text).build()))
                .build();
    }

    private ChatbotProperties chatbotProperties() {
        ChatbotProperties properties = new ChatbotProperties();
        properties.setApiKey("test-key");
        properties.setBaseUrl("http://localhost");
        properties.setApiVersion("v1beta");
        properties.setTimeoutMs(1000);

        ChatbotProperties.Model defaultModel = new ChatbotProperties.Model();
        defaultModel.setName("gemini-3.1-flash-lite");
        properties.setDefaultModel(defaultModel);

        ChatbotProperties.Model escalationModel = new ChatbotProperties.Model();
        escalationModel.setName("gemini-3.6-flash");
        properties.setEscalationModel(escalationModel);

        ChatbotProperties.Handoff handoff = new ChatbotProperties.Handoff();
        handoff.setSupportUrl("http://localhost/support");
        handoff.setInquiryFormUrl("http://localhost/inquiry");
        properties.setHandoff(handoff);

        return properties;
    }
}
