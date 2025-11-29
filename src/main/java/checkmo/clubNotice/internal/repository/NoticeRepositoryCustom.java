package checkmo.clubNotice.internal.repository;

import checkmo.clubNotice.internal.entity.Notice;
import java.util.List;

public interface NoticeRepositoryCustom {
    List<Notice> findAllByClubIdAndCursorPaging(Long clubId, boolean onlyImportant, Long cursorId, Integer size);
}
