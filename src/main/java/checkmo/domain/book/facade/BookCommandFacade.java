package checkmo.domain.book.facade;

import checkmo.domain.book.dto.BookRequestDTO;

/**
 * Book Domain Command Facade
 * Book 도메인의 Command(생성, 수정, 삭제) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface BookCommandFacade {

    /**
     * 알라딘 API로부터 얻은 책 정보를 저장합니다. (내부용)
     *
     * @param request 저장할 책 정보 DTO
     */
    void saveBookFromAladin(BookRequestDTO.Aladin2BookDTO request); //

    /**
     * 특정 책 정보를 삭제합니다. (내부용)
     *
     * @param bookId 삭제할 책의 ID (ISBN)
     */
    void deleteBook(String bookId); //
}