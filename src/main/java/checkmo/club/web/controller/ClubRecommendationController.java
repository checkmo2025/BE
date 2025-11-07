package checkmo.club.web.controller;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.club.facade.ClubCommandFacade;
import checkmo.club.facade.ClubQueryFacade;
import checkmo.club.web.dto.club.ClubRequestDTO;
import checkmo.club.web.dto.club.ClubResponseDTO;
import checkmo.global.auth.CurrentId;
import io.swagger.v3.oas.annotations.Operation;
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
    private final ClubQueryFacade clubQueryFacade;

    @Operation(summary = "추천 책 작성", description = "특정 모임에 추천 책을 작성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 값이 유효하지 않음")
    })
    @PostMapping
    public ApiResponse<ClubResponseDTO.BookRecommendDetailDTO> createRecommendation(
            @PathVariable Long clubId,
            @CurrentId String memberId,
            @RequestBody @Valid ClubRequestDTO.CreateBookRecommendDTO request
    ) {
        return ApiResponse.onSuccess(clubCommandFacade.recommendBook(clubId, memberId, request));
    }

    @Operation(summary = "추천 책 수정", description = "추천 책의 소개 이유, 별점, 태그를 수정합니다. 책 자체는 변경할 수 없습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "추천 책 또는 클럽을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "작성자가 아님")
    })
    @PatchMapping("/{recommendId}")
    public ApiResponse<ClubResponseDTO.BookRecommendDetailDTO> updateRecommendation(
            @PathVariable Long clubId,
            @PathVariable Long recommendId,
            @CurrentId String memberId,
            @RequestBody @Valid ClubRequestDTO.UpdateBookRecommendDTO request
    ) {
        return ApiResponse.onSuccess(clubCommandFacade.updateBookRecommend(clubId, memberId, recommendId, request));
    }

    @Operation(summary = "추천 책 전체 조회", description = "해당 독서모임의 추천 책 목록을 커서 기반으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "클럽을 찾을 수 없음")
    })
    @GetMapping
    public ApiResponse<ClubResponseDTO.BookRecommendListDTO> getAllRecommendations(
            @PathVariable Long clubId,
            @RequestParam(required = false) Long cursorId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubQueryFacade.getRecommendedBooks(clubId, cursorId, memberId));
    }

    @Operation(summary = "추천 책 상세 조회", description = "추천 책 ID를 기반으로 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "추천 책 또는 클럽을 찾을 수 없음")
    })
    @GetMapping("/{recommendId}")
    public ApiResponse<ClubResponseDTO.BookRecommendDetailDTO> getRecommendationDetail(
            @PathVariable Long clubId,
            @PathVariable Long recommendId,
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubQueryFacade.getRecommendedBookDetail(clubId, recommendId, memberId));
    }

    @Operation(summary = "추천 책 삭제", description = "추천 책을 삭제합니다. 작성자만 삭제할 수 있습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "작성자가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "추천 책 또는 클럽을 찾을 수 없음")
    })
    @DeleteMapping("/{recommendId}")
    public ApiResponse<Void> deleteRecommendation(
            @PathVariable Long clubId,
            @PathVariable Long recommendId,
            @CurrentId String memberId
    ) {
        clubCommandFacade.deleteRecommendedBook(clubId, memberId, recommendId);
        return ApiResponse.onSuccess(null);
    }


}
