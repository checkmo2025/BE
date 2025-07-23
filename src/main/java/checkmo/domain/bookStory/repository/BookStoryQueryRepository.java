package checkmo.domain.bookStory.repository;

import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;

import java.util.List;

public interface BookStoryQueryRepository {
    List<BookStory> searchBookStories(String memberId, BookStoryRequestDTO.BookStoryScope scope, Long clubId, Long cursorId, int pageSize);
}
