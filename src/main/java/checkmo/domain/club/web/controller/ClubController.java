package checkmo.domain.club.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.club.facade.ClubQueryFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Tag(name = "독서 모임", description = "독서 모임 생성, 검색, 가입, 기본 정보 관리 API")
public class ClubController {

    private final ClubQueryFacade clubQueryFacade;

    /**
     * 모임 이름 중복 검사 API
     * @param clubName 중복 여부 확인할 모임 이름
     * @return true: 이미 존재하는 이름, false: 사용 가능한 이름
     */
    @Operation(summary = "모임 이름 중복 검사", description = "중복 여부 확인할 모임 이름을 전달하면 존재 여부를 반환합니다.")
    @Parameters({
            @Parameter(name = "clubName", description = "중복 여부 확인할 모임 이름", required = true, example = "독서모임A")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @GetMapping("/checkName")
    public ApiResponse<Boolean> checkClubNameDuplicate(
            @RequestParam String clubName
    ) {
        boolean isDuplicate = clubQueryFacade.isDuplicateClubName(clubName);
        return ApiResponse.onSuccess(isDuplicate);
    }


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
