package checkmo.domain.bookStory.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.entity.BookStoryLiked;
import checkmo.domain.bookStory.repository.BookStoryLikedRepository;
import checkmo.domain.bookStory.repository.BookStoryRepository;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.event.LikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStorySocialCommandServiceImpl implements BookStorySocialCommandService {

    // Domain level 2
    private final MemberQueryFacade memberQueryFacade;

    // 자신의 Repository
    private final BookStoryRepository bookStoryRepository;
    private final BookStoryLikedRepository bookStoryLikedRepository;

    // 이벤트 발행을 위한 ApplicationEventPublisher
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public boolean toggleLikeOnBookStory(String memberId, Long bookStoryId) {

        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_STORY_NOT_FOUND));

        Member proxyMember = memberQueryFacade.findMemberReferenceById(memberId);

        return bookStoryLikedRepository.findBookStoryLikedByBookStoryIdAndMemberId(bookStoryId, memberId)
                .map(bookStoryLiked -> {
                    // 좋아요가 이미 있다면 제거하고 false 반환
                    deleteBookStoryLiked(bookStory, bookStoryLiked);
                    return false;
                })
                .orElseGet(() -> {
                    // 좋아요가 없다면 새로 생성하고 true 반환
                    boolean created = createAndSaveBookStoryLiked(bookStory, proxyMember);
                    if (created && !memberId.equals(bookStory.getMemberId())) {
                        // 좋아요가 생성되고, 좋아요를 누른 사람이 책이야기를 작성한 사람과 다를 때만 이벤트 발행
                        eventPublisher.publishEvent(new LikeEvent(memberId, bookStory.getMemberId(), bookStory.getId()));
                    }
                    return created;
                });
    }

    private void deleteBookStoryLiked(BookStory bookStory, BookStoryLiked bookStoryLiked) {
        bookStoryLikedRepository.delete(bookStoryLiked);
        bookStory.removeBookStoryLiked(bookStoryLiked);
    }

    private boolean createAndSaveBookStoryLiked(BookStory bookStory, Member member) {
        try {
            BookStoryLiked bookStoryLiked = BookStoryLiked.builder()
                    .bookStory(bookStory)
                    .member(member)
                    .build();

            bookStoryLikedRepository.save(bookStoryLiked);
            bookStory.addBookStoryLiked(bookStoryLiked);
            return true; // 정상적으로 생성됨
        } catch (DataIntegrityViolationException e) {
            // 이미 존재하는 경우는 생성 X
            return false; // 중복으로 인해 생성 안됨
        }
    }
}
