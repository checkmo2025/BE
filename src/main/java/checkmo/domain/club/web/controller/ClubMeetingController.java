package checkmo.domain.club.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.club.facade.ClubCommandFacade;
import checkmo.domain.club.facade.ClubQueryFacade;
import checkmo.domain.club.validation.validCursor.ValidCursor;
import checkmo.domain.club.validation.validSize.ValidSize;
import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;
import checkmo.domain.club.web.dto.meeting.MeetingResponseDTO;
import checkmo.global.auth.CurrentId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Tag(name = "독서모임 미팅", description = "독서 모임 미팅, 발제, 토론조 관리 API")
@Validated
public class ClubMeetingController {

    private final ClubCommandFacade clubCommandFacade;
    private final ClubQueryFacade clubQueryFacade;

    @Operation(summary = "정기 독서모임 생성 API", description = "정기 독서모임을 생성합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "정기 독서 모임을 생성할 독서클럽 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "독서클럽 운영진만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
    })
    @PostMapping("/api/clubs/{clubId}/meetings")
    public ApiResponse<Long> createMeeting(
            @PathVariable Long clubId,
            @RequestBody @Valid MeetingRequestDTO.MeetingCreateRequestDTO request,
            @CurrentId String memberId
    ) {
        Long meetingId = clubCommandFacade.createMeeting(clubId, memberId, request);
        return ApiResponse.onSuccess(meetingId);
    }

    @Operation(summary = "정기 독서모임 수정 API", description = "정기 독서모임을 수정합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "수정할 정기 독서 모임 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "독서클럽 운영진만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기 독서모임을 찾을 수 없습니다."),
    })
    @PatchMapping("/api/meetings/{meetingId}")
    public ApiResponse<Long> updateMeeting(
            @PathVariable Long meetingId,
            @RequestBody @Valid MeetingRequestDTO.MeetingUpdateRequestDTO request,
            @CurrentId String memberId
    ) {
        Long updateMeetingId = clubCommandFacade.updateMeeting(meetingId, memberId, request);
        return ApiResponse.onSuccess(updateMeetingId);
    }

    // GET /api/clubs/{clubId}/meetings - Meeting 전체 보기
    @Operation(summary = "정기 독서모임 간편 조회 API", description = "정기 독서모임을 커서 기반 최신순 정렬 간편 조회합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "정기 독서 모임을 조회할 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "cursorId", description = "커서 ID", required = false, example = "3"),
            @Parameter(name = "size", description = "조회할 개수 (기본값: 5)", required = false, example = "5")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
    })
    @GetMapping("/api/clubs/{clubId}/meetings")
    public ApiResponse<MeetingResponseDTO.MeetingListDTO> getMeetings(
            @PathVariable Long clubId,
            @RequestParam(required = false) @ValidCursor Long cursorId,
            @RequestParam(required = false, defaultValue = "5") @ValidSize Integer size,
            @CurrentId String memberId
    ) {
        MeetingResponseDTO.MeetingListDTO meetings = clubQueryFacade.getMeetingsByClub(clubId, cursorId, size, memberId);
        return ApiResponse.onSuccess(meetings);
    }
    // GET /api/meetings/{meetingId} - Meeting 상세 보기

    // 캘린더 관련
    // GET /api/clubs/{clubId}/calendar?year=[조회하고자 하는 연도]&month=[조회하고자 하는 달] - 독서모임의 모임 캘린더 조회
    @Operation(summary = "독서모임 캘린더 조회 API", description = "독서모임의 모임 캘린더를 조회합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "year", description = "조회하고자 하는 연도", required = true, example = "2023"),
            @Parameter(name = "month", description = "조회하고자 하는 달", required = true, example = "10")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
    })
    @GetMapping("/api/clubs/{clubId}/calendar")
    public ApiResponse<List<MeetingResponseDTO.MeetingInfoDTO>> getClubCalendar(
            @PathVariable Long clubId,
            @RequestParam @Min(2000) @Max(2050) int year,
            @RequestParam @Min(1) @Max(12) int month,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubQueryFacade.getClubMeetingCalendar(clubId, year, month, memberId));
    }

    // 토론조 관리
    // GET /api/meetings/{meetingId}/teams - Meeting 참여 인원 전체 조회
    @Operation(summary = "토론조 관리(생성/수정/삭제) API", description = "Response Body에 따라 정기 독서모임의 토론조를 생성/수정/삭제합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "팀을 관리할 정기 독서 모임 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "독서클럽 운영진만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기 독서모임을 찾을 수 없습니다."),
    })
    @PutMapping("/api/meetings/{meetingId}/teams")
    public ApiResponse<Void> manageTeams(
            @PathVariable Long meetingId,
            @RequestBody @Valid MeetingRequestDTO.TeamManageDTO request,
            @CurrentId String memberId
    ) {
        clubCommandFacade.manageTeams(memberId, meetingId, request);
        return ApiResponse.onSuccess(null);
    }
    // GET api/meetings/{meetingId}?teamNumber=1 - Team에 속한 인원 전체보기

    // 팀-발제 연결 관리
    // POST /api/meetings/{meetingId}/teams/{teamId}/topics/{topicId}/select - Team에서 Topic 선택하기
    // GET /api/meetings/{meetingId}/teams/{teamId}/topics - Team별로 선택된 Topic 보기
}
