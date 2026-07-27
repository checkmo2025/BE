package checkmo.chatbot.internal.service;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 규칙 기반으로 기본 모델 대신 에스컬레이션(고급) 모델을 써야 하는지 판단한다.
 * 추가 LLM 호출 없이 키워드 매칭만으로 판단해 비용을 늘리지 않는다.
 */
@Component
public class EscalationDecider {

    private static final List<String> ESCALATION_KEYWORDS = List.of(
            "오류", "에러", "버그", "실패", "안돼요", "안 돼요", "안됐어요", "안 됐어요",
            "안열려요", "안 열려요", "튕겨요", "먹통", "안됨", "안 됨"
    );

    public boolean shouldEscalate(String userMessageText, boolean userNegativeReaction) {
        if (userNegativeReaction) {
            return true;
        }
        if (!StringUtils.hasText(userMessageText)) {
            return false;
        }

        return ESCALATION_KEYWORDS.stream().anyMatch(userMessageText::contains);
    }
}
