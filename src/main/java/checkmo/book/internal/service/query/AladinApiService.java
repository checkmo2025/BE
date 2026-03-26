package checkmo.book.internal.service.query;

import checkmo.book.internal.config.properties.AladinProperties;
import checkmo.book.internal.converter.BookConverter;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.repository.BookLikedRepository;
import checkmo.book.web.dto.AladinApiResponseDTO;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AladinApiService {

    private static final int RECOMMENDED_MAX_RESULTS = 28;

    private final RestTemplate restTemplate;

    private final AladinProperties aladinProperties;
    private final BookLikedRepository bookLikedRepository;

    public BookResponseDTO.BookList searchBooks(String keyword, int page, String memberId) {
        try {

            String url = buildHttpUrl(keyword, page);

            var response = restTemplate.getForObject(
                    url,
                    AladinApiResponseDTO.BookList.class
            );

            BookResponseDTO.BookList bookList = BookConverter.toBookList(response, page);
            return applyLikedByMe(bookList, memberId);

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

    public BookResponseDTO.BookList retrieveRecommendedBooks() {
        try {
            String url = buildHttpUrl();

            var response = restTemplate.getForObject(
                    url,
                    AladinApiResponseDTO.BookList.class
            );

            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                throw new BookException(BookErrorStatus.ALADIN_API_ERROR);
            }

            return BookConverter.toBookList(response, 1);

        } catch (Exception e) {
            throw new BookException(BookErrorStatus.ALADIN_API_ERROR);
        }
    }

    private String buildHttpUrl() {
        return UriComponentsBuilder
                .fromUriString(aladinProperties.getUrl().getBase() + aladinProperties.getUrl().getItemList())
                .queryParam("ttbkey", aladinProperties.getAuth().getTtbKey())
                .queryParam("QueryType", aladinProperties.getSearch().getRecommendQueryType())
                .queryParam("MaxResults", RECOMMENDED_MAX_RESULTS)
                .queryParam("SearchTarget", aladinProperties.getSearch().getSearchTarget())
                .queryParam("output", aladinProperties.getSearch().getOutput())
                .queryParam("Version", aladinProperties.getAuth().getVersion())
                .build()
                .toUriString();
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

    public BookResponseDTO.BookList applyLikedByMe(BookResponseDTO.BookList bookList, String memberId) {
        if (bookList == null || bookList.getDetailInfoList() == null || bookList.getDetailInfoList().isEmpty()) {
            return bookList;
        }

        List<String> bookIds = bookList.getDetailInfoList().stream()
                .map(DetailInfo::getIsbn)
                .toList();
        Set<String> likedBookIds = bookLikedRepository.findLikedBookIdSet(memberId, bookIds);

        List<DetailInfo> updatedDetails = bookList.getDetailInfoList().stream()
                .map(detail -> DetailInfo.builder()
                        .isbn(detail.getIsbn())
                        .title(detail.getTitle())
                        .author(detail.getAuthor())
                        .imgUrl(detail.getImgUrl())
                        .publisher(detail.getPublisher())
                        .description(detail.getDescription())
                        .link(detail.getLink())
                        .likedByMe(likedBookIds.contains(detail.getIsbn()))
                        .build())
                .toList();

        return BookResponseDTO.BookList.builder()
                .detailInfoList(updatedDetails)
                .hasNext(bookList.isHasNext())
                .currentPage(bookList.getCurrentPage())
                .build();
    }
}
