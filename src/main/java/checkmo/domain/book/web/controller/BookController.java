package checkmo.domain.book.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.book.web.dto.BookResponseDTO;
import checkmo.domain.book.facade.BookQueryFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "책 검색", description = "알라딘 API를 이용한 책 검색 API")
public class BookController {

    private final BookQueryFacade bookQueryFacade;

    @Operation(summary = "책 검색 API", description = "키워드를 이용해 알라딘에서 책 목록을 검색합니다.")
    @Parameters({
            @Parameter(name = "keyword", description = "검색할 키워드 (책 제목, 저자)", required = true, example = "자바"),
            @Parameter(name = "page", description = "페이지 번호 (값 넣지 않으면 1부터 시작 그 다음 요청은 반드시 2!!)", example = "1")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 정보를 찾을 수 없음"),
    })
    @GetMapping("/search")
    public ApiResponse<BookResponseDTO.BookListResponseDTO> searchBook(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "1")
            @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
            int page
    ) {
        BookResponseDTO.BookListResponseDTO result = bookQueryFacade.searchBookFromAladin(keyword, page);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "책 상세 정보 조회 API", description = "ISBN 코드를 이용해 책의 상세 정보를 조회합니다.")
    @Parameter(name = "isbn", description = "책의 13자리 ISBN", required = true, example = "9791169213882")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책 정보를 찾을 수 없음"),
    })
    @GetMapping({"/{isbn}"})
    public ApiResponse<BookResponseDTO.BookInfoDetailResponseDTO> getBookDetail(@PathVariable String isbn) {

        BookResponseDTO.BookInfoDetailResponseDTO result = bookQueryFacade.findBook(isbn);
        return ApiResponse.onSuccess(result);
    }
}
