package checkmo.domain.club.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.club.facade.ClubCommandFacade;
import checkmo.domain.club.facade.ClubQueryFacade;
import checkmo.domain.club.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.domain.club.web.dto.bookshelf.BookShelfResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Tag(name = "모임 책장", description = "독서 모임 책장, 한줄평 관리 API")
public class ClubBookshelfController {

    private final ClubCommandFacade clubCommandFacade;
    private final ClubQueryFacade clubQueryFacade;

    // 책장 조회 (Meeting 기반)
    // GET /api/clubs/{clubId}/meetings - 책장(책장이 곧 Meeting) 전체 조회 ← 필터 적용 가능
    // GET /api/meetings/{meetingId} - 책장(책장이 곧 Meeting) 상세 화면 (책 정보, 발제들, 한줄평)

    // 한줄평(BookReview) 관리
    // GET /api/meetings/{meetingId}/reviews - 책(Meeting)에 대한 BookReview 전체 조회
    @Operation(summary = "한줄평 조회 API", description = "한줄평을 조회합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "한줄평을 조회할 정기 독서모임 ID", required = true, example = "1"),
            @Parameter(name = "lastReviewId", description = "마지막으로 조회한 한줄평 ID (무한 스크롤용)", required = false, example = "10"),
            @Parameter(name = "size", description = "조회할 한줄평 개수", required = true, example = "10"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기 독서모임을 찾을 수 없습니다."),
    })
    @GetMapping("/api/meetings/{meetingId}/reviews")
    public ApiResponse<BookShelfResponseDTO.BookReviewListDTO> getAllReviews(
            @PathVariable Long meetingId,
            @RequestParam(required = false) Long lastReviewId,
            @RequestParam Integer size,
            @RequestParam String memberId // TODO: @CurrentId로 추후 변경 예정
    ) {
        BookShelfResponseDTO.BookReviewListDTO bookReviewList = clubQueryFacade.getBookReviewList(meetingId, lastReviewId, size, memberId);
        return ApiResponse.onSuccess(bookReviewList);
    }

    @Operation(summary = "한줄평 생성 API", description = "한줄평을 생성합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "한줄평을 등록한 정기 독서모임 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "한줄평은 20자 이하로 입력해주세요."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "평점은 0.5 단위로만 입력 가능합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "평점은 1.0 이상 5.0 이하만 가능합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기 독서모임을 찾을 수 없습니다."),
    })
    @PostMapping("/api/meetings/{meetingId}/reviews")
    public ApiResponse<Long> createReview(
            @PathVariable Long meetingId,
            @RequestBody @Valid BookShelfRequestDTO.BookReviewDTO request,
            @RequestParam String memberId // TODO: @CurrentId로 추후 변경 예정
    ) {
        Long bookReviewId = clubCommandFacade.createBookReview(memberId, meetingId, request);
        return ApiResponse.onSuccess(bookReviewId);
    }


    // PATCH /api/meetings/{meetingId}/reviews/{reviewId} - 한줄평(Book Review) 수정
    @Operation(summary = "한줄평 수정 API", description = "한줄평을 수정합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "한줄평을 수정할 정기 독서모임 ID", required = true, example = "1"),
            @Parameter(name = "reviewId", description = "수정할 한줄평 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "한줄평은 20자 이하로 입력해주세요."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "평점은 0.5 단위로만 입력 가능합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "평점은 1.0 이상 5.0 이하만 가능합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이 한줄평에 대한 수정/삭제 권한이 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기 독서모임을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 한줄평을 찾을 수 없습니다."),
    })
    @PatchMapping("/api/meetings/{meetingId}/reviews/{reviewId}")
    public ApiResponse<Long> updateReview(
            @PathVariable Long meetingId,
            @PathVariable Long reviewId,
            @RequestBody @Valid BookShelfRequestDTO.BookReviewDTO request,
            @RequestParam String memberId // TODO: @CurrentId로 추후 변경 예정
    ) {
        Long updatedReviewId = clubCommandFacade.updateBookReview(memberId, meetingId, reviewId, request);
        return ApiResponse.onSuccess(updatedReviewId);
    }

    // DELETE /api/meetings/{meetingId}/reviews/{reviewId} - 한줄평(Book Review) 삭제
    @Operation(summary = "한줄평 삭제 API", description = "한줄평을 삭제합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "한줄평을 삭제할 정기 독서모임 ID", required = true, example = "1"),
            @Parameter(name = "reviewId", description = "삭제할 한줄평 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이 한줄평에 대한 수정/삭제 권한이 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기 독서모임을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 한줄평을 찾을 수 없습니다."),
    })
    @DeleteMapping("/api/meetings/{meetingId}/reviews/{reviewId}")
    public ApiResponse<Void> deleteReview(
            @PathVariable Long meetingId,
            @PathVariable Long reviewId,
            @RequestParam String memberId // TODO: @CurrentId로 추후 변경 예정
    ) {
        clubCommandFacade.deleteBookReview(memberId, meetingId, reviewId);
        return ApiResponse.onSuccess(null);
    }

    // 발제(Topic) 관리
    // POST /api/meetings/{meetingId}/topics - Topic 등록
    @Operation(summary = "발제 등록 API", description = "발제를 등록합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "발제를 등록할 정기 독서모임 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기 독서모임을 찾을 수 없습니다."),
    })
    @PostMapping("/api/meetings/{meetingId}/topics")
    public ApiResponse<Long> createTopic(
            @PathVariable Long meetingId,
            @RequestBody @Valid BookShelfRequestDTO.TopicDTO request,
            @RequestParam String memberId // TODO: @CurrentId로 추후 변경 예정
    ) {
        Long topicId = clubCommandFacade.createTopic(memberId, meetingId, request);
        return ApiResponse.onSuccess(topicId);
    }

    // PATCH /api/meetings/{meetingId}/topics/{topicId} - Topic 수정
    @Operation(summary = "발제 수정 API", description = "발제를 수정합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "발제를 수정할 정기 독서모임 ID", required = true, example = "1"),
            @Parameter(name = "topicId", description = "수정할 발제 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이 발제에 대한 수정 권한이 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기 독서모임을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 발제를 찾을 수 없습니다."),
    })
    @PatchMapping("/api/meetings/{meetingId}/topics/{topicId}")
    public ApiResponse<Long> updateTopic(
            @PathVariable Long meetingId,
            @PathVariable Long topicId,
            @RequestBody @Valid BookShelfRequestDTO.TopicDTO request,
            @RequestParam String memberId // TODO: @CurrentId로 추후 변경 예정
    ) {
        Long updatedTopicId = clubCommandFacade.updateTopic(memberId, meetingId, topicId, request);
        return ApiResponse.onSuccess(updatedTopicId);
    }
    // DELETE /api/meetings/{meetingId}/topics/{topicId} - Topic 삭제
    // GET /api/meetings/{meetingId}/topics - Meeting에 대한 Topic 전체보기
}
