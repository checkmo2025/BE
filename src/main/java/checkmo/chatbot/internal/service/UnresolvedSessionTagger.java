package checkmo.chatbot.internal.service;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 규칙 기반으로 "미해결" 신호를 판별한다. 추가 LLM 호출 없이 정규식/키워드 매칭만 사용한다.
 */
@Component
public class UnresolvedSessionTagger {

    private static final String BOT_HEDGE_PHRASE = "현재 확인된 사용법 기준으로는";

    private static final List<String> USER_NEGATIVE_REACTION_KEYWORDS = List.of(
            "안돼요", "안 돼요", "안됐어요", "안 됐어요", "안되는데", "안 되는데",
            "안됨", "안 됨", "여전히 안", "그래도 안", "아직도 안", "실패했어요", "안 열려요", "안열려요"
    );

    /**
     * 봇 응답에 헤지 문구가 포함되어 있으면 봇이 확신하지 못한 답변으로 간주한다.
     */
    public boolean isBotUncertain(String botReplyText) {
        if (!StringUtils.hasText(botReplyText)) {
            return false;
        }

        return botReplyText.contains(BOT_HEDGE_PHRASE);
    }

    /**
     * 사용자 발화가 부정 반응 키워드와 매칭되면 이전 안내가 해결되지 않은 것으로 간주한다.
     */
    public boolean isUserNegativeReaction(String userMessageText) {
        if (!StringUtils.hasText(userMessageText)) {
            return false;
        }

        return USER_NEGATIVE_REACTION_KEYWORDS.stream().anyMatch(userMessageText::contains);
    }
}
