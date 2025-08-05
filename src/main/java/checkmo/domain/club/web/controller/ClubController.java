package checkmo.domain.club.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.club.facade.ClubCommandFacade;
import checkmo.domain.club.facade.ClubQueryFacade;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
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
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Tag(name = "독서 모임", description = "독서 모임 생성, 검색, 가입, 기본 정보 관리 API")
public class ClubController {

    private final ClubQueryFacade clubQueryFacade;
    private final ClubCommandFacade clubCommandFacade;

    /**
     * 모임 이름 중복 검사 API
     * @param clubName 중복 여부 확인할 모임 이름
     * @return true: 이미 존재하는 이름, false: 사용 가능한 이름
     */
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
        boolean isDuplicate = clubQueryFacade.isDuplicateClubName(clubName);
        return ApiResponse.onSuccess(isDuplicate);
    }


    /**
     * 모임 생성 API
     *
     * @param memberId 회원 ID (시큐리티 구현 후 삭제 예정)
     * @param request 클럽 생성에 필요한 정보(name, description, profileImageUrl, isOpen, category 등)
     * @return 생성된 클럽 정보를 포함한 성공 응답
     */
    @Operation(summary = "독서 모임 생성 API", description = "새로운 독서 모임을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 값이 유효하지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복된 모임 이름입니다."),
    })
    @PostMapping("")
    public ApiResponse<ClubResponseDTO.ClubDetailDTO> createClub(
            @RequestBody @Valid ClubRequestDTO.ClubDetailDTO request,
            @CurrentId String memberId
    ) {
        Long clubId = clubCommandFacade.createClub(memberId, request);
        ClubResponseDTO.ClubDetailDTO result = clubQueryFacade.getClubInfo(clubId, memberId);
        return ApiResponse.onSuccess(result);
    }

    /**
     * 독서클럽 회원 가입 신청 API
     *
     * @param clubId 독서 모임 ID
     * @return 가입 신청 결과를 포함한 성공 응답
     */
    @Operation(summary = "독서 모임 가입 신청 API", description = "독서 모임에 가입 신청을 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입 신청을 했거나, 가입이 승인된 상태입니다."),
    })
    @PostMapping("/{clubId}/join")
    public ApiResponse<ClubResponseDTO.ClubInfoDTO> joinClub(
            @PathVariable Long clubId,
            @CurrentId String memberId,
            @RequestBody @Valid ClubRequestDTO.ClubMemberJoinDTO request
    ) {
        return ApiResponse.onSuccess(clubCommandFacade.joinClub(clubId, memberId, request));
    }

    /**
     * 독서클럽 회원 조회하기 API (상태별 필터링 가능)
     *
     * @param clubId 독서 모임 ID
     * @param status 조회할 회원 상태 (MEMBER, STAFF, PENDING, BLOCKED, ALL 중 선택)
     * @param cursorId 페이징을 위한 커서 ID (선택 사항)
     * @return 독서 모임의 회원 정보를 포함한 성공 응답
     */
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
            @RequestParam(required = true, defaultValue = "ALL") String status, // 상태별 필터링 (MEMBER, STAFF, PENDING, BLOCKED, ALL 중 선택)
            @RequestParam(required = false) Long cursorId // 페이징을 위한 커서 ID
    ) {
        return ApiResponse.onSuccess(clubQueryFacade.getClubMemberListByStatus(clubId, memberId, status, cursorId));
    }

    /**
     * 독서 클럽 회원 등급 수정 API
     *
     * @param clubId 독서 모임 ID
     * @param memberId 수정할 회원 ID
     * @param status 수정할 등급 (MEMBER, STAFF, PENDING, BLOCKED 중 선택)
     * @return 수정된 회원 정보를 포함한 성공 응답
     */
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
    public ApiResponse<ClubResponseDTO.ClubMemberUpdateResponseDTO> updateClubMemberStatus(
            @PathVariable Long clubId,
            @PathVariable Long memberId,
            @CurrentId String currentMemberId,
            @RequestParam(required = true, defaultValue = "STAFF") String status // (MEMBER, STAFF, PENDING, BLOCKED 중 선택)
    ) {
        return ApiResponse.onSuccess(clubCommandFacade.updateClubMemberStatus(clubId, memberId, currentMemberId, status));
    }

    /**
     * 독서 모임 탈퇴하기 API
     *
     * @param clubId 탈퇴할 독서 모임 ID
     * @return 성공 응답
     */
    @Operation(summary = "독서 모임 탈퇴 API", description = "본인이 가입한 독서 모임에서 탈퇴합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인만 탈퇴할 수 있습니다."),
    })
    @DeleteMapping("/{clubId}/leave")
    public ApiResponse<Void> leaveClub(
            @PathVariable Long clubId,
            @CurrentId String memberId
    ) {
        clubCommandFacade.leaveClub(clubId, memberId);
        return ApiResponse.onSuccess(null);
    }

}
