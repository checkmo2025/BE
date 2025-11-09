package checkmo.bookStory.internal.service.command;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.book.BookAPI;
import checkmo.bookStory.converter.BookStoryConverter;
import checkmo.bookStory.entity.BookStory;
import checkmo.bookStory.repository.BookStoryRepository;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.member.entity.Member;
import checkmo.member.MemberAPI;
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
    private final MemberAPI memberAPI;
    // Domain level 1
    private final BookAPI bookAPI;

    // 자신의 Repository
    private final BookStoryRepository bookStoryRepository;

    @Override
    @Transactional
    public Long createBookStory(String memberId, BookStoryRequestDTO.BookStoryCreateRequest request) {

        String bookId = bookAPI.getOrCreateBook(request.getBookInfo());

        Member proxyMember = memberAPI.findMemberReferenceById(memberId);

        BookStory bookStory = BookStoryConverter.fromBookStoryRequestDTO(request, proxyMember, bookId);
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
