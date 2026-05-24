package checkmo.report.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.common.apiPayload.ApiResponse;
import checkmo.report.internal.service.command.ReportCommandService;
import checkmo.report.internal.service.query.ReportQueryService;
import checkmo.report.web.dto.ReportRequestDTO;
import checkmo.report.web.dto.ReportResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "신고", description = "신고 API")
public class ReportController {

    private final ReportCommandService reportCommandService;
    private final ReportQueryService reportQueryService;

    @PostMapping
    public ApiResponse<Long> createReport(
            @CurrentId String reporterId,
            @Valid @RequestBody ReportRequestDTO.Create request
    ) {
        return ApiResponse.onSuccess(reportCommandService.createReport(reporterId, request));
    }

    @GetMapping("/me")
    public ApiResponse<ReportResponseDTO.MyReportList> getMyReports(
            @CurrentId String memberId,
            @RequestParam(required = false) Long cursorId
    ) {
        return ApiResponse.onSuccess(reportQueryService.retrieveMyReports(memberId, cursorId));
    }
}