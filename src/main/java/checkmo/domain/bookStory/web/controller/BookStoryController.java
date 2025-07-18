package checkmo.domain.bookStory.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.domain.bookStory.web.dto.BookStoryResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book-stories")
@RequiredArgsConstructor
@Tag(name = "책 이야기", description = "책 이야기 업로드, 조회, 좋아요, 수정, 삭제 관련 API")
public class BookStoryController {
    @Operation(summary = "책 이야기 업로드 API", description = "새로운 책 이야기를 작성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ApiResponse<Long> createBookStory(
            @RequestBody BookStoryRequestDTO.BookStoryCreateRequestDTO request
    ) {
        // TODO: 구현 예정
        return null;
    }

    @Operation(summary = "책 이야기 전체보기 API", description = "조건에 따라 책 이야기 목록을 조회합니다.")
    @Parameters({
            @Parameter(name = "scope", description = "조회 범위 (all: 전체, my: 내 책이야기, club: 특정 클럽)", required = true, example = "all"),
            @Parameter(name = "clubId", description = "조회하는 Club ID (scope가 club일 때 필수)", required = false, example = "1"),
            @Parameter(name = "page", description = "페이지 번호 (기본값: 1)", required = false, example = "1")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @GetMapping
    public ApiResponse<BookStoryResponseDTO.BookStoryListResponse> getBookStories(
            @RequestParam String scope,
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false, defaultValue = "1")
            @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
            int page
    ) {
        // TODO: 구현 예정
        return null;
    }

    @Operation(summary = "책 이야기 상세 조회 API", description = "특정 책 이야기의 상세 정보를 조회합니다.")
    @Parameter(name = "bookStoryId", description = "조회할 책 이야기 ID", required = true, example = "1")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 이야기를 찾을 수 없음")
    })
    @GetMapping("/{bookStoryId}")
    public ApiResponse<BookStoryResponseDTO.BookStoryResponse> getBookStory(
            @PathVariable Long bookStoryId
    ) {
        // TODO: 구현 예정
        return null;
    }

    @Operation(summary = "책 이야기 좋아요 누르기 API", description = "특정 책 이야기에 좋아요를 추가하거나 취소합니다.")
    @Parameter(name = "bookStoryId", description = "좋아요를 누를 책 이야기 ID", required = true, example = "1")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 이야기를 찾을 수 없음")
    })
    @PatchMapping("/{bookStoryId}")
    public ApiResponse<Long> updateBookStory(
            @PathVariable Long bookStoryId,
            @RequestBody BookStoryRequestDTO.BookStoryUpdateRequestDTO request
    ) {
        // TODO: 구현 예정
        return null;
    }

    @Operation(summary = "책 이야기 삭제 API", description = "작성한 책 이야기를 삭제합니다.")
    @Parameter(name = "bookStoryId", description = "삭제할 책 이야기 ID", required = true, example = "1")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 이야기를 찾을 수 없음")
    })
    @DeleteMapping("/{bookStoryId}")
    public ApiResponse<Long> deleteBookStory(
            @PathVariable Long bookStoryId
    ) {
        // TODO: 구현 예정
        return null;
    }
}
