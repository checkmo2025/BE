package checkmo.club.web.controller;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.club.facade.ClubCommandFacade;
import checkmo.club.facade.ClubQueryFacade;
import checkmo.club.validation.validCursor.ValidCursor;
import checkmo.club.validation.validSize.ValidSize;
import checkmo.club.web.dto.club.ClubRequestDTO;
import checkmo.club.web.dto.club.ClubResponseDTO;
import checkmo.global.auth.CurrentId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Tag(name = "독서 모임", description = "독서 모임 생성, 검색, 가입, 기본 정보 관리 API")
public class ClubController {

    private final ClubQueryFacade clubQueryFacade;
    private final ClubCommandFacade clubCommandFacade;


    @Operation(summary = "회원의 공지사항 목록 조회 (미팅, 투표, 공지 모두 포함)", description = "회원의 공지사항 목록을 조회합니다. onlyImportant=true 면 중요 공지사항만 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음")
    })
    @GetMapping("/notices")
    public ApiResponse<ClubResponseDTO.MemberNoticeListDTO> getMemberNoticeList(
            @CurrentId String memberId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false, defaultValue = "false") boolean onlyImportant,
            @RequestParam(required = false) Integer size // 페이지 사이즈
    ) {
        return ApiResponse.onSuccess(clubQueryFacade.getNoticeForHome(memberId, cursorId, onlyImportant, size));
    }

    /**
     * 모임 이름 중복 검사 API
     *
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
     * 독서 모임 상세 조회 API
     *
     * @param clubId   조회할 클럽 ID
     * @param memberId 현재 로그인한 회원 ID
     * @return 클럽 상세 정보
     */
    @Operation(summary = "독서 모임 상세 조회", description = "지정한 클럽의 상세 정보를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "클럽을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "조회 권한 없음")
    })
    @GetMapping("/{clubId}")
    public ApiResponse<ClubResponseDTO.ClubDetailDTO> getClubDetail(
            @PathVariable Long clubId,
            @CurrentId String memberId
    ) {
        ClubResponseDTO.ClubDetailDTO result = clubQueryFacade.getClubInfo(clubId, memberId);
        return ApiResponse.onSuccess(result);
    }

    /**
     * 독서 모임 업데이트 API
     *
     * @param clubId   수정할 클럽 ID
     * @param memberId 현재 로그인한 회원 ID
     * @param request  수정할 클럽 정보(name, description, profileImageUrl, isOpen, category 등)
     * @return 수정된 클럽 상세 정보
     */
    @Operation(
            summary = "독서 모임 정보 수정",
            description = """
            지정한 클럽의 정보를 수정합니다.
            
            수정 가능 필드:
              - name (모임 이름)
              - description (모임 설명)
              - profileImageUrl (프로필 이미지 URL)
              - participantTypes (참여자 유형 목록)
              - region (지역)
              - insta (인스타그램 주소)
              - kakao (카카오 오픈채팅 주소)
            
            수정 불가 필드:
              - open (공개 여부)
                - 요청 DTO에는 포함되지만, 서버에서는 해당 값을 무시하며 DB에 반영하지 않습니다.
                - open 값 변경은 모임 생성 시에만 가능합니다.
                - 피그마에 적힌 요구 사항을 참고한 것으로, 수정이 필요하면 말씀해주세요!
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 값이 유효하지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "클럽을 찾을 수 없음")
    })
    @PutMapping("/{clubId}")
    public ApiResponse<ClubResponseDTO.ClubDetailDTO> updateClub(
            @PathVariable Long clubId,
            @CurrentId String memberId,
            @RequestBody @Valid ClubRequestDTO.ClubDetailDTO request
    ) {
        clubCommandFacade.updateClub(clubId, memberId, request);
        ClubResponseDTO.ClubDetailDTO result = clubQueryFacade.getClubInfo(clubId, memberId);
        return ApiResponse.onSuccess(result);
    }

    /**
     * 독서 모임 검색 API
     *
     * @param memberId 현재 로그인한 회원 ID
     * @param filter 검색 필터 (keyword, name, region, participants)
     * @param pageRequest 페이징 요청 (cursorId, size)
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
            @ModelAttribute ClubRequestDTO.ClubSearchFilter filter,
            @ModelAttribute ClubRequestDTO.CursorPageRequest pageRequest
    ) {
        return ApiResponse.onSuccess(clubQueryFacade.getClubList(memberId, filter, pageRequest));
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

    /**
     * 마이 페이지 - 내가 가입한 클럽 조회 API
     */
    @Operation(summary = "마이 페이지 - 내가 가입한 클럽 조회 API", description = "내가 가입한 클럽 목록을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/myPage")
    public ApiResponse<ClubResponseDTO.MyPageClubListDTO> getMyPageClubs(
            @CurrentId String memberId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(clubQueryFacade.getMyPageClubList(memberId, cursorId, size));
    }

    /**
     * 독서클럽 회원 가입 신청 API
     *
     * @param clubId 독서 모임 ID
     * @return 가입 신청 결과를 포함한 성공 응답
     */
    @Operation(summary = "독서 모임 가입 신청 API", description = "독서 모임에 가입 신청을 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입 신청을 했거나, 가입이 승인된 상태입니다."),
    })
    @PostMapping("/{clubId}/join")
    public ApiResponse<Long> joinClub(
            @PathVariable Long clubId,
            @CurrentId String memberId,
            @RequestBody @Valid ClubRequestDTO.ClubMemberJoinDTO request
    ) {
        return ApiResponse.onSuccess(clubCommandFacade.joinClub(clubId, memberId, request));
    }

    /**
     * 독서클럽 회원 조회하기 API (상태별 필터링 가능)
     *
     * @param clubId 독서 모임 ID
     * @param status 조회할 회원 상태 (MEMBER, STAFF, PENDING, BLOCKED, ALL 중 선택)
     * @param cursorId 페이징을 위한 커서 ID (선택 사항)
     * @return 독서 모임의 회원 정보를 포함한 성공 응답
     */
    @Operation(summary = "독서 모임 회원 조회 API", description = "독서 모임의 회원 정보를 조회합니다.")
    @Parameters({
            @Parameter(
                    name = "status",
                    description = "조회할 회원 상태 (MEMBER, STAFF, PENDING, BLOCKED, ALL 중 선택)",
                    example = "ALL"
            )
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
    })
    @GetMapping("/{clubId}/members")
    public ApiResponse<ClubResponseDTO.ClubMemberListDTO> getClubMembers(
            @PathVariable Long clubId,
            @CurrentId String memberId,
            @RequestParam(defaultValue = "ALL") String status, // 상태별 필터링 (MEMBER, STAFF, PENDING, BLOCKED, ALL 중 선택)
            @RequestParam(required = false) @ValidCursor Long cursorId, // 페이징을 위한 커서 ID
            @RequestParam(required = false) @ValidSize Integer size // 페이지 사이즈
    ) {
        return ApiResponse.onSuccess(clubQueryFacade.getClubMemberListByStatus(clubId, memberId, status, cursorId, size));
    }

    /**
     * 독서 클럽 회원 등급 수정 API
     *
     * @param clubId 독서 모임 ID
     * @param memberId 수정할 회원 ID
     * @param status 수정할 등급 (MEMBER, STAFF, PENDING, BLOCKED 중 선택)
     * @return 수정된 회원 정보를 포함한 성공 응답
     */
    @Operation(summary = "독서 모임 회원 등급 수정 API", description = "독서 모임 회원의 등급을 수정합니다.")
    @Parameters({
            @Parameter(
                    name = "status",
                    description = "수정할 등급 (MEMBER, STAFF, PENDING, BLOCKED 중 선택)",
                    example = "STAFF"
            )
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "운영진만 사용할 수 있는 API입니다."),
    })
    @PatchMapping("/{clubId}/members/{memberId}/status")
    public ApiResponse<ClubResponseDTO.ClubMemberUpdateResponseDTO> updateClubMemberStatus(
            @PathVariable Long clubId,
            @PathVariable Long memberId,
            @CurrentId String currentMemberId,
            @RequestParam(defaultValue = "STAFF") String status // (MEMBER, STAFF, PENDING, BLOCKED 중 선택)
    ) {
        return ApiResponse.onSuccess(
                clubCommandFacade.updateClubMemberStatus(clubId, currentMemberId, memberId, status));
    }

    /**
     * 독서 모임 탈퇴하기 API
     *
     * @param clubId 탈퇴할 독서 모임 ID
     * @return 성공 응답
     */
    @Operation(summary = "독서 모임 탈퇴 API", description = "본인이 가입한 독서 모임에서 탈퇴합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 독서 모임입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인만 탈퇴할 수 있습니다."),
    })
    @DeleteMapping("/{clubId}/leave")
    public ApiResponse<Void> leaveClub(
            @PathVariable Long clubId,
            @CurrentId String memberId
    ) {
        clubCommandFacade.leaveClub(clubId, memberId);
        return ApiResponse.onSuccess(null);
    }

    /**
     * 독서 모임의 스태프인지 여부 확인하는 API
     *
     * @param clubId 독서 모임 ID
     * @param memberId 현재 로그인한 회원 ID
     * @return 스태프 여부 (true: 스태프, false: 일반 회원)
     */
    @Operation(summary = "클럽 스태프 여부 확인 API", description = "로그인한 회원이 해당 클럽의 스태프인지 확인합니다.")
    @Parameters({
            @Parameter(name = "clubId", description = "독서클럽 ID", required = true, example = "1"),
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 클럽의 회원이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서클럽을 찾을 수 없습니다."),
    })
    @GetMapping("/{clubId}/staff")
    public ApiResponse<Boolean> checkStaffStatus(
            @PathVariable Long clubId,
            @CurrentId String memberId
    ) {
        Boolean isStaff = clubQueryFacade.checkStaffStatus(clubId, memberId);
        return ApiResponse.onSuccess(isStaff);
    }
}