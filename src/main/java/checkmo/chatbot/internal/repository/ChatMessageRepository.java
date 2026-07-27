package checkmo.chatbot.internal.repository;

import checkmo.chatbot.internal.entity.ChatMessage;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 최근 메시지를 최신순으로 최대 {@code pageable}의 page size만큼 조회한다.
     * 대화 이력을 프롬프트에 무한정 실어 보내지 않기 위해 상한을 둔다.
     */
    List<ChatMessage> findByChatSessionIdOrderByCreatedAtDesc(Long chatSessionId, Pageable pageable);
}
