package checkmo.member.web.controller;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.member.internal.converter.TermsConverter;
import checkmo.member.internal.service.query.MemberTermsQueryService;
import checkmo.member.web.dto.TermsResponseDTO.PublicTermsList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
@Tag(name = "약관", description = "약관 조회 API")
public class TermsController {

    private final MemberTermsQueryService memberTermsQueryService;

    @Operation(summary = "활성 약관 목록 조회", description = "현재 활성화된 약관을 표시 순서대로 조회합니다.")
    @GetMapping
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "활성 약관 종류 중복 시 TERMS_500")
    })
    public ApiResponse<PublicTermsList> getActiveTerms() {
        return ApiResponse.onSuccess(TermsConverter.toPublicTermsList(
                memberTermsQueryService.retrieveActiveTerms()
        ));
    }
}
