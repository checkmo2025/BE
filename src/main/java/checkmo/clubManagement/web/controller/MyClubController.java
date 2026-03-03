package checkmo.clubManagement.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.clubManagement.internal.service.ClubManagementQueryFacade;
import checkmo.clubManagement.web.dto.myClub.MyClubResponseDTO;
import checkmo.common.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/me/clubs")
@RequiredArgsConstructor
@Tag(name = "내 독서 모임", description = "내가 가입한 독서 모임 관련 API")
public class MyClubController {

    private final ClubManagementQueryFacade clubManagementQueryFacade;

    @Operation(summary = "내가 가입한 클럽 목록 전체 조회", description = "내가 가입한 클럽 목록을 반환합니다.(단 가입 신청 상태/차단 상태는 제외) - 홈화면, 모임검색 화면, 마이페이지에서 다양하게 사용할 예정")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping
    public ApiResponse<MyClubResponseDTO.MyClubList> getMyClubs(
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubManagementQueryFacade.retrieveMyClubList(memberId));
    }

}
