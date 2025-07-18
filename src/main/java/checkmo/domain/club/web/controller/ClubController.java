package checkmo.domain.club.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Tag(name = "독서 모임", description = "독서 모임 생성, 검색, 가입, 기본 정보 관리 API")
public class ClubController {

    // 모임 기본 관리
    // POST /api/clubs - Club 생성
    // GET /api/clubs?keyword=독서&region=1&participants=1 - 독서 모임 조회 및 검색
    // POST /api/clubs/{clubId}/join - 독서 모임 가입 신청
    // GET /api/clubs/{clubId}/dashboard - 참여중인 Club 메인 화면

    // 회원 관리
    // GET /api/clubs/{clubId}/members?status=pending - 독서클럽 회원 조회하기 (상태별 필터링 가능)
    // PATCH /api/clubs/{clubId}/members/{memberId}/approve - 독서클럽 가입 승인하기 (운영진만)
    // PATCH /api/clubs/{clubId}/members/{memberId}/status - 독서클럽 회원 등급/상태 수정하기 (운영진만)
    // DELETE /api/clubs/{clubId}/members/me - 독서클럽 탈퇴하기 (본인)
}
