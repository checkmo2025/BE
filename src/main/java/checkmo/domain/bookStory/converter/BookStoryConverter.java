package checkmo.domain.bookStory.converter;

import checkmo.domain.book.entity.Book;
import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.domain.bookStory.web.dto.BookStoryResponseDTO;
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
    // BookStoryRequestDTO → BookStory 변환
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
    public static BookStoryResponseDTO.BookStoryListResponse fromBookStoryResponses(
            List<BookStoryResponseDTO.BookStoryResponse> bookStoryResponses,
            boolean hasNext,
            Long nextCursor,
            int pageSize,
            BookStoryResponseDTO.ScopeInfo scopeInfo,
            ClubSharedDTO.MyClubListDTO myClubList
    ) {
        return BookStoryResponseDTO.BookStoryListResponse.builder()
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
    public static BookStoryResponseDTO.ScopeInfo fromScopeInfo(
            BookStoryRequestDTO.BookStoryScope scope,
            ClubSharedDTO.MyClubInfoDTO selectedClub
    ) {
        return BookStoryResponseDTO.ScopeInfo.builder()
                .scope(scope)
                .selectedClub(selectedClub)
                .build();
    }

    /**
     * BookStory -> BookStoryResponseDTO 변환
     */
    public static BookStoryResponseDTO.BookStoryResponse fromBookStoryToResponse(
            BookStory bookStory,
            String currentMemberId,
            BookSharedDTO.BasicInfoDTO bookInfo,
            MemberSharedDTO.WithFollowStatusDTO authorInfo,
            boolean isLiked
    ) {
        return BookStoryResponseDTO.BookStoryResponse.builder()
                .bookStoryId(bookStory.getId())
                .bookInfo(bookInfo)
                .authorInfo(authorInfo)
                .bookStoryTitle(bookStory.getTitle())
                .description(bookStory.getDescription())
                .likes(bookStory.getLikes())
                .isLiked(isLiked)
                .createdAt(bookStory.getCreatedAt())
                .isAuthor(bookStory.getMemberId().equals(currentMemberId))
                .build();
    }
}
