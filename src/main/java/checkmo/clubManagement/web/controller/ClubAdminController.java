package checkmo.clubManagement.web.controller;

import checkmo.clubManagement.internal.service.ClubManagementQueryFacade;
import checkmo.clubManagement.internal.service.command.ClubManagementCommandService;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.clubManagement.web.dto.admin.ClubAdminResponseDTO;
import checkmo.common.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/clubs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "모임 (관리자)", description = "모임 관리 API (관리자 전용)")
public class ClubAdminController {

    private final ClubManagementQueryFacade clubManagementQueryFacade;
    private final ClubManagementCommandService clubManagementCommandService;

    @Operation(summary = "모임 검색 및 조회", description = "모임명을 기준으로 모임 목록을 조회합니다. 만약 검색어가 없다면(\" \", \"\", null) 전체 모임 목록을 조회합니다.")
    @Parameters({
            @Parameter(name = "keyword", description = "모임명 검색어", required = false, example = "북"),
            @Parameter(name = "page", description = "페이지 번호 (1부터 시작)", required = false, example = "1"),
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다.")
    })
    @GetMapping
    public ApiResponse<ClubAdminResponseDTO.ClubPreviewList> getClubList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ApiResponse.onSuccess(clubManagementQueryFacade.retrieveAdminClubList(keyword, page));
    }

    @Operation(summary = "모임 상세 조회", description = "관리자 전용 모임 상세 조회 API입니다.")
    @Parameter(name = "clubId", description = "조회할 모임 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없습니다.")
    })
    @GetMapping("/{clubId}")
    public ApiResponse<ClubResponseDTO.ClubDetail> getClubDetail(
            @PathVariable Long clubId
    ) {
        return ApiResponse.onSuccess(
                clubManagementQueryFacade.retrieveAdminClubDetail(clubId)
        );
    }

    @Operation(summary = "모임 수정", description = "관리자 전용 모임 수정 API입니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "수정할 모임 ID", required = true, example = "1")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없습니다.")
    })
    @PutMapping("/{clubId}")
    public ApiResponse<String> updateClub(
            @PathVariable Long clubId,
            @RequestBody @Valid ClubRequestDTO.ClubDetail request
    ) {
        clubManagementCommandService.updateClubByAdmin(clubId, request);
        return ApiResponse.onSuccess("모임이 정상적으로 수정되었습니다.");
    }
}
