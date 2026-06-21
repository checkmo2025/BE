package checkmo.report.web.controller;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.report.internal.service.query.ReportQueryService;
import checkmo.report.web.dto.ReportResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "신고 (관리자)", description = "관리자 전용 신고 조회 API")
public class ReportAdminController {

    private final ReportQueryService reportQueryService;

    @Operation(
            summary = "특정 회원이 작성한 신고 목록 조회 (관리자)",
            description = "관리자가 특정 회원이 제출한 신고 목록을 커서 기반으로 조회합니다."
    )
    @Parameter(name = "nickname", description = "신고를 작성한 회원의 닉네임", required = true, example = "hy_0716")
    @Parameter(name = "cursorId", description = "마지막으로 조회한 신고 ID", example = "10")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 회원을 찾을 수 없습니다.")
    })
    @GetMapping("/{nickname}/reports")
    public ApiResponse<ReportResponseDTO.AdminMemberReportList> getMemberReportsForAdmin(
            @PathVariable String nickname,
            @RequestParam(required = false) Long cursorId
    ) {
        return ApiResponse.onSuccess(
                reportQueryService.retrieveMemberReportsForAdmin(nickname, cursorId)
        );
    }
}
