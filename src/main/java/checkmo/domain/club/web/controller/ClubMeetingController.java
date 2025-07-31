package checkmo.domain.club.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.club.facade.ClubCommandFacade;
import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;
import checkmo.global.auth.CurrentId;
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
@Tag(name = "모임 토론", description = "독서 모임 미팅, 발제, 토론조 관리 API")
public class ClubMeetingController {

    private final ClubCommandFacade clubCommandFacade;

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
    // GET /api/meetings/{meetingId} - Meeting 상세 보기

    // 토론조 관리
    // GET /api/meetings/{meetingId}/teams - Meeting 참여 인원 전체 조회
    // POST /api/meetings/{meetingId}/teams - 토론조 생성
    // GET api/meetings/{meetingId}?teamNumber=1 - Team에 속한 인원 전체보기

    // 팀-발제 연결 관리
    // POST /api/meetings/{meetingId}/teams/{teamId}/topics/{topicId}/select - Team에서 Topic 선택하기
    // GET /api/meetings/{meetingId}/teams/{teamId}/topics - Team별로 선택된 Topic 보기
}
