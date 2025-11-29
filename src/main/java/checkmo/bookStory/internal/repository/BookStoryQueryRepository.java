package checkmo.bookStory.internal.repository;

import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import java.util.List;

public interface BookStoryQueryRepository {
    List<BookStory> searchBookStories(
            String memberId,
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId,
            String targetMemberId,
            Long cursorId,
            int pageSize
    );
}
