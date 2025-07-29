package checkmo.domain.club.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.club.facade.ClubCommandFacade;
import checkmo.domain.club.facade.ClubQueryFacade;
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
@RequestMapping("/api/clubs/{clubId}/notices")
@RequiredArgsConstructor
@Tag(name = "모임 공지사항", description = "독서 모임 공지사항, 투표 생성 및 관리 API")
public class ClubNoticeController {

    private final ClubCommandFacade clubCommandFacade;
    private final ClubQueryFacade clubQueryFacade;

    @Operation(summary = "공지사항 작성", description = "특정 모임에 공지사항을 작성합니다. (운영진만 작성 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 작성 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음")
    })
    @PostMapping("")
    public ApiResponse<ClubResponseDTO.ClubNoticeDetailDTO> createNotice(
            @PathVariable Long clubId,
            @Parameter(name = "MemberId", description = "회원 ID (시큐리티 구현 후 삭제 예정)", required = true, example = "mem_001")
            @RequestHeader("MemberId") String memberId,
            @RequestBody @Valid ClubRequestDTO.CreateClubNoticeDTO request
    ) {
        // TODO - 로그인 된 사용자가 맞는지 검사하는 어노테이션 필요
        return ApiResponse.onSuccess(clubCommandFacade.createNotice(clubId, memberId, request));
    }


}
