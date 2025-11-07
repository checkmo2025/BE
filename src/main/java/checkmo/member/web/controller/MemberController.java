package checkmo.member.web.controller;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.member.facade.MemberCommandFacade;
import checkmo.member.facade.MemberQueryFacade;
import checkmo.member.web.dto.MemberRequestDTO;
import checkmo.member.web.dto.MemberResponseDTO;
import checkmo.member.auth_annotation.CurrentId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    // 알림 설정 관련
    // PATCH /api/members/me/notification-settings - 알림 설정

    // 다른 사람 프로필 관련
    // GET /api/members/{memberNickname} - 다른 사람 프로필 조회

    // 팔로우 관련
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
            "팔로워를 삭제하면 해당 회원의 팔로잉 목록에서도 제거됩니다.")
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

    @Operation(summary = "팔로잉 목록 조회 API", description = "특정 회원의 팔로잉 목록을 조회합니다.")
    @Parameter(name = "cursorId", description = "커서 ID (페이징을 위한 커서, 처음에는 null)", required = false, example = "10")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다.")
    })
    @GetMapping("/me/following")
    public ApiResponse<MemberResponseDTO.FollowList> getFollowingList(
            @CurrentId String memberId,
            @RequestParam(required = false) Long cursorId
    ) {
        var followingList = memberQueryFacade.getFollowingList(memberId, cursorId);
        return ApiResponse.onSuccess(followingList);
    }

    @Operation(summary = "팔로워 목록 조회 API", description = "특정 회원의 팔로워 목록을 조회합니다.")
    @Parameter(name = "cursorId", description = "커서 ID (페이징을 위한 커서, 처음에는 null)", required = false, example = "10")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다.")
    })
    @GetMapping("/me/follower")
    public ApiResponse<MemberResponseDTO.FollowList> getFollowerList(
            @CurrentId String memberId,
            @RequestParam(required = false) Long cursorId
    ) {
        var followerList = memberQueryFacade.getFollowerList(memberId, cursorId);
        return ApiResponse.onSuccess(followerList);
    }

    @Operation(summary = "내 프로필 편집 API", description = "내 프로필을 편집합니다. 프로필 이미지, 소개, 관심 카테고리를 수정할 수 있습니다.")
    @PatchMapping("/me")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "프로필이 완성되지 않은 회원입니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 회원을 찾을 수 없습니다.")
    })
    public ApiResponse<MemberResponseDTO.MemberProfileWithCategoryResponseDTO> updateMemberProfile(
            @CurrentId String memberId,
            @RequestBody MemberRequestDTO.MemberProfileUpdateRequestDTO request
    ) {
        return ApiResponse.onSuccess(memberCommandFacade.updateMemberProfile(memberId, request));
    }

    @Operation(summary = "내 프로필 조회 API", description = "내 프로필 정보(관심 카테고리 정보 포함)를 조회합니다.")
    @GetMapping("/me")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "프로필이 완성되지 않은 회원입니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 회원을 찾을 수 없습니다.")
    })
    public ApiResponse<MemberResponseDTO.MemberProfileWithCategoryResponseDTO> getMemberProfile(
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(memberQueryFacade.getMemberProfile(memberId));
    }

    @Operation(summary = "다른 사람 프로필 조회 API", description = "다른 사람의 프로필 정보를 조회합니다. 프로필 이미지, 닉네임, 소개, 관심 카테고리, 팔로우 상태를 포함합니다.\n" +
            "책 이야기 목록은 별도 API(GET /api/book-stories?scope=TARGET&targetMemberNickname={닉네임})를 통해 조회해야 합니다.")
    @GetMapping("/{memberNickname}")
    public ApiResponse<MemberResponseDTO.otherProfileResponseDTO> getOtherProfile(
            @CurrentId String memberId,
            @PathVariable String memberNickname
    ) {
        return ApiResponse.onSuccess(memberQueryFacade.getOtherProfile(memberNickname, memberId));
    }
}