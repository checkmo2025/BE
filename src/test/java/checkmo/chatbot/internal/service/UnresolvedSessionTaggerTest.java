package checkmo.chatbot.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UnresolvedSessionTaggerTest {

    private final UnresolvedSessionTagger unresolvedSessionTagger = new UnresolvedSessionTagger();

    @Test
    void detectsBotHedgePhrase() {
        String reply = "현재 확인된 사용법 기준으로는 해당 기능은 준비 중이에요.";

        assertThat(unresolvedSessionTagger.isBotUncertain(reply)).isTrue();
    }

    @Test
    void doesNotFlagConfidentReply() {
        String reply = "책 이야기는 하단 탭의 책 이야기에서 작성할 수 있어요.";

        assertThat(unresolvedSessionTagger.isBotUncertain(reply)).isFalse();
    }

    @Test
    void detectsUserNegativeReactionKeyword() {
        assertThat(unresolvedSessionTagger.isUserNegativeReaction("말씀하신 대로 했는데 안 돼요")).isTrue();
    }

    @Test
    void doesNotFlagOrdinaryQuestionAsNegativeReaction() {
        assertThat(unresolvedSessionTagger.isUserNegativeReaction("모임 가입은 어떻게 해요?")).isFalse();
    }

    @Test
    void handlesBlankInput() {
        assertThat(unresolvedSessionTagger.isBotUncertain("")).isFalse();
        assertThat(unresolvedSessionTagger.isBotUncertain(null)).isFalse();
        assertThat(unresolvedSessionTagger.isUserNegativeReaction("")).isFalse();
        assertThat(unresolvedSessionTagger.isUserNegativeReaction(null)).isFalse();
    }
}
