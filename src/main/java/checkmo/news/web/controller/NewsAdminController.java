package checkmo.news.web.controller;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.news.internal.service.NewsQueryFacade;
import checkmo.news.internal.service.command.NewsCommandService;
import checkmo.news.web.dto.NewsRequestDTO;
import checkmo.news.web.dto.NewsResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/news")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "소식 (관리자)", description = "소식 등록, 수정, 삭제 API (관리자 전용)")
public class NewsAdminController {

    private final NewsCommandService newsCommandService;
    private final NewsQueryFacade newsQueryFacade;

    @Operation(summary = "소식 목록 조회 (관리자)", description = "모든 소식 목록을 조회합니다. (게시 기간 관계없이, 페이지당 12개)")
    @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", required = false, example = "0")
    @Parameter(name = "keyword", description = "소식 제목 검색어", required = false, example = "한강공원")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다.")
    })
    @GetMapping
    public ApiResponse<NewsResponseDTO.AdminNewsList> getNewsListForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword
    ) {
        NewsResponseDTO.AdminNewsList newsList = newsQueryFacade.fetchNewsListForAdmin(keyword, page);
        return ApiResponse.onSuccess(newsList);
    }

    @Operation(summary = "소식 상세 조회 (관리자)", description = "소식의 모든 상세 정보를 조회합니다.")
    @Parameter(name = "newsId", description = "조회할 소식 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "소식을 찾을 수 없습니다.")
    })
    @GetMapping("/{newsId}")
    public ApiResponse<NewsResponseDTO.AdminDetailInfo> getNewsForAdmin(@PathVariable Long newsId) {
        NewsResponseDTO.AdminDetailInfo detailInfo = newsQueryFacade.fetchNewsDetailForAdmin(newsId);
        return ApiResponse.onSuccess(detailInfo);
    }

    @Operation(summary = "소식 등록", description = "관리자만 소식을 등록할 수 있습니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다.")
    })
    @PostMapping
    public ApiResponse<Long> createNews(@Valid @RequestBody NewsRequestDTO.CreateNews request) {
        Long newsId = newsCommandService.createNews(request);
        return ApiResponse.onSuccess(newsId);
    }

    @Operation(summary = "소식 수정", description = "관리자만 소식을 수정할 수 있습니다.")
    @Parameter(name = "newsId", description = "수정할 소식 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "소식을 찾을 수 없습니다.")
    })
    @PatchMapping("/{newsId}")
    public ApiResponse<Long> updateNews(
            @PathVariable Long newsId,
            @Valid @RequestBody NewsRequestDTO.UpdateNews request
    ) {
        Long updatedNewsId = newsCommandService.updateNews(newsId, request);
        return ApiResponse.onSuccess(updatedNewsId);
    }

    @Operation(summary = "소식 삭제", description = "관리자만 소식을 삭제할 수 있습니다.")
    @Parameter(name = "newsId", description = "삭제할 소식 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "소식을 찾을 수 없습니다.")
    })
    @DeleteMapping("/{newsId}")
    public ApiResponse<String> deleteNews(@PathVariable Long newsId) {
        newsCommandService.deleteNews(newsId);
        return ApiResponse.onSuccess("소식이 성공적으로 삭제되었습니다.");
    }

    @Operation(
            summary = "특정 회원 등록 소식 조회 (관리자)",
            description = "관리자가 특정 회원이 등록한 소식 목록을 조회합니다. 커서 기반 무한 스크롤을 지원합니다."
    )
    @io.swagger.v3.oas.annotations.Parameters({
            @Parameter(name = "memberNickname", description = "조회할 회원 닉네임", required = true, example = "hy_0716"),
            @Parameter(name = "cursorId", description = "커서 ID (처음에는 null)", required = false, example = "1")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 회원을 찾을 수 없습니다.")
    })
    @GetMapping("/members/{memberNickname}")
    public ApiResponse<NewsResponseDTO.NewsList> getMemberNewsForAdmin(
            @PathVariable String memberNickname,
            @RequestParam(required = false) Long cursorId
    ) {
        return ApiResponse.onSuccess(
                newsQueryFacade.fetchMemberNewsListForAdmin(memberNickname, cursorId)
        );
    }
}