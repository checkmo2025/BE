package checkmo.domain.bookStory.converter;

import checkmo.domain.book.entity.Book;
import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.global.dto.BookStorySharedDTO;
import checkmo.domain.member.entity.Member;
import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.ClubSharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookStoryConverter {

    // =====================================================
    // BookStoryRequestDTO → BookStory 변환, 테스트용
    // =====================================================

    /**
     * BookStoryCreateRequestDTO → BookStory 변환
     */
    public static BookStory fromBookStoryRequestDTO(BookStoryRequestDTO.BookStoryCreateRequestDTO request, Member proxyMember, Book proxyBook) {
        return BookStory.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .member(proxyMember)
                .book(proxyBook)
                .build();
    }

    // =====================================================
    // BookStory → BookStoryResponseDTO 변환
    // =====================================================

    /**
     * BookStoryResponseDTO -> BookStoryListResponse 변환
     */
    public static BookStorySharedDTO.BookStoryListResponse fromBookStoryResponses(
            List<BookStorySharedDTO.BookStoryResponse> bookStoryResponses,
            boolean hasNext,
            Long nextCursor,
            int pageSize,
            BookStorySharedDTO.ScopeInfo scopeInfo,
            ClubSharedDTO.MyClubListDTO myClubList
    ) {
        return BookStorySharedDTO.BookStoryListResponse.builder()
                .scopeInfo(scopeInfo)
                .memberClubList(myClubList)
                .bookStoryResponses(bookStoryResponses)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(pageSize)
                .build();
    }

    /**
     * BookStoryScope + MyClubInfoDTO -> ScopeInfo 변환
     */
    public static BookStorySharedDTO.ScopeInfo fromScopeInfo(
            BookStoryRequestDTO.BookStoryScope scope,
            ClubSharedDTO.MyClubInfoDTO selectedClub
    ) {
        return BookStorySharedDTO.ScopeInfo.builder()
                .scope(scope)
                .selectedClub(selectedClub)
                .build();
    }

    /**
     * BookStory -> BookStoryResponseDTO 변환
     */
    public static BookStorySharedDTO.BookStoryResponse fromBookStoryToResponse(
            BookStory bookStory,
            String currentMemberId,
            BookSharedDTO.BasicInfoDTO bookInfo,
            MemberSharedDTO.WithFollowStatusDTO authorInfo,
            boolean isLiked
    ) {
        return BookStorySharedDTO.BookStoryResponse.builder()
                .bookStoryId(bookStory.getId())
                .bookInfo(bookInfo)
                .authorInfo(authorInfo)
                .bookStoryTitle(bookStory.getTitle())
                .description(bookStory.getDescription())
                .likes(bookStory.getLikes())
                .likedByMe(isLiked)
                .createdAt(bookStory.getCreatedAt())
                .writtenByMe(bookStory.getMemberId().equals(currentMemberId))
                .build();
    }
}
