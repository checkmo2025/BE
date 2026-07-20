package checkmo.book.internal.service.query;

import checkmo.book.internal.config.properties.AladinProperties;
import checkmo.book.internal.converter.BookConverter;
import checkmo.book.web.dto.AladinApiResponseDTO;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.common.monitoring.CheckmoMetrics;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class AladinSearchClient {

    private final RestTemplate restTemplate;
    private final AladinProperties aladinProperties;
    private final CheckmoMetrics checkmoMetrics;

    public BookResponseDTO.BookList fetchSearchBooks(String keyword, int page) {
        Timer.Sample sample = checkmoMetrics.startTimer();
        AladinApiResponseDTO.BookList response;
        try {
            response = restTemplate.getForObject(
                    buildHttpUrl(keyword, page),
                    AladinApiResponseDTO.BookList.class
            );
        } catch (RestClientException e) {
            checkmoMetrics.recordAladinClientResult(
                    sample,
                    "search",
                    checkmoMetrics.classifyAladinResult(e),
                    e
            );
            throw new IllegalStateException(sanitizedFailureMessage(e));
        }

        checkmoMetrics.recordAladinClientResult(sample, "search", "success", null);
        return BookConverter.toBookList(response, page);
    }

    private String sanitizedFailureMessage(RestClientException exception) {
        if (exception instanceof RestClientResponseException responseException) {
            return "Aladin API request failed for search books. exceptionType=%s, statusCode=%s".formatted(
                    exception.getClass().getName(),
                    responseException.getStatusCode()
            );
        }
        return "Aladin API request failed for search books. exceptionType=%s".formatted(
                exception.getClass().getName()
        );
    }

    private String buildHttpUrl(String keyword, int page) {
        return UriComponentsBuilder
                .fromUriString(aladinProperties.getUrl().getBase() + aladinProperties.getUrl().getItemSearch())
                .queryParam("ttbkey", aladinProperties.getAuth().getTtbKey())
                .queryParam("Query", keyword)
                .queryParam("QueryType", aladinProperties.getSearch().getSearchQueryType())
                .queryParam("MaxResults", aladinProperties.getSearch().getMaxResults())
                .queryParam("start", page)
                .queryParam("output", aladinProperties.getSearch().getOutput())
                .queryParam("Version", aladinProperties.getAuth().getVersion())
                .build()
                .toUriString();
    }
}
