package checkmo.clubManagement.web.controller;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.service.command.ClubManagementCommandService;
import checkmo.clubManagement.internal.service.command.ClubMemberCommandService;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.common.apiPayload.ApiResponse;
import checkmo.member.internal.authAnnotation.CurrentId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "독서 모임", description = "독서 모임 생성, 검색, 가입, 기본 정보 관리 API")
public class ClubController {

    private final ClubManagementAPI clubManagementAPI;
    private final ClubMemberCommandService clubMemberCommandService;
    private final ClubManagementCommandService clubManagementCommandService;

    @Operation(summary = "모임 이름 중복 검사", description = "중복 여부 확인할 모임 이름을 전달하면 존재 여부를 반환합니다.")
    @Parameters({
            @Parameter(name = "clubName", description = "중복 여부 확인할 모임 이름", required = true, example = "독서모임A")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @GetMapping("/checkName")
    public ApiResponse<Boolean> checkClubNameDuplicate(
            @RequestParam String clubName
    ) {
        boolean isDuplicate = clubManagementAPI.isDuplicateClubName(clubName);
        return ApiResponse.onSuccess(isDuplicate);
    }

    @Operation(summary = "독서 모임 생성 API", description = "새로운 독서 모임을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 값이 유효하지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복된 모임 이름입니다."),
    })
    @PostMapping("")
    public ApiResponse<String> createClub(
            @RequestBody @Valid ClubRequestDTO.ClubDetailDTO request,
            @CurrentId String memberId
    ) {
        Long clubId = clubManagementCommandService.createClub(memberId, request);
        return ApiResponse.onSuccess("독서 모임(id:" + clubId + ")가 정상적으로 생성되었습니다.");
    }

    @Operation(summary = "독서 모임 상세 조회", description = "지정한 클럽의 상세 정보를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "클럽을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "조회 권한 없음")
    })
    @GetMapping("/{clubId}")
    public ApiResponse<ClubResponseDTO.ClubDetailDTO> getClubDetail(
            @PathVariable Long clubId,
            @CurrentId String memberId
    ) {
        ClubResponseDTO.ClubDetailDTO result = clubManagementAPI.getClubInfo(clubId, memberId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(
            summary = "독서 모임 정보 수정",
            description = """
                    지정한 클럽의 정보를 수정합니다.
                    
                    수정 가능 필드:
                      - name (모임 이름)
                      - description (모임 설명)
                      - profileImageUrl (프로필 이미지 URL)
                      - participantTypes (참여자 유형 목록)
                      - region (지역)
                      - insta (인스타그램 주소)
                      - kakao (카카오 오픈채팅 주소)
                    
                    수정 불가 필드:
                      - open (공개 여부)
                        - 요청 DTO에는 포함되지만, 서버에서는 해당 값을 무시하며 DB에 반영하지 않습니다.
                        - open 값 변경은 모임 생성 시에만 가능합니다.
                        - 피그마에 적힌 요구 사항을 참고한 것으로, 수정이 필요하면 말씀해주세요!
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 값이 유효하지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "클럽을 찾을 수 없음")
    })
    @PutMapping("/{clubId}")
    public ApiResponse<String> updateClub(
            @PathVariable Long clubId,
            @CurrentId String memberId,
            @RequestBody @Valid ClubRequestDTO.ClubDetailDTO request
    ) {
        Long updatedClubId = clubManagementCommandService.updateClub(clubId, memberId, request);
        return ApiResponse.onSuccess("독서모임(id:" + updatedClubId + ")이 정상적으로 수정되었습니다.");
    }

    @Operation(summary = "독서 모임 검색 API", description = "키워드를 기반으로 독서 모임을 검색합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/search")
    public ApiResponse<ClubResponseDTO.ClubListDTO> searchClubs(
            @CurrentId String memberId,
            @ModelAttribute ClubRequestDTO.ClubSearchFilter filter,
            @ModelAttribute ClubRequestDTO.CursorPageRequest pageRequest
    ) {
        return ApiResponse.onSuccess(clubManagementAPI.getClubList(memberId, filter, pageRequest));
    }

    @Operation(summary = "사이드바 - 내가 가입한 클럽 목록 API", description = "내가 가입한 클럽 목록을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/myClubs")
    public ApiResponse<ClubResponseDTO.MyClubListDTO> getMyClubs(
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubManagementAPI.getMyClubList(memberId));
    }

    @Operation(summary = "마이 페이지 - 내가 가입한 클럽 조회 API", description = "내가 가입한 클럽 목록을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/myPage")
    public ApiResponse<ClubResponseDTO.MyPageClubListDTO> getMyPageClubs(
            @CurrentId String memberId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(clubManagementAPI.getMyPageClubList(memberId, cursorId, size));
    }

    @Operation(summary = "독서 모임 가입 신청 API", description = "독서 모임에 가입 신청을 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입 신청을 했거나, 가입이 승인된 상태입니다."),
    })
    @PostMapping("/{clubId}/join")
    public ApiResponse<String> joinClub(
            @PathVariable Long clubId,
            @CurrentId String memberId,
            @RequestBody @Valid ClubRequestDTO.ClubMemberJoinDTO request
    ) {
        ClubMember joinedClubMember = clubMemberCommandService.joinClub(clubId, memberId, request);
        return ApiResponse.onSuccess("독서 모임 가입 신청이 완료되었습니다." + joinedClubMember.getId());
    }

    @Operation(summary = "독서 모임 회원 조회 API", description = "독서 모임의 회원 정보를 조회합니다.")
    @Parameters({
            @Parameter(
                    name = "status",
                    description = "조회할 회원 상태 (MEMBER, STAFF, PENDING, BLOCKED, ALL 중 선택)",
                    example = "ALL"
            )
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
    })
    @GetMapping("/{clubId}/members")
    public ApiResponse<ClubResponseDTO.ClubMemberListDTO> getClubMembers(
            @PathVariable Long clubId,
            @CurrentId String memberId,
            @RequestParam(defaultValue = "ALL") String status, // 상태별 필터링 (MEMBER, STAFF, PENDING, BLOCKED, ALL 중 선택)
            @RequestParam(required = false) Long cursorId, // 페이징을 위한 커서 ID
            @RequestParam(required = false) Integer size // 페이지 사이즈
    ) {
        return ApiResponse.onSuccess(
                clubManagementAPI.getClubMemberListByStatus(clubId, memberId, status, cursorId, size));
    }

    @Operation(summary = "독서 모임 회원 등급 수정 API", description = "독서 모임 회원의 등급을 수정합니다.")
    @Parameters({
            @Parameter(
                    name = "status",
                    description = "수정할 등급 (MEMBER, STAFF, PENDING, BLOCKED 중 선택)",
                    example = "STAFF"
            )
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 사용할 수 있는 API입니다."),
    })
    @PatchMapping("/{clubId}/members/{memberId}/status")
    public ApiResponse<String> updateClubMemberStatus(
            @PathVariable Long clubId,
            @PathVariable Long memberId,
            @CurrentId String currentMemberId,
            @RequestParam(defaultValue = "STAFF") String status // (MEMBER, STAFF, PENDING, BLOCKED 중 선택)
    ) {
        ClubMember updatedClubMember = clubMemberCommandService.updateClubMemberStatus(clubId, currentMemberId,
                memberId, status);
        return ApiResponse.onSuccess(updatedClubMember.getId() + "의 상태가 정상적으로 변경되었습니다.");
    }

    @Operation(summary = "독서 모임 탈퇴 API", description = "본인이 가입한 독서 모임에서 탈퇴합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인만 탈퇴할 수 있습니다."),
    })
    @DeleteMapping("/{clubId}/leave")
    public ApiResponse<String> leaveClub(
            @PathVariable Long clubId,
            @CurrentId String memberId
    ) {
        clubMemberCommandService.leaveClub(clubId, memberId);
        return ApiResponse.onSuccess("정상적으로 독서모임에서 탈퇴되었습니다.");
    }

    @Operation(summary = "클럽 스태프 여부 확인 API", description = "로그인한 회원이 해당 클럽의 스태프인지 확인합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "독서클럽 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
    })
    @GetMapping("/{clubId}/staff")
    public ApiResponse<Boolean> checkStaffStatus(
            @PathVariable Long clubId,
            @CurrentId String memberId
    ) {
        Boolean isStaff = clubManagementAPI.checkStaffStatus(clubId, memberId);
        return ApiResponse.onSuccess(isStaff);
    }
}