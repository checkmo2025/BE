package checkmo.clubMeeting.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.clubMeeting.internal.service.ClubMeetingQueryFacade;
import checkmo.clubMeeting.internal.service.command.ClubBookReviewCommandService;
import checkmo.clubMeeting.internal.service.command.ClubMeetingCommandService;
import checkmo.clubMeeting.internal.service.command.ClubTopicCommandService;
import checkmo.clubMeeting.internal.validation.validCursor.ValidCursor;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.common.apiPayload.ApiResponse;
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
@RequestMapping("/api/clubs/{clubId}/bookshelves")
@RequiredArgsConstructor
@Tag(name = "책장", description = "독서 모임 책장, 한줄평 관리, 발제 관리 API")
public class ClubBookshelfController {

    private final ClubMeetingQueryFacade clubMeetingQueryFacade;
    private final ClubMeetingCommandService clubMeetingCommandService;
    private final ClubTopicCommandService clubTopicCommandService;
    private final ClubBookReviewCommandService clubBookReviewCommandService;

    // ========== 책장 ==========
    @Operation(summary = "책장 간편 조회", description = "책장을 커서 기반 조회합니다.(최신순 정렬)")
    @Parameters({
            @Parameter(name = "clubId", description = "책장을 조회할 클럽 ID", required = true, example = "1"),
            @Parameter(name = "cursorId", description = "마지막으로 조회한 책장 ID (무한 스크롤용)", required = false, example = "10"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 클럽을 찾을 수 없습니다."),
    })
    @GetMapping
    public ApiResponse<BookShelfResponseDTO.BookShelfList> getBookShelfList(
            @PathVariable Long clubId,
            @RequestParam(required = false) @ValidCursor Long cursorId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubMeetingQueryFacade.retrieveBookShelfList(clubId, memberId, cursorId));
    }

    @Operation(summary = "책장 상세 조회", description = "책장의 상세 정보를 조회합니다.(책장의 기본 정보만 제공, 발제/한줄평/정기모임은 API 별도 제공)")
    @Parameters({
            @Parameter(name = "clubId", description = "책장이 속한 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "책장(책장이 곧 Meeting)의 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
    })
    @GetMapping("/{meetingId}")
    public ApiResponse<BookShelfResponseDTO.BookShelfDetail> getBookShelfDetail(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubMeetingQueryFacade.retrieveBookShelf(clubId, meetingId, memberId));
    }

    @Operation(summary = "[운영진] 책장 생성", description = "책장을 생성합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "책장을 생성할 독서클럽 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "독서클럽 운영진만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
    })
    @PostMapping
    public ApiResponse<String> createMeeting(
            @PathVariable Long clubId,
            @RequestBody @Valid BookShelfRequestDTO.BookShelfCreate request,
            @CurrentId String memberId
    ) {
        clubMeetingCommandService.createMeeting(clubId, memberId, request);
        return ApiResponse.onSuccess("책장이 정상적으로 생성되었습니다.");
    }

    @Operation(summary = "[운영진] 책장 수정 조회", description = "책장 수정에 필요한 정보를 조회합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "책장이 속한 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "수정할 정기 책장 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "독서클럽 운영진만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
    })
    @GetMapping("/{meetingId}/edit")
    public ApiResponse<BookShelfResponseDTO.BookShelfUpdate> getBookShelfEditInfo(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubMeetingQueryFacade.retrieveBookShelfDetail(clubId, meetingId, memberId));
    }

    @Operation(summary = "[운영진] 책장 수정", description = "책장을 수정합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "책장이 속한 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "수정할 정기 책장 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "독서클럽 운영진만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
    })
    @PatchMapping("/{meetingId}")
    public ApiResponse<String> updateMeeting(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @RequestBody @Valid BookShelfRequestDTO.BookShelfUpdate request,
            @CurrentId String memberId
    ) {
        clubMeetingCommandService.updateMeeting(clubId, meetingId, memberId, request);
        return ApiResponse.onSuccess("책장이 정상적으로 수정되었습니다.");
    }

    @Operation(summary = "[운영진] 책장 삭제", description = "책장을 삭제합니다. 복구할 수 없습니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "책장이 속한 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "삭제할 정기 책장 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "독서클럽 운영진만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
    })
    @DeleteMapping("/{meetingId}")
    public ApiResponse<String> deleteMeeting(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @CurrentId String memberId
    ) {
        clubMeetingCommandService.deleteMeeting(clubId, meetingId, memberId);
        return ApiResponse.onSuccess("책장이 정상적으로 삭제되었습니다.");
    }

    // ========== 발제 ==========
    @Operation(summary = "책장에 대한 발제 조회", description = "발제를 최신순으로 조회합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "발제를 조회할 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "발제를 조회할 정기모임 ID", required = true, example = "1"),
            @Parameter(name = "cursorId", description = "마지막으로 조회한 발제 ID (무한 스크롤용)", required = false, example = "10"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
    })
    @GetMapping("/{meetingId}/topics")
    public ApiResponse<BookShelfResponseDTO.TopicList> getTopicList(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @RequestParam(required = false) @ValidCursor Long cursorId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubMeetingQueryFacade.retrieveTopicList(clubId, meetingId, memberId, cursorId));
    }

    @Operation(summary = "발제 등록", description = "발제를 등록합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "발제를 조회할 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "발제를 등록할 정기모임 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
    })
    @PostMapping("/{meetingId}/topics")
    public ApiResponse<String> createTopic(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @RequestBody @Valid BookShelfRequestDTO.TopicCreate request,
            @CurrentId String memberId
    ) {
        clubTopicCommandService.createTopic(clubId, meetingId, memberId, request);
        return ApiResponse.onSuccess("발제가 정상적으로 생성되었습니다.");
    }

    @Operation(summary = "발제 수정", description = "발제를 수정합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "발제를 조회할 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "발제를 수정할 정기모임 ID", required = true, example = "1"),
            @Parameter(name = "topicId", description = "수정할 발제 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이 발제에 대한 수정 권한이 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 발제를 찾을 수 없습니다."),
    })
    @PatchMapping("/{meetingId}/topics/{topicId}")
    public ApiResponse<String> updateTopic(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @PathVariable Long topicId,
            @RequestBody @Valid BookShelfRequestDTO.TopicCreate request,
            @CurrentId String memberId
    ) {
        clubTopicCommandService.updateTopic(clubId, meetingId, topicId, memberId, request);
        return ApiResponse.onSuccess("발제가 정상적으로 수정되었습니다.");
    }

    @Operation(summary = "발제 삭제", description = "발제를 삭제합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "발제를 조회할 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "발제를 삭제할 정기모임 ID", required = true, example = "1"),
            @Parameter(name = "topicId", description = "삭제할 발제 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이 발제에 대한 삭제 권한이 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기 독서모임을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 발제를 찾을 수 없습니다."),
    })
    @DeleteMapping("/{meetingId}/topics/{topicId}")
    public ApiResponse<String> deleteTopic(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @PathVariable Long topicId,
            @CurrentId String memberId
    ) {
        clubTopicCommandService.deleteTopic(clubId, meetingId, topicId, memberId);
        return ApiResponse.onSuccess("발제가 정상적으로 삭제되었습니다.");
    }

    // ========== 한줄평 ==========
    @Operation(summary = "한줄평 조회", description = "한줄평을 조회합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "한줄평을 조회할 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "한줄평을 조회할 정기 독서모임 ID", required = true, example = "1"),
            @Parameter(name = "cursorId", description = "마지막으로 조회한 한줄평 ID (무한 스크롤용)", required = false, example = "10"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
    })
    @GetMapping("/{meetingId}/reviews")
    public ApiResponse<BookShelfResponseDTO.BookReviewList> getBookReviewList(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @RequestParam(required = false) @ValidCursor Long cursorId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(
                clubMeetingQueryFacade.retrieveBookReviewList(clubId, meetingId, memberId, cursorId));
    }

    @Operation(summary = "한줄평 생성", description = "한줄평을 생성합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "한줄평을 등록할 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "한줄평을 등록한 정기모임 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "한줄평은 300자 이하로 입력해주세요."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "평점은 0.5 단위로만 입력 가능합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "평점은 1.0 이상 5.0 이하만 가능합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
    })
    @PostMapping("/{meetingId}/reviews")
    public ApiResponse<String> createReview(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @RequestBody @Valid BookShelfRequestDTO.BookReviewCreate request,
            @CurrentId String memberId
    ) {
        clubBookReviewCommandService.createBookReview(clubId, meetingId, memberId, request);
        return ApiResponse.onSuccess("한줄평이 생성되었습니다.");
    }

    @Operation(summary = "한줄평 수정", description = "한줄평을 수정합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "한줄평을 수정할 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "한줄평을 수정할 정기모임 ID", required = true, example = "1"),
            @Parameter(name = "reviewId", description = "수정할 한줄평 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "한줄평은 300자 이하로 입력해주세요."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "평점은 0.5 단위로만 입력 가능합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "평점은 1.0 이상 5.0 이하만 가능합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이 한줄평에 대한 수정/삭제 권한이 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 한줄평을 찾을 수 없습니다."),
    })
    @PatchMapping("/{meetingId}/reviews/{reviewId}")
    public ApiResponse<String> updateReview(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @PathVariable Long reviewId,
            @RequestBody @Valid BookShelfRequestDTO.BookReviewCreate request,
            @CurrentId String memberId
    ) {
        clubBookReviewCommandService.updateBookReview(clubId, meetingId, reviewId, memberId, request);
        return ApiResponse.onSuccess("한줄평이 수정되었습니다.");
    }

    @Operation(summary = "한줄평 삭제", description = "한줄평을 삭제합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "한줄평을 삭제할 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "한줄평을 삭제할 정기모임 ID", required = true, example = "1"),
            @Parameter(name = "reviewId", description = "삭제할 한줄평 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이 한줄평에 대한 수정/삭제 권한이 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 한줄평을 찾을 수 없습니다."),
    })
    @DeleteMapping("/{meetingId}/reviews/{reviewId}")
    public ApiResponse<String> deleteReview(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @PathVariable Long reviewId,
            @CurrentId String memberId
    ) {
        clubBookReviewCommandService.deleteBookReview(clubId, meetingId, reviewId, memberId);
        return ApiResponse.onSuccess("한줄평이 삭제되었습니다.");
    }
}
