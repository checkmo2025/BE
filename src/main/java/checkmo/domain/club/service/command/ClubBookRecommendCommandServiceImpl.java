package checkmo.domain.club.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.book.entity.Book;
import checkmo.domain.book.facade.BookCommandFacade;
import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.club.entity.BookRecommend;
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

    /**
     * 독서모임에 추천 책을 수정합니다.
     *
     * @param clubId 독서모임 ID
     * @param memberId 추천하는 회원 ID -> 클럽 회원인지 확인하는 로직 필요
     * @param bookRecommendId 수정할 추천 책의 ID
     * @param request 수정할 추천책의 정보 DTO
     * @return 수정한 추천 책의 ID
     */
    @Override
    @Transactional
    public Long updateBookRecommend(Long clubId, String memberId, Long bookRecommendId, ClubRequestDTO.UpdateBookRecommendDTO request) {

        // 1. 클럽 및 클럽 멤버 유효성 검증
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 추천 책 조회 및 존재 여부 검증
        BookRecommend bookRecommend = bookRecommendRepository.findById(bookRecommendId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_BOOK_RECOMMEND_NOT_FOUND));

        // 3. 작성자 권한 확인
        if (!bookRecommend.getClubMember().equals(clubMember)) {
            throw new GeneralException(ErrorStatus.CLUB_BOOK_RECOMMEND_FORBIDDEN);
        }

        // 4. 값 수정 (책 자체는 변경 불가)
        bookRecommend.updateRecommendInfo(request.getTitle(), request.getContent(), request.getRate(), request.getTag());

        return bookRecommend.getId();
    }

    /**
     * 독서모임에서 추천한 책을 삭제합니다.
     *
     * @param clubId 독서모임 ID
     * @param memberId 삭제하는 회원 ID -> 클럽 회원인지 확인하는 로직 필요
     * @param bookRecommendId 삭제할 추천 책의 ID
     */
    @Override
    public void deleteRecommendedBook(Long clubId, String memberId, Long bookRecommendId) {

        // 1. 클럽 및 클럽 멤버 유효성 검증
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 추천 책 조회 및 존재 여부 검증
        BookRecommend bookRecommend = bookRecommendRepository.findById(bookRecommendId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_BOOK_RECOMMEND_NOT_FOUND));

        // 3. 작성자 권한 확인
        if (!bookRecommend.getClubMember().equals(clubMember)) {
            throw new GeneralException(ErrorStatus.CLUB_BOOK_RECOMMEND_FORBIDDEN);
        }

        // 4. 삭제
        bookRecommendRepository.delete(bookRecommend);
    }

}