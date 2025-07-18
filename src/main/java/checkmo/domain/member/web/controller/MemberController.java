package checkmo.domain.member.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Tag(name = "회원", description = "마이페이지, 프로필 관리, 팔로우, 모임 관리, 알림 설정 관련 API")
public class MemberController {

    // 마이페이지 관련
    // GET /api/members/me - 마이페이지 조회
    // PATCH /api/members/me - 프로필 편집
    // DELETE /api/members/me - 탈퇴

    // 모임 관리 관련
    // GET /api/members/me/clubs?status=all - 모임관리 페이지
    // DELETE /api/clubs/{clubId}/members/me - 모임 탈퇴

    // 팔로우 관련
    // POST /api/members/{memberNickname}/follow - 팔로우 누르기
    // DELETE /api/members/{memberNickname}/follow - 팔로우 삭제
    // GET /api/members/me/follow - 내 팔로우 조회
    // GET /api/members/me/following - 내 팔로잉 조회

    // 알림 설정 관련
    // PATCH /api/members/me/notification-settings - 알림 설정

    // 캘린더/스케줄 관련 TODO: 어떻게 할지 확실하진 않음...!! ☠️
    // GET /api/calendar?year=2025&month=1&clubId=1 - 달력 보기
    // GET /api/schedule?year=2025&month=1 - 스케줄 조회

    // 다른 사람 프로필 관련
    // GET /api/members/{memberNickname} - 다른 사람 프로필 조회
}
