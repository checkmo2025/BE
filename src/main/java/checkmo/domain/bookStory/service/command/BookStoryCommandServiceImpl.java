package checkmo.domain.bookStory.service.command;

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

    private final BookStoryRepository bookStoryRepository;
    private final MemberQueryFacade memberQueryFacade;
    private final BookCommandFacade bookCommandFacade;
    private final BookQueryFacade bookQueryFacade;

    @Override
    @Transactional
    public Long createBookStory(String memberId, BookStoryRequestDTO.BookStoryCreateRequestDTO request) {

        bookCommandFacade.saveBook(request.getBookInfo());
        Book proxyBook = bookQueryFacade.findBookReferenceById(request.getBookInfo().getIsbn());

        Member proxyMember = memberQueryFacade.findMemberReferenceById(memberId);

        BookStory bookStory = BookStoryConverter.fromBookStoryRequestDTO(request, proxyMember, proxyBook);
        BookStory savedBookStory = bookStoryRepository.save(bookStory);

        return savedBookStory.getId();
    }

    @Override
    @Transactional
    public Long updateBookStory(String memberId, Long bookStoryId, BookStoryRequestDTO.BookStoryUpdateRequestDTO request) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 책이야기가 존재하지 않습니다."));

        if (!bookStory.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("해당 책이야기를 수정할 권한이 없습니다.");
        }

        return bookStory.updateDescription(request.getDescription());
    }

    @Override
    @Transactional
    public void deleteBookStory(String memberId, Long bookStoryId) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 책이야기가 존재하지 않습니다."));

        if (!bookStory.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("해당 책이야기를 삭제할 권한이 없습니다.");
        }

        bookStoryRepository.delete(bookStory);
    }
}
