package checkmo.book.internal.converter;

import static org.assertj.core.api.Assertions.assertThat;

import checkmo.book.web.dto.AladinApiResponseDTO;
import checkmo.book.web.dto.BookResponseDTO;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

class BookConverterTest {

    private final XmlMapper xmlMapper = new XmlMapper();

    @Test
    void toBookListIncludesTotalResultsFromAladinResponse() throws Exception {
        AladinApiResponseDTO.BookList aladinResponse = xmlMapper.readValue(
                """
                        <object>
                            <totalResults>37</totalResults>
                            <startIndex>1</startIndex>
                            <itemsPerPage>10</itemsPerPage>
                            <item>
                                <title>테스트 책</title>
                                <author>테스트 저자 (지은이)</author>
                                <isbn13>9791169213882</isbn13>
                                <cover>https://image.aladin.co.kr/product/coversum/test.jpg</cover>
                                <publisher>테스트 출판사</publisher>
                                <description>테스트 설명</description>
                                <link>https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=1</link>
                            </item>
                        </object>
                        """,
                AladinApiResponseDTO.BookList.class
        );

        BookResponseDTO.BookList response = BookConverter.toBookList(aladinResponse, 1);

        assertThat(response.getTotalResults()).isEqualTo(37);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getDetailInfoList()).singleElement()
                .satisfies(book -> {
                    assertThat(book.getIsbn()).isEqualTo("9791169213882");
                    assertThat(book.getAuthor()).isEqualTo("테스트 저자");
                });
    }

    @Test
    void toBookListReturnsZeroTotalResultsForInvalidResponse() {
        BookResponseDTO.BookList response = BookConverter.toBookList(null, 1);

        assertThat(response.getTotalResults()).isZero();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getCurrentPage()).isNull();
        assertThat(response.getDetailInfoList()).isEmpty();
    }
}
