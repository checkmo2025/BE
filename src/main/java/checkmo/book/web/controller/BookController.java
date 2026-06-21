package checkmo.book.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.book.internal.service.BookRecommendationService;
import checkmo.book.internal.service.command.BookSocialCommandService;
import checkmo.book.internal.service.query.AladinApiService;
import checkmo.book.internal.service.query.BookLikeQueryService;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.common.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "책 검색", description = "알라딘 API를 이용한 책 검색 API")
public class BookController {

    private final AladinApiService aladinApiService;
    private final BookRecommendationService bookRecommendationService;
    private final BookSocialCommandService bookSocialCommandService;
    private final BookLikeQueryService bookLikeQueryService;

    @Operation(summary = "책 검색 API", description = "키워드를 이용해 알라딘에서 책 목록을 검색합니다.")
    @Parameters({
            @Parameter(name = "keyword", description = "검색할 키워드 (책 제목, 저자)", required = true, example = "자바"),
            @Parameter(name = "page", description = "페이지 번호 (값 넣지 않으면 1부터 시작 그 다음 요청은 반드시 2!!)", example = "1")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 정보를 찾을 수 없음"),
    })
    @GetMapping("/search")
    public ApiResponse<BookResponseDTO.BookList> searchBook(
            @CurrentId String memberId,
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "1")
            @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
            int page
    ) {
        BookResponseDTO.BookList result = aladinApiService.retrieveSearchBooks(keyword, page, memberId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "책 상세 정보 조회 API", description = "ISBN 코드를 이용해 책의 상세 정보를 조회합니다.")
    @Parameter(name = "isbn", description = "책의 13자리 ISBN", required = true, example = "9791169213882")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 정보를 찾을 수 없음"),
    })
    @GetMapping({"/{isbn}"})
    public ApiResponse<DetailInfo> getBookDetail(@PathVariable String isbn) {
        DetailInfo result = aladinApiService.retrieveBookDetailInfo(isbn);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "책 추천 API", description = "블로그 베스트 추천 책 목록을 가져옵니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 정보를 찾을 수 없음"),
    })
    @GetMapping("/recommend")
    public ApiResponse<BookResponseDTO.BookList> recommendBooks(@CurrentId String memberId) {
        BookResponseDTO.BookList result = bookRecommendationService.retrieveRecommendedBooks(memberId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "책 좋아요/취소 API", description = "ISBN으로 좋아요를 추가하거나 취소합니다. DB에 없는 책이면 알라딘 조회 후 생성하여 처리합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 정보를 찾을 수 없음"),
    })
    @PostMapping("/{isbn}/like")
    public ApiResponse<BookResponseDTO.LikeResult> toggleLikeBook(
            @CurrentId String memberId,
            @PathVariable String isbn
    ) {
        BookResponseDTO.LikeResult result = bookSocialCommandService.toggleLikeOnBook(memberId, isbn);
        boolean liked = result.isLiked();

        if (liked) {
            return ApiResponse.onSuccess("좋아요가 추가되었습니다.", result);
        }
        return ApiResponse.onSuccess("좋아요가 취소되었습니다.", result);
    }

    @Operation(summary = "내가 좋아요한 책 목록 조회 API", description = "현재 로그인한 회원이 좋아요한 책 목록을 커서 기반으로 조회합니다.")
    @Parameter(name = "cursorId", description = "커서 ID (처음 조회 시 생략)", required = false, example = "10")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다.")
    })
    @GetMapping("/me/likes")
    public ApiResponse<BookResponseDTO.LikedBookList> getMyLikedBooks(
            @CurrentId String memberId,
            @RequestParam(required = false) Long cursorId
    ) {
        BookResponseDTO.LikedBookList result = bookLikeQueryService.retrieveMyLikedBooks(memberId, cursorId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "다른 회원이 좋아요한 책 목록 조회 API", description = "닉네임으로 특정 회원의 좋아요한 책 목록을 커서 기반으로 조회합니다.")
    @Parameters({
            @Parameter(name = "memberNickname", description = "조회 대상 회원 닉네임", required = true, example = "책벌레"),
            @Parameter(name = "cursorId", description = "커서 ID (처음 조회 시 생략)", required = false, example = "10")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 회원을 찾을 수 없습니다.")
    })
    @GetMapping("/{memberNickname}/likes")
    public ApiResponse<BookResponseDTO.LikedBookList> getMemberLikedBooks(
            @CurrentId String memberId,
            @PathVariable String memberNickname,
            @RequestParam(required = false) Long cursorId
    ) {
        BookResponseDTO.LikedBookList result = bookLikeQueryService.retrieveMemberLikedBooks(memberNickname, memberId, cursorId);
        return ApiResponse.onSuccess(result);
    }
}
