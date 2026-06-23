package checkmo.bookStory.internal.repository;

import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.repository.projection.BookStorySitemapProjection;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
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

    List<BookStorySitemapProjection> findPublishedSitemapItems(Long cursorId, int pageSize);
}
