package checkmo.chatbot.internal.service.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Gemini generateContent REST API 요청/응답 페이로드.
 * 필드명이 Gemini API의 camelCase JSON 키와 그대로 매칭되도록 맞췄다(별도 @JsonProperty 불필요).
 *
 * 주의: 이 스펙은 구현 시점 기준의 일반적인 Gemini generateContent 계약을 따른 것으로,
 * 실제 호출 전 최신 Gemini API 문서로 재확인이 필요하다(로컬 QA 항목).
 */
public class GeminiApiDTO {

    @Getter
    @Builder
    public static class GenerateContentRequest {
        private SystemInstruction systemInstruction;
        private List<Content> contents;
    }

    @Getter
    @Builder
    public static class SystemInstruction {
        private List<Part> parts;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String role; // "user" 또는 "model"
        private List<Part> parts;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
    }

    @Getter
    @NoArgsConstructor
    public static class GenerateContentResponse {
        private List<Candidate> candidates;
        private UsageMetadata usageMetadata;
    }

    @Getter
    @NoArgsConstructor
    public static class Candidate {
        private Content content;
        private String finishReason;
    }

    @Getter
    @NoArgsConstructor
    public static class UsageMetadata {
        private int promptTokenCount;
        private int candidatesTokenCount;
        private int totalTokenCount;
    }
}
