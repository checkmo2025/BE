package checkmo.bookStory.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.bookStory.internal.service.BookStoryQueryFacade;
import checkmo.bookStory.internal.service.command.BookStoryCommandService;
import checkmo.bookStory.internal.service.command.BookStoryCommentCommandService;
import checkmo.bookStory.web.dto.BookStoryResponseDTO;
import checkmo.common.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/book-stories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "책 이야기 (관리자)", description = "책 이야기 관리 API (관리자 전용)")
public class BookStoryAdminController {

    private final BookStoryQueryFacade bookStoryQueryFacade;
    private final BookStoryCommandService bookStoryCommandService;
    private final BookStoryCommentCommandService bookStoryCommentCommandService;

    @Operation(summary = "책 이야기 목록 조회 (관리자)", description = "관리자 전용 책 이야기 목록 조회 API입니다. 제목 검색과 오프셋 기반 페이지네이션을 지원합니다.")
    @Parameter(name = "page", description = "페이지 번호 (1부터 시작)", required = false, example = "1")
    @Parameter(name = "keyword", description = "책 이야기 제목 검색어", required = false, example = "왕자")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다.")
    })
    @GetMapping
    public ApiResponse<BookStoryResponseDTO.AdminBookStoryList> getBookStoriesForAdmin(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.onSuccess(bookStoryQueryFacade.fetchBookStoriesForAdmin(keyword, page));
    }

    @Operation(summary = "책 이야기 상세 조회 (관리자)", description = "관리자 전용 책 이야기 상세 조회 API입니다. 일반 사용자용 상세 응답과 동일합니다.")
    @Parameter(name = "bookStoryId", description = "조회할 책 이야기 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 이야기를 찾을 수 없습니다.")
    })
    @GetMapping("/{bookStoryId}")
    public ApiResponse<BookStoryResponseDTO.DetailInfo> getBookStoryForAdmin(
            @CurrentId Long memberId,
            @PathVariable Long bookStoryId
    ) {
        return ApiResponse.onSuccess(bookStoryQueryFacade.fetchBookStoryDetailInfoForAdmin(memberId, bookStoryId));
    }

    @Operation(summary = "책 이야기 삭제 (관리자)", description = "관리자가 책 이야기를 삭제합니다.")
    @Parameter(name = "bookStoryId", description = "삭제할 책 이야기 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 이야기를 찾을 수 없습니다.")
    })
    @DeleteMapping("/{bookStoryId}")
    public ApiResponse<String> deleteBookStoryByAdmin(@PathVariable Long bookStoryId) {
        bookStoryCommandService.deleteBookStoryByAdmin(bookStoryId);
        return ApiResponse.onSuccess("책 이야기가 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "댓글 삭제 (관리자)", description = "관리자가 책 이야기 댓글 또는 대댓글을 삭제합니다.")
    @Parameter(name = "bookStoryId", description = "댓글이 속한 책 이야기 ID", required = true, example = "1")
    @Parameter(name = "commentId", description = "삭제할 댓글 ID", required = true, example = "10")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 이야기 또는 댓글을 찾을 수 없습니다.")
    })
    @DeleteMapping("/{bookStoryId}/comments/{commentId}")
    public ApiResponse<Long> deleteCommentByAdmin(
            @PathVariable Long bookStoryId,
            @PathVariable Long commentId
    ) {
        Long deletedCommentId = bookStoryCommentCommandService.deleteCommentByAdmin(bookStoryId, commentId);
        return ApiResponse.onSuccess(deletedCommentId);
    }

    @Operation(
            summary = "특정 회원 책이야기 목록 조회 (관리자)",
            description = "관리자가 특정 회원이 작성한 책이야기 목록을 조회합니다. 커서 기반 무한 스크롤을 지원합니다."
    )
    @Parameter(name = "memberNickname", description = "조회할 회원 닉네임", required = true, example = "hy_0716")
    @Parameter(name = "cursorId", description = "커서 ID (처음에는 null)", required = false, example = "10")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 회원을 찾을 수 없습니다.")
    })
    @GetMapping("/members/{memberNickname}")
    public ApiResponse<BookStoryResponseDTO.BookStoryList> getMemberBookStoriesForAdmin(
            @CurrentId Long memberId,
            @PathVariable String memberNickname,
            @RequestParam(required = false) Long cursorId
    ) {
        return ApiResponse.onSuccess(
                bookStoryQueryFacade.fetchMemberBookStories(memberId, memberNickname, cursorId)
        );
    }
}
