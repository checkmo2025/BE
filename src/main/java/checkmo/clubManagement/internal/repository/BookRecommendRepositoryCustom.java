package checkmo.clubManagement.internal.repository;

import checkmo.clubManagement.internal.entity.BookRecommend;
import java.util.List;

public interface BookRecommendRepositoryCustom {

    List<BookRecommend> getBookRecommendsAndClubMemberByClubIdAndCursor(Long clubId, Long cursorId, Integer size);
}
