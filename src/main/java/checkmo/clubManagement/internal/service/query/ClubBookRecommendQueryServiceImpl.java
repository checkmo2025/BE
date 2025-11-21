package checkmo.clubManagement.internal.service.query;

import checkmo.clubManagement.internal.entity.BookRecommend;
import checkmo.clubManagement.internal.repository.BookRecommendRepository;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubBookRecommendQueryServiceImpl implements ClubBookRecommendQueryService {

    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    private final BookRecommendRepository bookRecommendRepository;

    @Override
    public BookRecommend getBookRecommend(Long clubId, Long bookRecommendId, String memberId) {
        clubQueryService.validateClub(clubId);
        clubMemberQueryService.validateClubMember(clubId, memberId);

        return bookRecommendRepository.findById(bookRecommendId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_BOOK_RECOMMEND_NOT_FOUND));
    }

    @Override
    public List<BookRecommend> getRecommendedBooks(Long clubId, Long cursorId, Integer size) {
        return bookRecommendRepository.getBookRecommendsAndClubMemberByClubIdAndCursor(clubId, cursorId, size);
    }

}