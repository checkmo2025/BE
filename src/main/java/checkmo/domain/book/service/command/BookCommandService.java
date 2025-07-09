package checkmo.domain.book.service.command;

import checkmo.domain.book.dto.BookRequestDTO;

/**
 *  알라딘 API에서 책 정보를 가져와 저장
 *  관리자가 책 정보를 수정하거나 삭제하는 작업
 *
 */
public interface BookCommandService {

    /**
     * 책 정보를 알라딘 API에서 가져와 저장
     *
     * @param request 알라딘 책 DTO
     */
    void saveBookFromAladin(BookRequestDTO.Aladin2BookDTO request);
    // 책 고유번호는 ISBN 13자리로
    //

    /**
     * 책 정보를 삭제
     *
     * @param bookId 삭제할 책의 ID
     */
    void deleteBook(String bookId);
}
