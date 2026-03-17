package checkmo.clubMeeting.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.clubMeeting.internal.service.ClubMeetingQueryFacade;
import checkmo.clubMeeting.internal.service.command.ClubMeetingCommandService;
import checkmo.clubMeeting.internal.service.command.ClubTopicCommandService;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO.MeetingInfo;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO.NextMeetingRedirect;
import checkmo.common.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clubs/{clubId}/meetings")
@RequiredArgsConstructor
@Tag(name = "정기모임", description = "정기모임 토론 조/팀 발제 관리 API")
@Validated
public class ClubMeetingController {

    private final ClubMeetingQueryFacade clubMeetingQueryFacade;
    private final ClubMeetingCommandService clubMeetingCommandService;
    private final ClubTopicCommandService clubTopicCommandService;

    @Operation(summary = "이번 모임 바로가기", description = "미래 정기모임 중 가장 빠른 정기모임 상세보기 화면으로 이동합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "이번 모임을 조회할 독서클럽 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "다음 정기모임이 존재하지 않습니다."),
    })
    @GetMapping("/next")
    public ApiResponse<NextMeetingRedirect> getNextMeetingRedirect(
            @PathVariable Long clubId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubMeetingQueryFacade.retrieveNextMeeting(clubId, memberId));
    }

    @Operation(summary = "정기모임 조회", description = "정기모임 정보를 조회합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "정기 독서 모임을 조회할 독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "조회할 정기모임 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
    })
    @GetMapping("/{meetingId}")
    public ApiResponse<MeetingInfo> getMeeting(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubMeetingQueryFacade.retrieveMeetingInfo(clubId, meetingId, memberId));
    }

    @Operation(summary = "[운영진] 조 관리 - 독서모임 회원 전체 조회",
            description = "독서클럽의 모든 회원 정보(OWNER, STAFF, MEMBER)에 배정된 팀 정보를 맵핑합니다. " +
                    "만약 팀 번호가 null이면 아직 아무 팀에도 배정되지 않은 것입니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "독서모임 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 모임의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "독서클럽 운영진만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
    })
    @GetMapping("/{meetingId}/members")
    public ApiResponse<MeetingResponseDTO.MeetingMemberList> getMeetingMembers(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(
                clubMeetingQueryFacade.retrieveMeetingMemberList(clubId, meetingId, memberId));
    }

    @Operation(summary = "[운영진] 정기모임 조 관리", description = "Request Body를 기준으로 팀을 교체합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "팀을 관리할 정기 독서 모임 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "독서클럽 운영진만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
    })
    @PutMapping("/{meetingId}/teams")
    public ApiResponse<Void> manageTeams(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @RequestBody @Valid MeetingRequestDTO.TeamManage request,
            @CurrentId String memberId
    ) {
        clubMeetingCommandService.manageTeam(clubId, meetingId, memberId, request);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "팀별 발제 조회", description = "팀별로 발제를 조회합니다. 각 발제에 대해 해당 팀의 선택 여부를 함께 반환합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "독서모임 ID", required = true, example = "1"),
            @Parameter(name = "teamId", description = "팀  ID(조회하려는 조 이름이 x조)", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 모임의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 팀을 찾을 수 없습니다.")
    })
    @GetMapping("/{meetingId}/teams/{teamId}/topics")
    public ApiResponse<MeetingResponseDTO.TeamTopic> getSelectedTopics(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @PathVariable Long teamId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubMeetingQueryFacade.retrieveSelectableTopics(clubId, meetingId, teamId, memberId));
    }

}
