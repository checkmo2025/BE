package checkmo.clubNotice.internal.repository;

import checkmo.clubNotice.internal.entity.Vote;
import java.util.List;

public interface VoteRepositoryCustom {
    List<Vote> findByClubIdAndCursorPaging(Long clubId, boolean onlyImportant, Long cursorId, Integer size);
}
