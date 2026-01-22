package checkmo.news.web.controller;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.news.internal.service.command.NewsCommandService;
import checkmo.news.web.dto.NewsRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "소식", description = "소식 업로드, 조회 API")
public class NewsController {

    private final NewsCommandService newsCommandService;

    @Operation(summary = "소식 등록", description = "관리자만 소식을 등록할 수 있습니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<Long> createNews(
            @Valid @RequestBody NewsRequestDTO.CreateNews request
    ) {
        Long newsId = newsCommandService.createNews(request);
        return ApiResponse.onSuccess(newsId);
    }

    // 소식 조회 API

    // 소식 상세 조회 API
}