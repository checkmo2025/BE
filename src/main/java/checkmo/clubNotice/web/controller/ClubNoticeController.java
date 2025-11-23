package checkmo.clubNotice.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.NoticeTag;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.internal.service.ClubNoticeQueryFacade;
import checkmo.clubNotice.internal.service.command.ClubNoticeCommandService;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "모임 공지사항", description = "독서 모임 공지사항, 투표 생성 및 관리 API")
public class ClubNoticeController {

    private final ClubNoticeQueryFacade clubNoticeQueryFacade;
    private final ClubNoticeCommandService clubNoticeCommandService;

    @Operation(summary = "공지사항 목록 조회 (미팅, 투표, 공지 모두 포함)", description = "특정 모임의 공지사항 목록을 조회합니다. onlyImportant=true 면 중요 공지사항만 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음")
    })
    @GetMapping("/api/clubs/{clubId}/notices")
    public ApiResponse<ClubNoticeResponseDTO.ClubNoticeList> getNoticeList(
            @CurrentId String memberId,
            @PathVariable Long clubId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false, defaultValue = "false") boolean onlyImportant
    ) {
        return ApiResponse.onSuccess(
                clubNoticeQueryFacade.retrieveClubNoticeList(clubId, memberId, cursorId, onlyImportant));
    }

    @Operation(summary = "순수 공지사항 작성", description = "특정 모임에 순수 공지사항을 작성합니다. (운영진만 작성 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 작성 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음")
    })
    @PostMapping("/api/clubs/{clubId}/notices")
    public ApiResponse<String> createPureVote(
            @CurrentId String memberId,
            @PathVariable Long clubId,
            @RequestBody @Valid ClubNoticeRequestDTO.CreateClubNotice request
    ) {
        Notice createdNotice = clubNoticeCommandService.createPureNotice(clubId, memberId, request);
        return ApiResponse.onSuccess("공지사항(id:" + createdNotice.getId() + "이 정상적으로 생성되었습니다.");
    }

    @Operation(summary = "순수 공지사항 상세 조회", description = "특정 순수 공지사항 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    @GetMapping("/api/clubs/{clubId}/notices/{noticeId}")
    public ApiResponse<ClubNoticeResponseDTO.ClubNoticeDetail> getNoticeDetail(
            @PathVariable Long clubId,
            @PathVariable Long noticeId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubNoticeQueryFacade.retrieveClubNoticeDetail(clubId, noticeId, NoticeTag.NOTICE, memberId));
    }

    @Operation(summary = "순수 공지사항 삭제", description = "순수 공지사항을 삭제합니다. (운영진만 삭제 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 삭제 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    @DeleteMapping("/api/clubs/{clubId}/notices/{noticeId}")
    public ApiResponse<String> deletePureNotice(
            @PathVariable Long clubId,
            @PathVariable Long noticeId,
            @CurrentId String memberId
    ) {
        clubNoticeCommandService.deletePureNotice(clubId, noticeId, memberId);
        return ApiResponse.onSuccess("공지사항이 삭제되었습니다.");
    }

    @Operation(summary = "모임 공지사항 상세 조회", description = "특정 모임 공지사항 상세 정보를 조회합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "클럽 ID", required = true, example = "1"),
            @Parameter(name = "noticeId", description = "모임 공지사항 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음"),
    })
    @GetMapping("/api/clubs/{clubId}/notices/meeting/{noticeId}")
    public ApiResponse<ClubNoticeResponseDTO.ClubNoticeDetail> getMeetingDetail(
            @PathVariable Long clubId,
            @PathVariable Long noticeId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubNoticeQueryFacade.retrieveClubNoticeDetail(clubId, noticeId, NoticeTag.MEETING, memberId));
    }

    @Operation(summary = "투표 생성", description = "특정 모임에 투표를 생성합니다. (운영진만 생성 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 생성 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음")
    })
    @PostMapping("/api/clubs/{clubId}/notices/votes")
    public ApiResponse<String> createVote(
            @CurrentId String memberId,
            @PathVariable Long clubId,
            @RequestBody @Valid ClubNoticeRequestDTO.CreateClubVote request
    ) {
        Vote createdVote = clubNoticeCommandService.createVote(clubId, memberId, request);
        return ApiResponse.onSuccess("투표(id:" + createdVote.getId() + "가 정상적으로 생성되었습니다.");
    }

    @Operation(summary = "투표 상세 조회", description = "특정 투표의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "투표를 찾을 수 없음")
    })
    @GetMapping("/api/clubs/{clubId}/notices/votes/{voteId}")
    public ApiResponse<ClubNoticeResponseDTO.ClubNoticeDetail> getVoteDetail(
            @PathVariable Long clubId,
            @PathVariable Long voteId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubNoticeQueryFacade.retrieveClubNoticeDetail(clubId, voteId, NoticeTag.VOTE, memberId));
    }

    @Operation(summary = "투표하기", description = "특정 투표에 참여합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "투표를 찾을 수 없음")
    })
    @PostMapping("/api/clubs/{clubId}/notices/votes/{voteId}/submit")
    public ApiResponse<String> submitVote(
            @PathVariable Long clubId,
            @PathVariable Long voteId,
            @CurrentId String memberId,
            @RequestBody @Valid ClubNoticeRequestDTO.VoteResult request
    ) {
        Long participatingVoteId = clubNoticeCommandService.haveVote(clubId, voteId, memberId, request);
        return ApiResponse.onSuccess("투표(id:" + participatingVoteId + ")에 투표했습니다.");
    }

    @Operation(summary = "투표 삭제", description = "특정 투표를 삭제합니다. (운영진만 삭제 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 삭제 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "투표를 찾을 수 없음")
    })
    @DeleteMapping("/api/clubs/{clubId}/notices/votes/{voteId}")
    public ApiResponse<String> deleteVote(
            @PathVariable Long clubId,
            @PathVariable Long voteId,
            @CurrentId String memberId
    ) {
        clubNoticeCommandService.deleteVote(clubId, voteId, memberId);
        return ApiResponse.onSuccess("투표가 삭제되었습니다.");
    }

}
