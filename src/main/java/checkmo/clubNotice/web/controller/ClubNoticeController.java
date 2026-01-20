package checkmo.clubNotice.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.service.ClubNoticeQueryFacade;
import checkmo.clubNotice.internal.service.command.ClubNoticeCommandService;
import checkmo.clubNotice.internal.service.command.NoticeCommentCommandService;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO.ClubNoticePreviewList;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO.NoticeCommentList;
import checkmo.common.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs/{clubId}/notices")
@RequiredArgsConstructor
@Tag(name = "모임 공지사항", description = "독서 모임 공지사항, 투표 생성 및 관리 API")
public class ClubNoticeController {

    private final ClubNoticeQueryFacade clubNoticeQueryFacade;
    private final ClubNoticeCommandService clubNoticeCommandService;
    private final NoticeCommentCommandService noticeCommentCommandService;

    @Operation(summary = "공지사항 목록 조회", description = "특정 모임의 공지사항 목록을 조회합니다. onlyImportant=true 면 중요 공지사항만 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음"),
    })
    @GetMapping
    public ApiResponse<ClubNoticePreviewList> getNoticeList(
            @CurrentId String memberId,
            @PathVariable Long clubId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false, defaultValue = "false") boolean onlyImportant
    ) {
        return ApiResponse.onSuccess(
                clubNoticeQueryFacade.retrieveClubNoticeList(clubId, memberId, cursorId, onlyImportant));
    }

    @Operation(summary = "공지사항 상세 조회", description = "공지사항 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    @GetMapping("/{noticeId}")
    public ApiResponse<ClubNoticeResponseDTO.ClubNoticeDetail> getNoticeDetail(
            @PathVariable Long clubId,
            @PathVariable Long noticeId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubNoticeQueryFacade.retrieveClubNoticeDetail(clubId, noticeId, memberId));
    }

    @Operation(summary = "공지사항 작성", description = "특정 모임에 공지사항을 작성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 작성 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음"),
    })
    @PostMapping
    public ApiResponse<String> createPureVote(
            @CurrentId String memberId,
            @PathVariable Long clubId,
            @RequestBody @Valid ClubNoticeRequestDTO.CreateClubNotice request
    ) {
        Notice createdNotice = clubNoticeCommandService.createNotice(clubId, memberId, request);
        return ApiResponse.onSuccess("공지사항(id:" + createdNotice.getId() + ")이 정상적으로 생성되었습니다.");
    }

    // 공지사항 수정

    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 삭제 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음"),
    })
    @DeleteMapping("/{noticeId}")
    public ApiResponse<String> deletePureNotice(
            @PathVariable Long clubId,
            @PathVariable Long noticeId,
            @CurrentId String memberId
    ) {
        clubNoticeCommandService.deleteNotice(clubId, memberId, noticeId);
        return ApiResponse.onSuccess("공지사항이 삭제되었습니다.");
    }

    @Operation(summary = "투표하기", description = "특정 투표에 참여합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "투표를 찾을 수 없음")
    })
    @PostMapping("/{noticeId}/votes/{voteId}")
    public ApiResponse<String> submitVote(
            @PathVariable Long clubId,
            @PathVariable Long noticeId,
            @PathVariable Long voteId,
            @CurrentId String memberId,
            @RequestBody @Valid ClubNoticeRequestDTO.VoteResult request
    ) {
        Long participatingVoteId = clubNoticeCommandService.haveVote(clubId, memberId, noticeId, voteId, request);
        return ApiResponse.onSuccess("투표(id:" + participatingVoteId + ")에 투표했습니다.");
    }

    @Operation(summary = "공지사항 댓글 조회", description = "특정 공지사항의 댓글을 최신순 10개 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    @GetMapping("/{noticeId}/comments")
    public ApiResponse<NoticeCommentList> getNoticeComments(
            @PathVariable Long clubId,
            @PathVariable Long noticeId,
            @CurrentId String memberId,
            @RequestParam(required = false) Long cursorId
    ) {
        return ApiResponse.onSuccess(
                clubNoticeQueryFacade.retrieveNoticeComments(clubId, noticeId, memberId, cursorId));
    }

    @Operation(summary = "공지사항 댓글 작성", description = "특정 공지사항에 댓글을 작성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    @PostMapping("/{noticeId}")
    public ApiResponse<String> createNoticeComment(
            @PathVariable Long clubId,
            @PathVariable Long noticeId,
            @CurrentId String memberId,
            @RequestBody @Valid ClubNoticeRequestDTO.CreateClubNoticeComment request
    ) {
        noticeCommentCommandService.createNoticeComment(clubId, noticeId, memberId, request);
        return ApiResponse.onSuccess("공지사항 댓글이 정상적으로 생성되었습니다.");
    }

    @Operation(summary = "공지사항 댓글 수정", description = "특정 공지사항 댓글을 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "댓글 작성자/모임 운영진/관리자만 수정 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @PostMapping("/{noticeId}/comments/{commentId}")
    public ApiResponse<String> updateNoticeComment(
            @PathVariable Long clubId,
            @PathVariable Long noticeId,
            @PathVariable Long commentId,
            @CurrentId String memberId,
            @RequestBody @Valid ClubNoticeRequestDTO.CreateClubNoticeComment request
    ) {
        noticeCommentCommandService.updateNoticeComment(clubId, noticeId, commentId, memberId, request);
        return ApiResponse.onSuccess("공지사항 댓글이 정상적으로 수정되었습니다.");
    }

    @Operation(summary = "공지사항 댓글 삭제", description = "특정 공지사항 댓글을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "댓글 작성자/모임 운영진/관리자만 삭제 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @DeleteMapping("/{noticeId}/comments/{commentId}")
    public ApiResponse<String> deleteNoticeComment(
            @PathVariable Long clubId,
            @PathVariable Long noticeId,
            @PathVariable Long commentId,
            @CurrentId String memberId
    ) {
        noticeCommentCommandService.deleteNoticeComment(clubId, noticeId, commentId, memberId);
        return ApiResponse.onSuccess("공지사항 댓글이 삭제되었습니다.");
    }

}
