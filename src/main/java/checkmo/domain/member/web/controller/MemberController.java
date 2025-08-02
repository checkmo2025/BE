package checkmo.domain.member.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.member.facade.MemberCommandFacade;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.global.auth.CurrentId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "회원", description = "마이페이지, 프로필 관리, 팔로우, 모임 관리, 알림 설정 관련 API")
public class MemberController {

    private final MemberCommandFacade memberCommandFacade;
    private final MemberQueryFacade memberQueryFacade;

    // 마이페이지 관련
    // GET /api/members/me - 마이페이지 조회
    // PATCH /api/members/me - 프로필 편집
    // DELETE /api/members/me - 탈퇴

    // 모임 관리 관련
    // GET /api/members/me/clubs?status=all - 모임관리 페이지
    // DELETE /api/clubs/{clubId}/members/me - 모임 탈퇴

    // 팔로우 관련
    // DELETE /api/members/{memberNickname}/follow - 팔로우 삭제
    // GET /api/members/me/follow - 내 팔로우 조회
    // GET /api/members/me/following - 내 팔로잉 조회
    @Operation(summary = "회원 팔로잉 API", description = "특정 회원을 팔로잉합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다.")
    })
    @PostMapping("/{memberNickname}/following")
    public ApiResponse<String> toggleFollow(
            @CurrentId String memberId,
            @PathVariable String memberNickname
    ) {
        memberCommandFacade.followMember(memberId, memberNickname);
        return ApiResponse.onSuccess(memberNickname + "님 팔로잉에 성공했습니다.");
    }

    // 알림 설정 관련
    // PATCH /api/members/me/notification-settings - 알림 설정

    // 다른 사람 프로필 관련
    // GET /api/members/{memberNickname} - 다른 사람 프로필 조회
}
