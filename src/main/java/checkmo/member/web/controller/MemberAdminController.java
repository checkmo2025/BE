package checkmo.member.web.controller;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.member.internal.service.MemberQueryFacade;
import checkmo.member.web.dto.MemberResponseDTO.AdminMemberDetailInfo;
import checkmo.member.web.dto.MemberResponseDTO.AdminMemberList;
import checkmo.member.web.dto.MemberResponseDTO.MemberEmailList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "회원 (관리자)", description = "회원 관리 API (관리자 전용)")
public class MemberAdminController {

    private final MemberQueryFacade memberQueryFacade;

    @Operation(summary = "회원 이메일 검색", description = "관리자 전용 자동완성용 이메일 검색 API입니다.")
    @Parameter(name = "keyword", description = "이메일 부분 검색어", required = false, example = "gmail")
    @Parameter(name = "limit", description = "최대 반환 개수(기본 20, 최대 100)", required = false, example = "20")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다.")
    })
    @GetMapping("/emails")
    public ApiResponse<MemberEmailList> getMemberEmails(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.onSuccess(memberQueryFacade.retrieveActiveEmailsForAdmin(keyword, limit));
    }

    @Operation(
            summary = "회원 목록 조회 (관리자)",
            description = "관리자 전용 회원 목록 조회 API입니다. 아이디 또는 이메일 검색과 페이지네이션을 지원합니다."
    )
    @Parameter(name = "page", description = "페이지 번호 (1부터 시작)", required = false, example = "1")
    @Parameter(name = "keyword", description = "회원 ID 또는 이메일 검색어", required = false, example = "yhi9839")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다.")
    })
    @GetMapping
    public ApiResponse<AdminMemberList> getMembersForAdmin(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.onSuccess(memberQueryFacade.retrieveMembersForAdmin(keyword, page));
    }

    @Operation(
            summary = "회원 상세 조회 (관리자)",
            description = "관리자 전용 회원 상세 기본 정보 조회 API입니다. 프로필, 이름, 이메일, 전화번호, 관심 카테고리 등을 조회합니다."
    )
    @Parameter(name = "memberNickName", description = "조회할 회원 닉네임", required = true)
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 회원을 찾을 수 없습니다.")
    })
    @GetMapping("/{memberNickName}")
    public ApiResponse<AdminMemberDetailInfo> getMemberDetailForAdmin(
            @PathVariable String memberNickName
    ) {
        return ApiResponse.onSuccess(memberQueryFacade.retrieveMemberDetailInfoForAdmin(memberNickName));
    }
}
