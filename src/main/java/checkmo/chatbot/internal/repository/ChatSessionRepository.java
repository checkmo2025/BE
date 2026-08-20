package checkmo.chatbot.internal.repository;

import checkmo.chatbot.internal.entity.ChatSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findBySessionToken(String sessionToken);
}
