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
    // GET /api/members/me/follow - 내 팔로우 조회
    // GET /api/members/me/following - 내 팔로잉 조회
    @Operation(summary = "회원 팔로잉 API", description = "특정 회원을 팔로잉합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다.")
    })
    @PostMapping("/{memberNickname}/following")
    public ApiResponse<String> following(
            @CurrentId String memberId,
            @PathVariable String memberNickname
    ) {
        memberCommandFacade.followingMember(memberId, memberNickname);
        return ApiResponse.onSuccess(memberNickname + "님 팔로잉에 성공했습니다.");
    }

    @Operation(summary = "회원 언팔로잉 API",
            description = "팔로잉 목록에서 특정 회원을 언팔로잉 합니다.\n"+
            "언팔로잉은 팔로잉 목록에서만 가능합니다. 팔로워 목록에서는 언팔로잉할 수 없습니다.\n"+
            "언팔로잉을 하면 해당 회원의 팔로워 목록에서도 제거됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다.")
    })
    @DeleteMapping("/{memberNickname}/following")
    public ApiResponse<String> unfollowing(
            @CurrentId String memberId,
            @PathVariable String memberNickname
    ) {
        memberCommandFacade.unfollowingMember(memberId, memberNickname);
        return ApiResponse.onSuccess(memberNickname + "님을 언팔로잉 하였습니다.");
    }

    @Operation(summary = "팔로잉 중 특정 회원 삭제 API",
            description = "나를 팔로잉하고 있는 특정 회원을 삭제합니다.\n"+
            "팔로워 삭제는 팔로워 목록에서만 가능합니다. 팔로잉 목록에서는 팔로워를 삭제할 수 없습니다.\n"+
            "팔로워를 삭제하면 해당 회운의 팔로잉 목록에서도 제거됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다.")
    })
    @DeleteMapping("/{memberNickname}/follower")
    public ApiResponse<String> deleteFollower(
            @CurrentId String memberId,
            @PathVariable String memberNickname
    ) {
        memberCommandFacade.deleteFollower(memberId, memberNickname);
        return ApiResponse.onSuccess(memberNickname + "님을 팔로워 목록에서 제거하였습니다.");
    }

    // 알림 설정 관련
    // PATCH /api/members/me/notification-settings - 알림 설정

    // 다른 사람 프로필 관련
    // GET /api/members/{memberNickname} - 다른 사람 프로필 조회
}
