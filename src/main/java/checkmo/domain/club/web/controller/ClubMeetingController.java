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
@Tag(name = "독서모임-모임(미팅)", description = "독서 모임 미팅, 팀 발제, 토론조 관리, 캘린더 API")
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
    // POST /api/meetings/{meetingId}/teams - 토론조 생성
    // GET api/meetings/{meetingId}?teamNumber=1 - Team에 속한 인원 전체보기

    @Operation(summary = "독서모임 발제 + 선택한 팀 정보 전체 조회 API", description = "[모임] 페이지 - 독서모임의 발제와 선택한 팀 정보를 등록순으로 전체 조회합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "독서모임 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 모임의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 독서모임을 찾을 수 없습니다."),
    })
    @GetMapping("/api/meetings/{meetingId}/team-topics")
    public ApiResponse<List<MeetingResponseDTO.TopicDTO>> getTopics(
            @PathVariable Long meetingId,
            @CurrentId String memberId
    ) {
        List<MeetingResponseDTO.TopicDTO> topics = clubQueryFacade.findMeetingTopicsWithTeam(meetingId, memberId);
        return ApiResponse.onSuccess(topics);
    }

    @Operation(summary = "팀별 선택된 Topic 조회 API", description = "[모임] 팀별로 선택된 Topic을 조회합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "독서모임 ID", required = true, example = "1"),
            @Parameter(name = "teamNumber", description = "팀  번호(조회하려는 조 이름이 x조(x는 A부터 Z까지 알파벳 중 하나)이면 x - ‘A’ + 1 로 조회하려는 조 번호로 요청", required = true, example = "1")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 모임의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 독서모임을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 팀을 찾을 수 없습니다.")
    })
    @GetMapping("/api/meetings/{meetingId}/teams/{teamNumber}/topics")
    public ApiResponse<MeetingResponseDTO.TeamTopicDTO> getSelectedTopics(
            @PathVariable Long meetingId,
            @PathVariable Integer teamNumber,
            @CurrentId String memberId
    ) {
        MeetingResponseDTO.TeamTopicDTO teamTopicDTO = clubQueryFacade.findMeetingTopicsByTeam(meetingId, teamNumber, memberId);
        return ApiResponse.onSuccess(teamTopicDTO);
    }

    @Operation(summary = "팀에서 Topic 선택/해제 API", description = "[모임] 팀에서 Topic을 선택/해제합니다.")
    @Parameters({
            @Parameter(name = "meetingId", description = "독서모임 ID", required = true, example = "1"),
            @Parameter(name = "topicId", description = "선택/해제할 Topic ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 모임의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 독서모임을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 팀을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 발제를 찾을 수 없습니다.")
    })
    @PostMapping("/api/meetings/{meetingId}/topics/{topicId}")
    public ApiResponse<MeetingResponseDTO.TopicSelectionDTO> selectOrCancelTopic(
            @PathVariable Long meetingId,
            @PathVariable Long topicId,
            @RequestBody @Valid MeetingRequestDTO.TopicSelectionDTO request,
            @CurrentId String memberId
    ) {
        MeetingResponseDTO.TopicSelectionDTO result = clubCommandFacade.selectOrCancelTopic(meetingId, topicId, request, memberId);
        return ApiResponse.onSuccess(result);
    }
}
