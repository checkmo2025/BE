package checkmo.news.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.common.apiPayload.ApiResponse;
import checkmo.news.internal.service.NewsQueryFacade;
import checkmo.news.web.dto.NewsResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Validated
@Tag(name = "소식", description = "소식 조회 API")
public class NewsController {

    private final NewsQueryFacade newsQueryFacade;

    @Operation(summary = "소식 조회", description = "공개 기간 내의 소식 목록을 조회합니다.")
    @Parameter(name = "cursorId", description = "커서 ID (페이징을 위한 커서, 처음에는 null)", required = false, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @GetMapping
    public ApiResponse<NewsResponseDTO.NewsList> getNewsList(@RequestParam(required = false) Long cursorId) {
        NewsResponseDTO.NewsList newsList = newsQueryFacade.fetchNewsList(cursorId);
        return ApiResponse.onSuccess(newsList);
    }

    @Operation(summary = "내 소식 조회", description = "로그인한 사용자가 요청한 소식 목록을 조회합니다.")
    @Parameter(name = "cursorId", description = "커서 ID (페이징을 위한 커서, 처음에는 null)", required = false, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @GetMapping("/me")
    public ApiResponse<NewsResponseDTO.NewsList> getMyNewsList(
            @CurrentId String memberId,
            @RequestParam(required = false) Long cursorId
    ) {
        NewsResponseDTO.NewsList newsList = newsQueryFacade.fetchMyNewsList(memberId, cursorId);
        return ApiResponse.onSuccess(newsList);
    }

    @Operation(summary = "소식 사이트맵 메타데이터 조회", description = "공개 기간 내 프로모션 소식의 사이트맵 메타데이터를 조회합니다.")
    @Parameters({
            @Parameter(name = "cursorId", description = "커서 ID (페이징을 위한 커서, 처음에는 null)", required = false, example = "10"),
            @Parameter(name = "limit", description = "조회 개수 (기본 1000, 최대 5000)", required = false, example = "1000")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/sitemap")
    public ApiResponse<NewsResponseDTO.SitemapPage> getNewsSitemap(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) @Min(1) Integer limit
    ) {
        return ApiResponse.onSuccess(newsQueryFacade.fetchNewsSitemap(cursorId, limit));
    }

    @Operation(summary = "소식 상세 조회", description = "특정 소식의 상세 정보를 조회합니다.")
    @Parameter(name = "newsId", description = "조회할 소식 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "소식을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @GetMapping("/{newsId}")
    public ApiResponse<NewsResponseDTO.DetailInfo> getNews(@PathVariable Long newsId) {
        NewsResponseDTO.DetailInfo detailInfo = newsQueryFacade.fetchNewsDetail(newsId);
        return ApiResponse.onSuccess(detailInfo);
    }
}
