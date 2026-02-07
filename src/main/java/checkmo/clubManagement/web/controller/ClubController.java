package checkmo.clubManagement.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.clubManagement.internal.service.ClubManagementQueryFacade;
import checkmo.clubManagement.internal.service.command.ClubManagementCommandService;
import checkmo.clubManagement.internal.service.command.ClubMemberCommandService;
import checkmo.clubManagement.internal.service.query.ClubManagementQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubRequestDTO.ClubMemberStatusFilter;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.common.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Tag(name = "독서 모임", description = "독서 모임 생성/수정/검색 API")
public class ClubController {

    private final ClubManagementQueryFacade clubManagementQueryFacade;
    private final ClubManagementQueryService clubManagementQueryService;
    private final ClubMemberCommandService clubMemberCommandService;
    private final ClubManagementCommandService clubManagementCommandService;

    @Operation(summary = "모임 이름 중복 확인", description = "입력한 모임 이름이 이미 사용 중이면 true, 아니면 false를 반환합니다.")
    @Parameters({
            @Parameter(name = "clubName", description = "중복 여부 확인할 모임 이름(최대 40자)", required = true, example = "독서모임A")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @GetMapping("/check-name")
    public ApiResponse<Boolean> checkClubNameDuplicate(
            @RequestParam @NotBlank(message = "clubName은 필수 입력입니다.") @Size(max = 40, message = "클럽 이름은 40자 이하로 입력해주세요.") String clubName
    ) {
        return ApiResponse.onSuccess(clubManagementQueryService.isDuplicateClubName(clubName));
    }

    @Operation(summary = "독서 모임 생성 API", description = "새로운 독서 모임을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복된 모임 이름입니다."),
    })
    @PostMapping
    public ApiResponse<String> createClub(
            @RequestBody @Valid ClubRequestDTO.ClubDetail request,
            @CurrentId String memberId
    ) {
        clubManagementCommandService.createClub(memberId, request);
        return ApiResponse.onSuccess("독서 모임이 정상적으로 생성되었습니다.");
    }

    @Operation(summary = "[운영진] 독서 모임 정보 수정", description = "지정한 클럽의 정보를 수정합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "수정할 독서클럽 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "독서클럽 운영진만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 독서클럽을 찾을 수 없습니다.")
    })
    @PutMapping("/{clubId}")
    public ApiResponse<String> updateClub(
            @PathVariable Long clubId,
            @RequestBody @Valid ClubRequestDTO.ClubDetail request,
            @CurrentId String memberId
    ) {
        clubManagementCommandService.updateClub(clubId, memberId, request);
        return ApiResponse.onSuccess("독서모임이 정상적으로 수정되었습니다.");
    }

    @Operation(summary = "[운영진] 독서 모임 상세 조회", description = "지정한 클럽의 상세 정보를 반환합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "조회할 독서클럽 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "독서클럽 운영진만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 독서클럽을 찾을 수 없습니다.")
    })
    @GetMapping("/{clubId}")
    public ApiResponse<ClubResponseDTO.ClubDetail> getClubDetail(
            @PathVariable Long clubId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubManagementQueryFacade.retrieveClubDetail(clubId, memberId));
    }

    @Operation(summary = "독서 모임 검색", description = "키워드와 필터 기반으로 독서 모임을 검색합니다.")
    @Parameters({
            @Parameter(name = "cursorId", description = "커서 기반 페이지네이션을 위한 마지막 독서 모임 ID", required = false, example = "10"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/search")
    public ApiResponse<ClubResponseDTO.ClubList> searchClubs(
            @ModelAttribute @ParameterObject ClubRequestDTO.ClubSearchFilter filter,
            @RequestParam(required = false) Long cursorId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubManagementQueryFacade.retrieveClubList(memberId, filter, cursorId));
    }

    @Operation(summary = "독서 모임 추천", description = "회원의 관심 카테고리를 기반으로 독서 모임을 추천합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/recommendations")
    public ApiResponse<ClubResponseDTO.ClubRecommendationList> recommendClubs(
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubManagementQueryFacade.recommend(memberId));
    }

    @Operation(summary = "독서 모임 홈 화면", description = "누구나 볼 수 있는 독서모임 홈 화면 정보를 제공합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "조회할 독서클럽 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
    })
    @GetMapping("/{clubId}/home")
    public ApiResponse<ClubResponseDTO.ClubDetail> getClubHome(
            @PathVariable Long clubId
    ) {
        return ApiResponse.onSuccess(clubManagementQueryFacade.retrieveClubHome(clubId));
    }

    @Operation(summary = "독서 모임 가입 신청", description = "독서 모임에 가입 신청을 합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "가입 신청할 독서클럽 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입 신청을 했거나, 가입이 승인된 상태입니다."),
    })
    @PostMapping("/{clubId}/join")
    public ApiResponse<String> joinClub(
            @PathVariable Long clubId,
            @RequestBody @Valid ClubRequestDTO.JoinClub request,
            @CurrentId String memberId
    ) {
        clubMemberCommandService.joinClub(clubId, memberId, request);
        return ApiResponse.onSuccess("독서 모임 가입 신청이 완료되었습니다.");
    }

    @Operation(summary = "클럽에서의 나의 상태 조회", description = "클럽에서 현재 로그인한 사용자의 멤버 상태를 반환합니다. 403 발생 시 클라이언트는 이 API를 호출하여 상태를 갱신할 수 있습니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "조회할 독서클럽 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
    })
    @GetMapping("/{clubId}/me")
    public ApiResponse<ClubResponseDTO.MyMembership> checkMyStatusInClub(
            @PathVariable Long clubId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubManagementQueryFacade.retrieveMyMembership(clubId, memberId));
    }

    @Operation(summary = "[운영진] 독서모임 회원 관리용 조회", description = "독서 모임의 회원을 관리하기 위해 회원 정보를 조회합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "조회할 독서 모임 ID", required = true, example = "1"),
            @Parameter(name = "status", description = """
                    멤버 상태 필터(어떤 멤버 상태의 멤버들을 조회할지 결정)
                    - ALL: 존재하는 모든 멤버 상태(MEMBER, STAFF, OWNER, PENDING, WITHDRAWN, KICKED)
                    - ACTIVE: 활동 중인 멤버 상태 (MEMBER, STAFF, OWNER)
                    - MEMBER: 일반 회원
                    - STAFF: 운영진
                    - OWNER: 소유자(최고 운영자)
                    - PENDING: 가입 대기 중인 회원
                    - WITHDRAWN: 탈퇴한 회원
                    - KICKED: 강제 탈퇴된 회원
                    """, required = true, example = "ACTIVE"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "독서클럽 운영진만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
    })
    @GetMapping("/{clubId}/members")
    public ApiResponse<ClubResponseDTO.ClubMemberList> getClubMembers(
            @PathVariable Long clubId,
            @RequestParam ClubMemberStatusFilter status,
            @RequestParam(required = false) Long cursorId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(
                clubManagementQueryFacade.retrieveClubMemberList(clubId, memberId, status, cursorId));
    }

    @Operation(summary = "[운영진] 독서 모임 회원 등급 수정", description = "독서 모임 회원의 등급을 수정합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "수정할 독서 모임 ID", required = true, example = "1"),
            @Parameter(name = "clubMemberId", description = "등급이 수정될 독서 모임 회원 ID", required = true, example = "10"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 사용할 수 있는 API입니다."),
    })
    @PatchMapping("/{clubId}/members/{clubMemberId}")
    public ApiResponse<String> updateClubMemberStatus(
            @PathVariable Long clubId,
            @PathVariable Long clubMemberId,
            @RequestBody @Valid ClubRequestDTO.ClubMemberStatusAction request,
            @CurrentId String memberId
    ) {
        clubMemberCommandService.updateClubMemberStatus(clubId, memberId, clubMemberId, request);
        return ApiResponse.onSuccess("독서 모임 회원 등급이 정상적으로 수정되었습니다.");
    }

    @Operation(summary = "독서 모임 탈퇴 API", description = "본인이 가입한 독서 모임에서 탈퇴합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "독서클럽장은 탈퇴할 수 없습니다."),
    })
    @DeleteMapping("/{clubId}/leave")
    public ApiResponse<String> leaveClub(
            @PathVariable Long clubId,
            @CurrentId String memberId
    ) {
        clubMemberCommandService.leaveClub(clubId, memberId);
        return ApiResponse.onSuccess("독서모임에서 탈퇴되었습니다.");
    }
}