package checkmo.domain.club.service.command;

import checkmo.domain.book.entity.Book;
import checkmo.domain.book.facade.BookCommandFacade;
import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.club.entity.BookRecommend;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.BookRecommendRepository;
import checkmo.domain.club.service.query.ClubMemberQueryService;
import checkmo.domain.club.service.query.ClubQueryService;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.converter.ClubConverter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubBookRecommendCommandServiceImpl implements ClubBookRecommendCommandService {

    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;
    private final BookQueryFacade bookQueryFacade;
    private final BookCommandFacade bookCommandFacade;
    private final BookRecommendRepository bookRecommendRepository;

    /**
     * 독서모임에 책을 추천합니다.
     *
     * @param clubId 독서모임 ID
     * @param memberId 추천하는 회원 ID -> 클럽 회원인지 확인하는 로직 필요
     * @param request 추천할 책 정보 DTO
     * @return 추천한 책의 ID
     */
    @Override
    @Transactional
    public Long recommendBook(Long clubId, String memberId, ClubRequestDTO.CreateBookRecommendDTO request) {

        // 1. 검증
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 책 저장 후 프록시 가져오기
        bookCommandFacade.saveBook(request.getBookDetail());
        Book bookProxy = bookQueryFacade.findBookReferenceById(request.getBookDetail().getIsbn());

        // 3. 책 추천 엔티티 생성
        BookRecommend bookRecommend = ClubConverter.fromCreateBookRecommendDTOToEntity(request, bookProxy, clubMember);

        // 4. 저장
        BookRecommend savedRecommend = bookRecommendRepository.save(bookRecommend);

        // 5. 추천 ID 반환
        return savedRecommend.getId();
    }

    @Override
    public Long updateBookRecommend(Long clubId, String memberId, Long bookRecommendId, ClubRequestDTO.UpdateBookRecommendDTO request) {
        return null;
    }

    @Override
    public void deleteRecommendedBook(Long clubId, String memberId, Long bookRecommendId) {
    }
}
