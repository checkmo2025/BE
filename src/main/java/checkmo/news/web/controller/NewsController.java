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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "소식", description = "소식 업로드, 조회 API")
public class NewsController {

    private final NewsCommandService newsCommandService;
    private final NewsQueryFacade newsQueryFacade;

    @Operation(summary = "소식 등록", description = "관리자만 소식을 등록할 수 있습니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<Long> createNews(@Valid @RequestBody NewsRequestDTO.CreateNews request) {
        Long newsId = newsCommandService.createNews(request);
        return ApiResponse.onSuccess(newsId);
    }

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