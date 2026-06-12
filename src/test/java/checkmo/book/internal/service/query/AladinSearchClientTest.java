package checkmo.book.internal.service.query;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import checkmo.book.internal.config.properties.AladinProperties;
import checkmo.book.web.dto.AladinApiResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class AladinSearchClientTest {

    @Test
    void fetchSearchBooksThrowsSanitizedExceptionWhenRestTemplateFails() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        AladinSearchClient client = new AladinSearchClient(restTemplate, aladinProperties());
        RestClientException failure = new RestClientException(
                "I/O error on GET request for \"https://example.com/ItemSearch.aspx?ttbkey=secret-key&Query=자바\""
        );
        when(restTemplate.getForObject(anyString(), eq(AladinApiResponseDTO.BookList.class))).thenThrow(failure);

        assertThatThrownBy(() -> client.fetchSearchBooks("자바", 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Aladin API request failed for search books")
                .hasMessageContaining(RestClientException.class.getName())
                .hasMessageNotContaining("secret-key")
                .hasMessageNotContaining("자바")
                .hasNoCause();
    }

    private AladinProperties aladinProperties() {
        AladinProperties properties = new AladinProperties();
        properties.getUrl().setBase("https://example.com");
        properties.getUrl().setItemSearch("/ItemSearch.aspx");
        properties.getAuth().setTtbKey("secret-key");
        properties.getAuth().setVersion("20131101");
        properties.getSearch().setSearchQueryType("Keyword");
        properties.getSearch().setMaxResults(10);
        properties.getSearch().setOutput("js");
        return properties;
    }
}
