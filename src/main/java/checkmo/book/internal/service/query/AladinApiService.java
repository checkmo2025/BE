package checkmo.book.internal.service.query;

import checkmo.book.internal.config.properties.AladinProperties;
import checkmo.book.internal.converter.BookConverter;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.web.dto.AladinApiResponseDTO;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AladinApiService {

    private final RestTemplate restTemplate;

    private final AladinProperties aladinProperties;

    public BookResponseDTO.BookList searchBooks(String keyword, int page) {
        try {

            String url = buildHttpUrl(keyword, page);

            var response = restTemplate.getForObject(
                    url,
                    AladinApiResponseDTO.BookList.class
            );

            return BookConverter.toBookList(response, page);

        } catch (Exception e) {
            throw new BookException(BookErrorStatus.ALADIN_API_ERROR);
        }
    }

    public DetailInfo retrieveBookDetailInfo(String isbn) {
        try {
            String url = buildHttpUrl(isbn);

            var response = restTemplate.getForObject(
                    url,
                    AladinApiResponseDTO.BookList.class
            );

            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                throw new BookException(BookErrorStatus.BOOK_NOT_FOUND);
            }

            return BookConverter.toBookInfoDetail(response);

        } catch (BookException e) {
            throw new BookException(BookErrorStatus.BOOK_NOT_FOUND);
        } catch (Exception e) {
            throw new BookException(BookErrorStatus.ALADIN_API_ERROR);
        }
    }

    private String buildHttpUrl(String keyword, int page) {
        return UriComponentsBuilder
                .fromUriString(aladinProperties.getUrl().getBase() + aladinProperties.getUrl().getItemSearch())
                .queryParam("ttbkey", aladinProperties.getAuth().getTtbKey())
                .queryParam("Query", keyword)
                .queryParam("QueryType", aladinProperties.getSearch().getQueryType())
                .queryParam("MaxResults", aladinProperties.getSearch().getMaxResults())
                .queryParam("start", page)
                .queryParam("output", aladinProperties.getSearch().getOutput())
                .queryParam("Version", aladinProperties.getAuth().getVersion())
                .build()
                .toUriString();
    }

    private String buildHttpUrl(String isbn) {
        return UriComponentsBuilder
                .fromUriString(aladinProperties.getUrl().getBase() + aladinProperties.getUrl().getItemLookup())
                .queryParam("ttbkey", aladinProperties.getAuth().getTtbKey())
                .queryParam("itemIdType", aladinProperties.getSearch().getItemIdType())
                .queryParam("ItemId", isbn)
                .queryParam("output", aladinProperties.getSearch().getOutput())
                .queryParam("Version", aladinProperties.getAuth().getVersion())
                .build()
                .toUriString();
    }
}
