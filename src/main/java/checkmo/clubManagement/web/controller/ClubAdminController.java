package checkmo.clubManagement.web.controller;

import checkmo.clubManagement.internal.service.ClubManagementQueryFacade;
import checkmo.clubManagement.web.dto.admin.ClubAdminResponseDTO;
import checkmo.common.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/clubs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "모임 (관리자)", description = "모임 관리 API (관리자 전용)")
public class ClubAdminController {

    private final ClubManagementQueryFacade clubManagementQueryFacade;

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
}
