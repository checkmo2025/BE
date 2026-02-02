package checkmo.member.web.controller;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.member.internal.service.MemberQueryFacade;
import checkmo.member.web.dto.MemberResponseDTO.ReportList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "회원 (관리자)", description = "회원 관리 API (관리자 전용)")
public class MemberAdminController {

    private final MemberQueryFacade memberQueryFacade;

    @Operation(summary = "회원 신고 목록 조회", description = "특정 회원이 신고당한 목록을 조회합니다.")
    @Parameter(name = "memberNickname", description = "조회할 회원 닉네임", required = true)
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다.")
    })
    @GetMapping("/{memberNickname}/reports")
    public ApiResponse<ReportList> getMemberReports(
            @PathVariable String memberNickname
    ) {
        return ApiResponse.onSuccess(memberQueryFacade.retrieveReportsByMemberNickname(memberNickname));
    }
}