package checkmo.domain.bookStory.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.book.entity.Book;
import checkmo.domain.book.facade.BookCommandFacade;
import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.bookStory.converter.BookStoryConverter;
import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.repository.BookStoryRepository;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.facade.MemberQueryFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryCommandServiceImpl implements BookStoryCommandService {

    // Domain level 2
    private final MemberQueryFacade memberQueryFacade;
    // Domain level 1
    private final BookCommandFacade bookCommandFacade;
    private final BookQueryFacade bookQueryFacade;

    // 자신의 Repository
    private final BookStoryRepository bookStoryRepository;

    @Override
    @Transactional
    public Long createBookStory(String memberId, BookStoryRequestDTO.BookStoryCreateRequest request) {

        bookCommandFacade.saveBook(request.getBookInfo());
        Book proxyBook = bookQueryFacade.findBookReferenceById(request.getBookInfo().getIsbn());

        Member proxyMember = memberQueryFacade.findMemberReferenceById(memberId);

        BookStory bookStory = BookStoryConverter.fromBookStoryRequestDTO(request, proxyMember, proxyBook);
        BookStory savedBookStory = bookStoryRepository.save(bookStory);

        return savedBookStory.getId();
    }

    @Override
    @Transactional
    public Long updateBookStory(String memberId, Long bookStoryId, BookStoryRequestDTO.BookStoryUpdateRequest request) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_STORY_NOT_FOUND));

        if (!bookStory.getMemberId().equals(memberId)) {
            throw new GeneralException(ErrorStatus.BOOK_STORY_NOT_AUTHORIZED);
        }

        return bookStory.updateDescription(request.getDescription());
    }

    @Override
    @Transactional
    public void deleteBookStory(String memberId, Long bookStoryId) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_STORY_NOT_FOUND));

        if (!bookStory.getMemberId().equals(memberId)) {
            throw new GeneralException(ErrorStatus.BOOK_STORY_NOT_AUTHORIZED);
        }

        bookStoryRepository.delete(bookStory);
    }
}
