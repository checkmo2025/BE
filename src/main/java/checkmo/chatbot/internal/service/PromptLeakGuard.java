package checkmo.chatbot.internal.service;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 모델 응답에 시스템 프롬프트 원문(구조/지침)이 그대로 노출됐는지 검사한다.
 *
 * 시스템 프롬프트의 "내용"(로그인이 필요합니다, 하단 탭에서... 등)은 정상 답변과 자연스럽게
 * 겹치므로 검사 대상으로 쓸 수 없다. 대신 정상 답변에는 절대 등장하지 않는 프롬프트 "구조"
 * 표지(섹션 제목 등)만 표지로 사용해 오탐을 최소화한다.
 */
@Component
public class PromptLeakGuard {

    public static final String SAFE_FALLBACK_REPLY =
            "죄송해요, 다시 질문해 주시겠어요? 책모 이용과 관련해 궁금한 점을 말씀해주세요.";

    private static final List<String> LEAK_MARKERS = List.of(
            "답변 원칙:",
            "책모의 주요 화면 구조:",
            "로그인 없이 가능한 주요 기능:",
            "로그인이 필요한 주요 기능:",
            "모임 권한 기준:",
            "자주 묻는 질문에 대한 응답 기준:",
            "답변 예시 형식:",
            "보안 지침 (매우 중요, 예외 없이 지킵니다)"
    );

    public boolean isLeaked(String replyText) {
        if (!StringUtils.hasText(replyText)) {
            return false;
        }

        return LEAK_MARKERS.stream().anyMatch(replyText::contains);
    }
}
