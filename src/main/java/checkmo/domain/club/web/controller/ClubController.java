package checkmo.domain.club.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.club.facade.ClubCommandFacade;
import checkmo.domain.club.facade.ClubQueryFacade;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
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
    @Parameters({
            @Parameter(name = "MemberId", description = "회원 ID (시큐리티 구현 후 삭제 예정)", required = true, example = "mem_001")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 값이 유효하지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복된 모임 이름입니다."),
    })
    @PostMapping("/")
    public ApiResponse<ClubResponseDTO.ClubDetailDTO> createClub(
            @RequestHeader("MemberId") String memberId,
            @RequestBody @Valid ClubRequestDTO.ClubDetailDTO request
    ) {
        // TODO - 로그인 된 사용자가 맞는지 검사하는 어노테이션 필요
        Long clubId = clubCommandFacade.createClub(memberId, request);
        ClubResponseDTO.ClubDetailDTO result = clubQueryFacade.getClubInfo(clubId, memberId);
        return ApiResponse.onSuccess(result);
    }


    // GET /api/clubs?keyword=독서&region=1&participants=1 - 독서 모임 조회 및 검색
    // POST /api/clubs/{clubId}/join - 독서 모임 가입 신청
    // GET /api/clubs/{clubId}/dashboard - 참여중인 Club 메인 화면

    // 회원 관리
    // GET /api/clubs/{clubId}/members?status=pending - 독서클럽 회원 조회하기 (상태별 필터링 가능)
    // PATCH /api/clubs/{clubId}/members/{memberId}/approve - 독서클럽 가입 승인하기 (운영진만)
    // PATCH /api/clubs/{clubId}/members/{memberId}/status - 독서클럽 회원 등급/상태 수정하기 (운영진만)
    // DELETE /api/clubs/{clubId}/members/me - 독서클럽 탈퇴하기 (본인)
}
