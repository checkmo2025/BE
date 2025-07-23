package checkmo.domain.bookStory.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.bookStory.facade.BookStoryCommandFacade;
import checkmo.domain.bookStory.facade.BookStoryQueryFacade;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.domain.bookStory.web.dto.BookStoryResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book-stories")
@RequiredArgsConstructor
@Tag(name = "책 이야기", description = "책 이야기 업로드, 조회, 좋아요, 수정, 삭제 관련 API")
public class BookStoryController {

    private final BookStoryCommandFacade bookStoryCommandFacade;
    private final BookStoryQueryFacade bookStoryQueryFacade;

    @Operation(summary = "책 이야기 업로드 API", description = "새로운 책 이야기를 작성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다.")
    })
    @PostMapping
    public ApiResponse<Long> createBookStory(
            @RequestParam String memberId, // TODO: 스프링 시큐리티 구현 후 제거 예정
            @Valid @RequestBody BookStoryRequestDTO.BookStoryCreateRequestDTO request
    ) {
        Long bookStoryId = bookStoryCommandFacade.createBookStory(memberId, request);
        return ApiResponse.onSuccess(bookStoryId);
    }

    @Operation(summary = "책 이야기 전체보기 API", description = "조건에 따라 책 이야기 목록을 조회합니다.")
    @Parameters({
            @Parameter(
                    name = "scope",
                    description = "조회 범위:\n" +
                            "• ALL: 전체 책이야기\n" +
                            "• FOLLOWING: 팔로우한 회원의 책이야기\n" +
                            "• MY: 내 책이야기\n" +
                            "• CLUB: 특정 클럽 책이야기 (clubId 필수)",
                    required = true,
                    example = "ALL"
            ),
            @Parameter(name = "clubId", description = "조회하는 Club ID (scope가 CLUB일 때 필수)", required = false, example = "1"),
            @Parameter(name = "cursorId", description = "커서 ID (페이징을 위한 커서, 처음에는 null)", required = false, example = "10")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다.")
    })
    @GetMapping
    public ApiResponse<BookStoryResponseDTO.BookStoryListResponse> getBookStories(
            @RequestParam String memberId, // TODO: 스프링 시큐리티 구현 후 제거 예정
            @RequestParam(required = false, defaultValue = "ALL") BookStoryRequestDTO.BookStoryScope scope,
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) Long cursorId
    ) {
        if (scope == BookStoryRequestDTO.BookStoryScope.CLUB && clubId == null) {
            throw new IllegalArgumentException("scope가 CLUB일 때는 clubId 파라미터가 필수입니다.");
        }

        var bookStoriesByScope = bookStoryQueryFacade.getBookStoriesByScope(memberId, scope, clubId, cursorId);
        return ApiResponse.onSuccess(bookStoriesByScope);
    }

    @Operation(summary = "책 이야기 상세 조회 API", description = "특정 책 이야기의 상세 정보를 조회합니다.")
    @Parameter(name = "bookStoryId", description = "조회할 책 이야기 ID", required = true, example = "1")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 이야기를 찾을 수 없음")
    })
    @GetMapping("/{bookStoryId}")
    public ApiResponse<BookStoryResponseDTO.BookStoryResponse> getBookStory(
            @RequestParam String memberId, // TODO: 스프링 시큐리티 구현 후 제거 예정
            @PathVariable Long bookStoryId
    ) {
        // TODO: 구현 예정
        return null;
    }

    @Operation(summary = "책 이야기 좋아요 누르기 API", description = "특정 책 이야기에 좋아요를 추가하거나 취소합니다.")
    @Parameter(name = "bookStoryId", description = "좋아요를 누를 책 이야기 ID", required = true, example = "1")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 이야기를 찾을 수 없음")
    })
    @PostMapping("/{bookStoryId}/like")
    public ApiResponse<Long> toggleLikeBookStory(
            @PathVariable Long bookStoryId
    ) {
        // TODO: 구현 예정
        return null;
    }

    @Operation(summary = "책 이야기 수정 API", description = "작성한 책 이야기를 수정합니다.")
    @Parameter(name = "bookStoryId", description = "수정할 책 이야기 ID", required = true, example = "1")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "책 이야기의 수정 권한이 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 이야기를 찾을 수 없습니다.")
    })
    @PatchMapping("/{bookStoryId}")
    public ApiResponse<Long> updateBookStory(
            @RequestParam String memberId, // TODO: 스프링 시큐리티 구현 후 제거 예정
            @PathVariable Long bookStoryId,
            @Valid @RequestBody BookStoryRequestDTO.BookStoryUpdateRequestDTO request
    ) {
        Long updateBookStoryId = bookStoryCommandFacade.updateBookStory(memberId, bookStoryId, request);
        return ApiResponse.onSuccess(updateBookStoryId);
    }

    @Operation(summary = "책 이야기 삭제 API", description = "작성한 책 이야기를 삭제합니다.")
    @Parameter(name = "bookStoryId", description = "삭제할 책 이야기 ID", required = true, example = "1")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "책 이야기의 삭제 권한이 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 이야기를 찾을 수 없습니다.")
    })
    @DeleteMapping("/{bookStoryId}")
    public ApiResponse<String> deleteBookStory(
            @RequestParam String memberId, // TODO: 스프링 시큐리티 구현 후 제거 예정
            @PathVariable Long bookStoryId
    ) {
        bookStoryCommandFacade.deleteBookStory(memberId, bookStoryId);
        return ApiResponse.onSuccess("책 이야기가 성공적으로 삭제되었습니다.");
    }
}
