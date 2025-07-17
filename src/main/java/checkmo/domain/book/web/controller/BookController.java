package checkmo.domain.book.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.book.web.dto.BookResponseDTO;
import checkmo.domain.book.facade.BookCommandFacade;
import checkmo.domain.book.facade.BookQueryFacade;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "책 검색", description = "알라딘 API를 이용한 책 검색 API")
public class BookController {

    private final BookQueryFacade bookQueryFacade;
    private final BookCommandFacade bookCommandFacade;

    @GetMapping("/search")
    public ApiResponse<BookResponseDTO.BookListResponseDTO> searchBook(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "1") int page
    ) {
        BookResponseDTO.BookListResponseDTO result = bookQueryFacade.searchBookFromAladin(keyword, page);
        return ApiResponse.onSuccess(result);
    }
}
