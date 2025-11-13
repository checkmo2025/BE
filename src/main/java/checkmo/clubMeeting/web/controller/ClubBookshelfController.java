package checkmo.clubMeeting.web.controller;

import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubMeeting.internal.service.command.ClubBookReviewCommandService;
import checkmo.clubMeeting.internal.service.command.ClubMeetingCommandService;
import checkmo.clubMeeting.internal.service.command.ClubTopicCommandService;
import checkmo.clubMeeting.internal.validation.validCursor.ValidCursor;
import checkmo.clubMeeting.internal.validation.validSize.ValidSize;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.common.apiPayload.ApiResponse;
import checkmo.member.CurrentId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Tag(name = "독서모임-책장", description = "독서 모임 책장, 한줄평 관리, 발제 관리 API")
public class ClubBookshelfController {

    private final ClubMeetingCommandService clubMeetingCommandService;
    private final ClubTopicCommandService clubTopicCommandService;
    private final ClubBookReviewCommandService clubBookReviewCommandService;
    private final ClubMeetingAPI clubMeetingAPI;

    @Operation(summary = "책장 간편 조회 API", description = "책장을 커서 기반 사이즈만큼 조회합니다.(최신순 정렬)")
    @Parameters({
            @Parameter(name = "clubId", description = "책장을 조회할 클럽 ID", required = true, example = "1"),
            @Parameter(name = "cursorId", description = "마지막으로 조회한 책장 ID (무한 스크롤용)", required = false, example = "10"),
            @Parameter(name = "size", description = "조회할 책장 개수", required = false, example = "9"),
            @Parameter(name = "generation", description = "활동 기수", required = false, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 클럽을 찾을 수 없습니다."),
    })
    @GetMapping("/api/clubs/{clubId}/bookshelves")
    public ApiResponse<BookShelfResponseDTO.BookShelfListDTO> getBookShelfList(
            @PathVariable Long clubId,
            @RequestParam(required = false) @ValidCursor Long cursorId,
            @RequestParam(required = false, defaultValue = "9") @ValidSize Integer size,
            @RequestParam(required = false) Integer generation,
            @CurrentId String memberId
    ) {
        BookShelfResponseDTO.BookShelfListDTO bookShelfList = clubMeetingAPI.getBookShelfList(clubId, cursorId, size,
                generation, memberId);
        return ApiResponse.onSuccess(bookShelfList);
    }

    @Operation(summary = "책장 상세 조회 API", description = "책장의 상세 정보를 조회합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "책장(책장이 곧 Meeting)의 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기 독서모임을 찾을 수 없습니다."),
    })
    @GetMapping("/api/bookshelves/{meetingId}")
    public ApiResponse<BookShelfResponseDTO.BookShelfDetailDTO> getBookShelfDetail(
            @PathVariable Long meetingId,
            @CurrentId String memberId
    ) {
        BookShelfResponseDTO.BookShelfDetailDTO bookShelfDetail = clubMeetingAPI.getBookShelfDetail(meetingId,
                memberId);
        return ApiResponse.onSuccess(bookShelfDetail);
    }

    @Operation(summary = "한줄평 조회 API", description = "한줄평을 조회합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "한줄평을 조회할 정기 독서모임 ID", required = true, example = "1"),
            @Parameter(name = "cursorId", description = "마지막으로 조회한 한줄평 ID (무한 스크롤용)", required = false, example = "10"),
            @Parameter(name = "size", description = "조회할 한줄평 개수", required = false, example = "15"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기 독서모임을 찾을 수 없습니다."),
    })
    @GetMapping("/api/meetings/{meetingId}/reviews")
    public ApiResponse<BookShelfResponseDTO.BookReviewListDTO> getAllReviews(
            @PathVariable Long meetingId,
            @RequestParam(required = false) @ValidCursor Long cursorId,
            @RequestParam @ValidSize Integer size,
            @CurrentId String memberId
    ) {
        BookShelfResponseDTO.BookReviewListDTO bookReviewList = clubMeetingAPI.getBookReviewList(meetingId, cursorId,
                size,
                memberId);
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
            @CurrentId String memberId
    ) {
        Long bookReviewId = clubBookReviewCommandService.createBookReview(meetingId, memberId, request);
        return ApiResponse.onSuccess(bookReviewId);
    }

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
            @CurrentId String memberId
    ) {
        Long updatedReviewId = clubBookReviewCommandService.updateBookReview(meetingId, reviewId, memberId, request);
        return ApiResponse.onSuccess(updatedReviewId);
    }

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
            @CurrentId String memberId
    ) {
        clubBookReviewCommandService.deleteBookReview(meetingId, reviewId, memberId);
        return ApiResponse.onSuccess(null);
    }

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
            @CurrentId String memberId
    ) {
        Long topicId = clubTopicCommandService.createTopic(meetingId, memberId, request);
        return ApiResponse.onSuccess(topicId);
    }

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
            @CurrentId String memberId
    ) {
        Long updatedTopicId = clubTopicCommandService.updateTopic(meetingId, topicId, memberId, request);
        return ApiResponse.onSuccess(updatedTopicId);
    }

    @Operation(summary = "발제 삭제 API", description = "발제를 삭제합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "발제를 삭제할 정기 독서모임 ID", required = true, example = "1"),
            @Parameter(name = "topicId", description = "삭제할 발제 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이 발제에 대한 삭제 권한이 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기 독서모임을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 발제를 찾을 수 없습니다."),
    })
    @DeleteMapping("/api/meetings/{meetingId}/topics/{topicId}")
    public ApiResponse<Void> deleteTopic(
            @PathVariable Long meetingId,
            @PathVariable Long topicId,
            @CurrentId String memberId
    ) {
        clubTopicCommandService.deleteTopic(meetingId, topicId, memberId);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "미팅에 대한 발제 조회 API", description = "[책장] 페이지 - 발제를 최신순으로 조회합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "발제를 조회할 정기 독서모임 ID", required = true, example = "1"),
            @Parameter(name = "cursorId", description = "마지막으로 조회한 발제 ID (무한 스크롤용)", required = false, example = "10"),
            @Parameter(name = "size", description = "조회할 발제 개수", required = false, example = "15"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기 독서모임을 찾을 수 없습니다."),
    })
    @GetMapping("/api/meetings/{meetingId}/topics")
    public ApiResponse<BookShelfResponseDTO.TopicListDTO> getTopicList(
            @PathVariable Long meetingId,
            @RequestParam(required = false) @ValidCursor Long cursorId,
            @RequestParam(required = false, defaultValue = "15") @ValidSize Integer size,
            @CurrentId String memberId
    ) {
        BookShelfResponseDTO.TopicListDTO topicList = clubMeetingAPI.findTopicsByMeeting(meetingId, cursorId, size,
                memberId);
        return ApiResponse.onSuccess(topicList);
    }
}
