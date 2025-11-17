package checkmo.book.internal.service.query;

import checkmo.book.internal.converter.BookConverter;
import checkmo.book.web.dto.AladinApiResponseDTO;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.book.internal.config.properties.AladinProperties;
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

    // 알라딘 API 호출을 위한 RestTemplate
    private final RestTemplate restTemplate;

    // 알라딘의 환경 설정 정보를 담은 Properties
    private final AladinProperties aladinProperties;

    @Override
    public BookResponseDTO.BookList searchBookFromAladin(String keyword, int page) {
        try {

            String url = buildHttpUrl(keyword, page);

            log.info("알라딘 API 요청 URL: {}", url);

            var response = restTemplate.getForObject(
                    url,
                    AladinApiResponseDTO.BookList.class
            );

            return BookConverter.fromAladinApiResponse(response, page);

        } catch (Exception e) {
            log.error("알라딘 API 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("알라딘 API 호출 실패", e);
        }
    }

    @Override
    public BookResponseDTO.BookInfoDetail getBookDetailInfoFromAladin(String isbn) {
        try {
            String url = buildHttpUrl(isbn);

            log.info("알라딘 API 요청 URL: {}", url);

            var response = restTemplate.getForObject(
                    url,
                    AladinApiResponseDTO.BookList.class
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
