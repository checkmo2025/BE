package checkmo.bookStory.internal.scheduler;

import checkmo.bookStory.internal.repository.BookStoryRepository;
import checkmo.bookStory.internal.service.query.BookStoryViewCacheService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookStoryViewScheduler {

    private final BookStoryViewCacheService viewCacheService;
    private final BookStoryRepository bookStoryRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void syncViewCounts() {
        Set<String> keys = viewCacheService.getViewCountKeys();

        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            Long bookStoryId = viewCacheService.parseBookStoryId(key);
            int viewCount = viewCacheService.getViewCountAndDelete(key);

            if (viewCount > 0) {
                bookStoryRepository.incrementViewCount(bookStoryId, viewCount);
            }
        }
    }
}
