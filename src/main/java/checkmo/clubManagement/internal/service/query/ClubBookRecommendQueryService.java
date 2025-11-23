package checkmo.clubManagement.internal.service.query;

import checkmo.clubManagement.internal.entity.BookRecommend;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.repository.BookRecommendRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubBookRecommendQueryService {

    private final ClubManagementQueryService clubManagementQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    private final BookRecommendRepository bookRecommendRepository;

    public BookRecommend retrieveBookRecommend(Long clubId, Long bookRecommendId, String memberId) {
        clubManagementQueryService.validateClub(clubId);
        clubMemberQueryService.validateClubMember(clubId, memberId);

        return bookRecommendRepository.findById(bookRecommendId)
                .orElseThrow(
                        () -> new ClubManagementException(ClubManagementErrorStatus.CLUB_BOOK_RECOMMEND_NOT_FOUND));
    }

    public List<BookRecommend> retrieveBookRecommends(Long clubId, Long cursorId, Integer size) {
        return bookRecommendRepository.getBookRecommendsAndClubMemberByClubIdAndCursor(clubId, cursorId, size);
    }
}
