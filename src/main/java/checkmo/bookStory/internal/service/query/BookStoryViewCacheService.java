package checkmo.bookStory.internal.service.query;

import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookStoryViewCacheService {

    private final StringRedisTemplate redisTemplate;

    // 조회수 저장 (bookStory:viewCount:{bookStoryId})
    private static final String VIEW_COUNT_KEY = "bookStory:viewCount:";
    // 중복 조회를 막기 위한 유저 저장 (bookStory:viewLog:{bookStoryId}:{memberId})
    private static final String VIEW_USER_LOG_KEY = "bookStory:viewLog:";

    /**
     * 조회수 증가 로직
     * @param bookStoryId 게시글 ID
     * @param memberId 조회한 유저 ID
     */
    @Async
    public void incrementViewCount(Long bookStoryId, String memberId) {
        String userLogKey = VIEW_USER_LOG_KEY + bookStoryId + ":" + memberId;
        String viewCountKey = VIEW_COUNT_KEY + bookStoryId;

        Boolean alreadyViewed = redisTemplate.hasKey(userLogKey);

        if (!alreadyViewed) {
            redisTemplate.opsForValue().increment(viewCountKey);

            redisTemplate.opsForValue().set(userLogKey, "1", Duration.ofMinutes(10));
        }
    }

    // TODO : SCAN 명령어로 구현하는게 더 좋지만 지금은 키 개수가 많지 않을 거라서 keys로 구현
    public Set<String> getViewCountKeys() {
        return redisTemplate.keys(VIEW_COUNT_KEY + "*");
    }

    public Long parseBookStoryId(String key) {
        return Long.parseLong(key.replace(VIEW_COUNT_KEY, ""));
    }

    public int getViewCountAndDelete(String key) {
        String value = redisTemplate.opsForValue().getAndDelete(key);

        if (value == null) {
            return 0;
        }

        return Integer.parseInt(value);
    }
}
