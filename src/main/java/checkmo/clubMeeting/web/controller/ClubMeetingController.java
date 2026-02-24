package checkmo.clubMeeting.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.clubMeeting.internal.service.ClubMeetingQueryFacade;
import checkmo.clubMeeting.internal.service.command.ClubMeetingCommandService;
import checkmo.clubMeeting.internal.service.command.ClubTopicCommandService;
import checkmo.clubMeeting.internal.validation.validCursor.ValidCursor;
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
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            description = "독서클럽의 모든 회원 정보(OWNER, STAFF, MEMBER)와 함께, 해당 미팅에 배정된 팀 번호까지 페이지네이션 조회합니다. " +
                    "만약 팀 번호가 null이면 아직 아무 팀에도 배정되지 않은 것입니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "독서모임 ID", required = true, example = "1"),
            @Parameter(name = "cursorId", description = "커서 ID (null이면 처음부터 조회)", required = false, example = "5"),
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
            @RequestParam(required = false) @ValidCursor Long cursorId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(
                clubMeetingQueryFacade.retrieveMeetingMemberList(clubId, meetingId, memberId, cursorId));
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

    @Operation(summary = "팀별 발제 조회", description = "팀별로 발제를 조회합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "독서클럽 ID", required = true, example = "1"),
            @Parameter(name = "meetingId", description = "독서모임 ID", required = true, example = "1"),
            @Parameter(name = "teamNumber", description = "팀  번호(조회하려는 조 이름이 x조(x는 A부터 Z까지 알파벳 중 하나)이면 x - ‘A’ + 1 로 조회하려는 조 번호로 요청", required = true, example = "1"),
            @Parameter(name = "cursorId", description = "커서 ID (null이면 처음부터 조회)", required = false, example = "5"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 모임의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 정기모임을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 팀을 찾을 수 없습니다.")
    })
    @GetMapping("/{meetingId}/teams/{teamNumber}/topics")
    public ApiResponse<MeetingResponseDTO.TeamTopic> getSelectedTopics(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @PathVariable @Min(value = 1) Integer teamNumber,
            @RequestParam(required = false) @ValidCursor Long cursorId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(
                clubMeetingQueryFacade.retrieveSelectableTopics(clubId, meetingId, teamNumber, memberId, cursorId));
    }

}
