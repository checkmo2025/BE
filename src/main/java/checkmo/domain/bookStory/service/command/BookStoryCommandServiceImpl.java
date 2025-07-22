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
    public Long updateBookStory(String memberId, BookStoryRequestDTO.BookStoryUpdateRequestDTO request) {
        return 0L;
    }

    @Override
    @Transactional
    public Long deleteBookStory(String memberId, Long bookStoryId) {
        return 0L;
    }
}
