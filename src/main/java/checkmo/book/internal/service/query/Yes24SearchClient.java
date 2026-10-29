package checkmo.book.internal.service.query;

import checkmo.book.web.dto.BookResponseDTO;
import checkmo.common.monitoring.CheckmoMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Yes24SearchClient {

    private final Yes24Client yes24Client;
    private final CheckmoMetrics checkmoMetrics;

    public BookResponseDTO.BookList fetchSearchBooks(String keyword, int page) {
        var sample = checkmoMetrics.startTimer();
        try {
            var response = yes24Client.retrieveSearchBooks(keyword, page);
            checkmoMetrics.recordYes24ClientResult(sample, "search", "success", null);
            return response;
        } catch (RuntimeException e) {
            checkmoMetrics.recordYes24ClientResult(sample, "search", checkmoMetrics.classifyYes24Result(e), e);
            throw e;
        }
    }
}
