package checkmo.domain.club.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.club.facade.ClubCommandFacade;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clubs/{clubId}/recommendations")
@RequiredArgsConstructor
@Tag(name = "모임 추천 책", description = "독서 모임 내 책 추천 및 관리 API")

public class ClubRecommendationController {

    private final ClubCommandFacade clubCommandFacade;

    @Operation(summary = "추천 책 작성", description = "특정 모임에 추천 책을 작성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 값이 유효하지 않음")
    })
    @PostMapping
    public ApiResponse<ClubResponseDTO.BookRecommendDetailDTO> createRecommendation(
            @PathVariable Long clubId,
            @Parameter(name = "MemberId", description = "회원 ID (시큐리티 구현 후 삭제 예정)", required = true, example = "mem_001")
            @RequestHeader("MemberId") String memberId,
            @RequestBody @Valid ClubRequestDTO.CreateBookRecommendDTO request
    ) {
        // TODO - 로그인 된 사용자가 맞는지 검사하는 어노테이션 필요
        return ApiResponse.onSuccess(clubCommandFacade.recommendBook(clubId, memberId, request));
    }

}