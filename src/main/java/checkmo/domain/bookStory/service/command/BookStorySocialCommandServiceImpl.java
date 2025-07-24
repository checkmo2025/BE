package checkmo.domain.bookStory.service.command;

import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.entity.BookStoryLiked;
import checkmo.domain.bookStory.repository.BookStoryLikedRepository;
import checkmo.domain.bookStory.repository.BookStoryRepository;
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
public class BookStorySocialCommandServiceImpl implements BookStorySocialCommandService {

    private final BookStoryRepository bookStoryRepository;
    private final BookStoryLikedRepository bookStoryLikedRepository;
    private final MemberQueryFacade memberQueryFacade;

    @Override
    @Transactional
    public boolean toggleLikeOnBookStory(String memberId, Long bookStoryId) {

        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new IllegalArgumentException(bookStoryId + "에 해당하는 책이야기를 찾을 수 없습니다"));

        Member proxyMember = memberQueryFacade.findMemberReferenceById(memberId);

        return bookStoryLikedRepository.findBookStoryLikedByBookStoryIdAndMemberId(bookStoryId, memberId)
                .map(bookStoryLiked -> {
                    // 좋아요가 이미 있다면 제거하고 false 반환
                    deleteBookStoryLiked(bookStory, bookStoryLiked);
                    return false;
                })
                .orElseGet(() -> {
                    // 좋아요가 없다면 새로 생성하고 true 반환
                    createAndSaveBookStoryLiked(bookStory, proxyMember);
                    return true;
                });
    }

    private void deleteBookStoryLiked(BookStory bookStory, BookStoryLiked bookStoryLiked) {
        bookStoryLikedRepository.delete(bookStoryLiked);
        bookStory.removeLike();
    }

    private void createAndSaveBookStoryLiked(BookStory bookStory, Member member) {
        bookStoryLikedRepository.save(
                BookStoryLiked.builder()
                        .bookStory(bookStory)
                        .member(member)
                        .build()
        );
        bookStory.addLike();
    }
}
