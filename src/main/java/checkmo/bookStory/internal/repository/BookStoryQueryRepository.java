package checkmo.bookStory.internal.repository;

import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.common.sitemap.SitemapResponseDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookStoryQueryRepository {
    List<BookStory> searchBookStories(
            String memberId,
            List<String> excludedMemberIds,
            List<String> followingMemberIds,
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId,
            String targetMemberId,
            Long cursorId,
            int pageSize
    );

    List<BookStory> searchBookStories(
            String bookId,
            List<String> excludedMemberIds,
            Long cursorId,
            int pageSize
    );

    Page<BookStory> searchBookStoriesForAdmin(String keyword, Pageable pageable);

    List<SitemapResponseDTO.Item> findPublishedSitemapItems(Long cursorId, int pageSize);
}
