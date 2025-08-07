package checkmo.domain.club.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.club.facade.ClubCommandFacade;
import checkmo.domain.club.facade.ClubQueryFacade;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.global.auth.CurrentId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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

    private final ClubQueryFacade clubQueryFacade;
    private final ClubCommandFacade clubCommandFacade;

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


    /**
     * 모임 생성 API
     *
     * @param memberId 회원 ID (시큐리티 구현 후 삭제 예정)
     * @param request 클럽 생성에 필요한 정보(name, description, profileImageUrl, isOpen, category 등)
     * @return 생성된 클럽 정보를 포함한 성공 응답
     */
    @Operation(summary = "독서 모임 생성 API", description = "새로운 독서 모임을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 값이 유효하지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복된 모임 이름입니다."),
    })
    @PostMapping("")
    public ApiResponse<ClubResponseDTO.ClubDetailDTO> createClub(
            @RequestBody @Valid ClubRequestDTO.ClubDetailDTO request,
            @CurrentId String memberId
    ) {
        Long clubId = clubCommandFacade.createClub(memberId, request);
        ClubResponseDTO.ClubDetailDTO result = clubQueryFacade.getClubInfo(clubId, memberId);
        return ApiResponse.onSuccess(result);
    }

    /**
     * 독서 모임 검색 API
     *
     * @param keyword 검색할 키워드
     * @param region 지역 필터링 여부 (0: 선택 안함, 1: 선택해서 검색)
     * @param participants 대상 필터링 여부 (0: 선택 안함, 1: 선택해서 검색)
     * @param cursorId 페이징 커서 ID (null: 처음부터)
     * @return 검색 결과를 포함한 성공 응답
     */
    @Operation(summary = "독서 모임 검색 API", description = "키워드를 기반으로 독서 모임을 검색합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/search")
    public ApiResponse<ClubResponseDTO.ClubListDTO> searchClubs(
            @CurrentId String memberId,
            @RequestParam(required = false, defaultValue = "") String keyword, // 검색 키워드 (모임명 등)
            @RequestParam(required = false, defaultValue = "0") int region, // 지역 필터링 여부 (0: 선택 안함, 1: 선택해서 검색)
            @RequestParam(required = false, defaultValue = "0") int participants, // 대상 필터링 여부 (0: 선택 안함, 1: 선택해서 검색)
            @RequestParam(required = false) Long cursorId // 페이징 커서 ID
    ) {
        return ApiResponse.onSuccess(clubQueryFacade.getClubList(memberId, keyword, region, participants, cursorId));
    }

    /**
     * 사이드바 - 내가 가입한 클럽 목록 조회 API
     *
     * @return 가입한 클럽 목록과 성공 응답
     */
    @Operation(summary = "사이드바 - 내가 가입한 클럽 목록 API", description = "내가 가입한 클럽 목록을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/myClubs")
    public ApiResponse<ClubResponseDTO.MyClubListDTO> getMyClubs(
            @CurrentId String memberId
    ) {
        return ApiResponse.onSuccess(clubQueryFacade.getMyClubList(memberId));
    }

}