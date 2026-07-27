package checkmo.chatbot.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EscalationDeciderTest {

    private final EscalationDecider escalationDecider = new EscalationDecider();

    @Test
    void escalatesWhenUserNegativeReactionIsTrue() {
        assertThat(escalationDecider.shouldEscalate("아무 텍스트", true)).isTrue();
    }

    @Test
    void escalatesWhenErrorKeywordPresent() {
        assertThat(escalationDecider.shouldEscalate("로그인이 계속 오류가 나요", false)).isTrue();
    }

    @Test
    void doesNotEscalateForPlainNavigationQuestion() {
        assertThat(escalationDecider.shouldEscalate("책이야기는 어디서 써요?", false)).isFalse();
    }

    @Test
    void doesNotEscalateForBlankInput() {
        assertThat(escalationDecider.shouldEscalate("", false)).isFalse();
        assertThat(escalationDecider.shouldEscalate(null, false)).isFalse();
    }
}
