package checkmo.clubManagement.internal.service.command;

import checkmo.book.BookAPI;
import checkmo.book.internal.entity.Book;
import checkmo.book.internal.facade.BookCommandFacade;
import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.BookRecommend;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import checkmo.clubManagement.internal.repository.BookRecommendRepository;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubBookRecommendCommandServiceImpl implements ClubBookRecommendCommandService {

    // Domain level 1
    private final BookAPI bookAPI;
    private final BookCommandFacade bookCommandFacade;

    // 자신의 QueryService
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    // 자신의 Repository
    private final BookRecommendRepository bookRecommendRepository;

    @Override
    @Transactional
    public Long recommendBook(Long clubId, String memberId, ClubRequestDTO.CreateBookRecommendDTO request) {

        // 1. 검증
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 책 저장 후 프록시 가져오기
        bookCommandFacade.saveBook(request.getBookDetail());
        Book bookProxy = bookAPI.findBookReferenceById(request.getBookDetail().getIsbn());

        // 3. 책 추천 엔티티 생성
        BookRecommend bookRecommend = ClubManagementConverter.fromCreateBookRecommendDTOToEntity(request, bookProxy,
                clubMember);

        // 4. 저장
        BookRecommend savedRecommend = bookRecommendRepository.save(bookRecommend);

        // 5. 추천 ID 반환
        return savedRecommend.getId();
    }

    @Override
    @Transactional
    public Long updateBookRecommend(Long clubId, String memberId, Long bookRecommendId,
                                    ClubRequestDTO.UpdateBookRecommendDTO request) {

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
        bookRecommend.updateRecommendInfo(request.getTitle(), request.getContent(), request.getRate(),
                request.getTag());

        return bookRecommend.getId();
    }

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