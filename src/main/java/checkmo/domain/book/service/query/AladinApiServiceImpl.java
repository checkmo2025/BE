package checkmo.domain.book.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.config.properties.AladinProperties;
import checkmo.domain.book.converter.BookConverter;
import checkmo.domain.book.web.dto.AladinApiResponseDTO;
import checkmo.domain.book.web.dto.BookResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AladinApiServiceImpl implements AladinApiService {

    private final RestTemplate restTemplate;
    private final AladinProperties aladinProperties;

    @Override
    public BookResponseDTO.BookListResponseDTO searchBookFromAladin(String keyword, int page) {
        try {

            String url = buildHttpUrl(keyword, page);

            log.info("알라딘 API 요청 URL: {}", url);

            var response = restTemplate.getForObject(
                    url,
                    AladinApiResponseDTO.AladinApiResponse.class
            );

            return BookConverter.fromAladinApiResponse(response, page);

        } catch (Exception e) {
            log.error("알라딘 API 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("알라딘 API 호출 실패", e);
        }
    }

    @Override
    public BookResponseDTO.BookInfoDetailResponseDTO getBookDetailInfoFromAladin(String isbn) {
        try {
            String url = buildHttpUrl(isbn);

            log.info("알라딘 API 요청 URL: {}", url);

            var response = restTemplate.getForObject(
                    url,
                    AladinApiResponseDTO.AladinApiResponse.class
            );

            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                throw new GeneralException(ErrorStatus.BOOK_NOT_FOUND);
            }

            return BookConverter.fromAladinApiResponse(response);

        } catch (GeneralException e) {
            log.error("알라딘 API 호출 중 오류 발생: {}", e.getMessage());
            throw new GeneralException(ErrorStatus.BOOK_NOT_FOUND);
        } catch (Exception e) {
            log.error("알라딘 API 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("알라딘 API 호출 실패", e);
        }
    }

    private String buildHttpUrl(String keyword, int page) {
        return UriComponentsBuilder
                .fromUriString(aladinProperties.getUrl().getBase() + aladinProperties.getUrl().getItemSearch())
                .queryParam("ttbkey", aladinProperties.getAuth().getTtbKey())
                .queryParam("Query", keyword)
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
