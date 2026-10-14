package checkmo.book.internal.service.query;

import checkmo.book.web.dto.BookResponseDTO;
import checkmo.common.monitoring.CheckmoMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AladinSearchClient {

    private final Yes24Client yes24Client;
    private final CheckmoMetrics checkmoMetrics;

    public BookResponseDTO.BookList fetchSearchBooks(String keyword, int page) {
        var sample = checkmoMetrics.startTimer();
        try {
            var response = yes24Client.retrieveSearchBooks(keyword, page);
            checkmoMetrics.recordAladinClientResult(sample, "search", "success", null);
            return response;
        } catch (RuntimeException e) {
            checkmoMetrics.recordAladinClientResult(sample, "search", checkmoMetrics.classifyAladinResult(e), e);
            throw e;
        }
    }
}
