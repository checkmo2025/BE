package checkmo.domain.club.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.club.service.command.ClubManagementCommandService;
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
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Tag(name = "독서 모임", description = "독서 모임 생성, 검색, 가입, 기본 정보 관리 API")
public class ClubController {

    private final ClubManagementCommandService clubManagementCommandService;

    @Operation(
            summary = "독서 모임 생성 API",
            description = "독서 모임 정보를 입력받아 새로운 모임을 생성합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "모임 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "정보를 찾을 수 없음")
    })
    @PostMapping
    public ApiResponse<ClubResponseDTO.ClubInfoDTO> createClub(
            @Parameter(
                    name = "MemberId",
                    description = "회원 ID (시큐리티 구현 후 삭제 예정)",
                    required = true,
                    example = "117"
            )
            @RequestHeader("MemberId") String memberId,

            @Valid
            @RequestBody ClubRequestDTO.ClubDetailDTO request
    ) {
        Long clubId = clubManagementCommandService.createClub(memberId, request);

        ClubResponseDTO.ClubInfoDTO clubInfoDTO = ClubResponseDTO.ClubInfoDTO.builder()
                .clubId(clubId)
                .clubName(null)
                .isOpen(null)
                .build();

        return ApiResponse.onSuccess(clubInfoDTO);
    }

}
