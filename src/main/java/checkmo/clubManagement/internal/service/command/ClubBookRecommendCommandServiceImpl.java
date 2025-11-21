package checkmo.clubManagement.internal.service.command;

import checkmo.book.BookAPI;
import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.BookRecommend;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.repository.BookRecommendRepository;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubBookRecommendCommandServiceImpl implements ClubBookRecommendCommandService {

    // Domain level 1
    private final BookAPI bookAPI;

    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    private final BookRecommendRepository bookRecommendRepository;

    @Override
    @Transactional
    public Long recommendBook(Long clubId, String memberId, ClubRequestDTO.CreateBookRecommend request) {
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        String bookId = bookAPI.getOrCreateBook(request.getBookDetail());

        BookRecommend bookRecommend = ClubManagementConverter.toBookRecommend(request, bookId, clubMember);

        BookRecommend savedRecommend = bookRecommendRepository.save(bookRecommend);

        return savedRecommend.getId();
    }

    @Override
    @Transactional
    public Long updateBookRecommend(
            Long clubId,
            String memberId,
            Long bookRecommendId,
            ClubRequestDTO.UpdateBookRecommend request
    ) {
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 추천 책 조회 및 존재 여부 검증
        BookRecommend bookRecommend = bookRecommendRepository.findById(bookRecommendId)
                .orElseThrow(
                        () -> new ClubManagementException(ClubManagementErrorStatus.CLUB_BOOK_RECOMMEND_NOT_FOUND));
        if (!bookRecommend.getClubMember().equals(clubMember)) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_BOOK_RECOMMEND_FORBIDDEN);
        }

        bookRecommend.updateRecommendInfo(request.getTitle(), request.getContent(), request.getRate(),
                request.getTag());

        return bookRecommend.getId();
    }

    @Override
    public void deleteRecommendedBook(Long clubId, String memberId, Long bookRecommendId) {
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        BookRecommend bookRecommend = bookRecommendRepository.findById(bookRecommendId)
                .orElseThrow(
                        () -> new ClubManagementException(ClubManagementErrorStatus.CLUB_BOOK_RECOMMEND_NOT_FOUND));
        if (!bookRecommend.getClubMember().equals(clubMember)) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_BOOK_RECOMMEND_FORBIDDEN);
        }

        bookRecommendRepository.delete(bookRecommend);
    }

}