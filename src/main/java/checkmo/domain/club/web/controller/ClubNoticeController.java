package checkmo.domain.club.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.club.facade.ClubCommandFacade;
import checkmo.domain.club.facade.ClubQueryFacade;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clubs/{clubId}/notices")
@RequiredArgsConstructor
@Tag(name = "모임 공지사항", description = "독서 모임 공지사항, 투표 생성 및 관리 API")
public class ClubNoticeController {

    private final ClubCommandFacade clubCommandFacade;
    private final ClubQueryFacade clubQueryFacade;

    @Operation(summary = "공지사항 작성", description = "특정 모임에 공지사항을 작성합니다. (운영진만 작성 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 작성 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음")
    })
    @PostMapping("")
    public ApiResponse<ClubResponseDTO.ClubNoticeDetailDTO> createNotice(
            @PathVariable Long clubId,
            @Parameter(name = "MemberId", description = "회원 ID (시큐리티 구현 후 삭제 예정)", required = true, example = "mem_001")
            @RequestHeader("MemberId") String memberId,
            @RequestBody @Valid ClubRequestDTO.CreateClubNoticeDTO request
    ) {
        // TODO - 로그인 된 사용자가 맞는지 검사하는 어노테이션 필요
        return ApiResponse.onSuccess(clubCommandFacade.createNotice(clubId, memberId, request));
    }

    @Operation(summary = "투표 생성", description = "특정 모임에 투표를 생성합니다. (운영진만 생성 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 생성 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음")
    })
    @PostMapping("/votes")
    public ApiResponse<ClubResponseDTO.ClubNoticeDetailDTO> createVote(
            @PathVariable Long clubId,
            @Parameter(name = "MemberId", description = "회원 ID (시큐리티 구현 후 삭제 예정)", required = true, example = "mem_001")
            @RequestHeader("MemberId") String memberId,
            @RequestBody @Valid ClubRequestDTO.CreateClubVoteDTO request
    ) {
        // TODO - 로그인 된 사용자가 맞는지 검사하는 어노테이션 필요
        return ApiResponse.onSuccess(clubCommandFacade.createVote(clubId, memberId, request));
    }

    @Operation(summary = "공지사항 상세 조회", description = "특정 공지사항 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    @GetMapping("/{noticeId}")
    public ApiResponse<ClubResponseDTO.ClubNoticeDetailDTO> getNoticeDetail(
            @PathVariable Long clubId,
            @PathVariable Long noticeId,
            @Parameter(name = "MemberId", description = "회원 ID (시큐리티 구현 후 삭제 예정)", required = true, example = "mem_001")
            @RequestHeader("MemberId") String memberId
    ) {
        // TODO - 로그인 된 사용자가 맞는지 검사하는 어노테이션 필요
        return ApiResponse.onSuccess(clubQueryFacade.getNoticeDetail(clubId, noticeId, "공지", memberId));
    }

    @Operation(summary = "투표 상세 조회", description = "특정 투표의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "투표를 찾을 수 없음")
    })
    @GetMapping("/votes/{voteId}")
    public ApiResponse<ClubResponseDTO.ClubNoticeDetailDTO> getVoteDetail(
            @PathVariable Long clubId,
            @PathVariable Long voteId,
            @Parameter(name = "MemberId", description = "회원 ID (시큐리티 구현 후 삭제 예정)", required = true, example = "mem_001")
            @RequestHeader("MemberId") String memberId
    ) {
        // TODO - 로그인 된 사용자가 맞는지 검사하는 어노테이션 필요
        return ApiResponse.onSuccess(clubQueryFacade.getNoticeDetail(clubId, voteId, "투표", memberId));
    }

    @Operation(summary = "투표하기", description = "특정 투표에 참여합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "모임 멤버가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "투표를 찾을 수 없음")
    })
    @PostMapping("/votes/{voteId}/submit")
    public ApiResponse<ClubResponseDTO.ClubNoticeDetailDTO> submitVote(
            @PathVariable Long clubId,
            @PathVariable Long voteId,
            @Parameter(name = "MemberId", description = "회원 ID", required = true, example = "mem_001")
            @RequestHeader("MemberId") String memberId,
            @RequestBody @Valid ClubRequestDTO.VoteResultDTO request
    ) {
        // TODO - 로그인 된 사용자가 맞는지 검사하는 어노테이션 필요
        return ApiResponse.onSuccess(clubCommandFacade.participateInPoll(clubId, memberId, voteId, request));
    }

    @Operation(summary = "공지사항 삭제", description = "특정 공지사항을 삭제합니다. (운영진만 삭제 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 삭제 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    @DeleteMapping("/{noticeId}")
    public ApiResponse<String> deleteNotice(
            @PathVariable Long clubId,
            @PathVariable Long noticeId,
            @Parameter(name = "MemberId", description = "회원 ID (시큐리티 구현 후 삭제 예정)", required = true, example = "mem_001")
            @RequestHeader("MemberId") String memberId
    ) {
        // TODO - 로그인 된 사용자가 맞는지 검사하는 어노테이션 필요
        clubCommandFacade.deleteNotice(clubId, memberId, noticeId);
        return ApiResponse.onSuccess("공지사항이 삭제되었습니다.");
    }

    @Operation(summary = "투표 삭제", description = "특정 투표를 삭제합니다. (운영진만 삭제 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 삭제 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "투표를 찾을 수 없음")
    })
    @DeleteMapping("/votes/{voteId}")
    public ApiResponse<String> deleteVote(
            @PathVariable Long clubId,
            @PathVariable Long voteId,
            @Parameter(name = "MemberId", description = "회원 ID (시큐리티 구현 후 삭제 예정)", required = true, example = "mem_001")
            @RequestHeader("MemberId") String memberId
    ) {
        // TODO - 로그인 된 사용자가 맞는지 검사하는 어노테이션 필요
        clubCommandFacade.deleteVote(clubId, memberId, voteId);
        return ApiResponse.onSuccess("투표가 삭제되었습니다.");
    }

}
