package checkmo.book.internal.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.common.monitoring.CheckmoMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class Yes24SearchClientTest {
    @Test
    void delegatesSearchAndPreservesProviderFailure() {
        var provider = mock(Yes24Client.class);
        var client = new Yes24SearchClient(provider, new CheckmoMetrics(new SimpleMeterRegistry()));
        var result = BookResponseDTO.BookList.builder().build();
        when(provider.retrieveSearchBooks("책", 2)).thenReturn(result);
        assertThat(client.fetchSearchBooks("책", 2)).isSameAs(result);
        var failure = new BookException(BookErrorStatus.YES24_API_ERROR);
        when(provider.retrieveSearchBooks("책", 3)).thenThrow(failure);
        assertThatThrownBy(() -> client.fetchSearchBooks("책", 3)).isSameAs(failure);
    }
}
