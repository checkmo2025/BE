package checkmo.domain.book.service.command;

import checkmo.global.dto.BookSharedDTO;

/**
 *  알라딘 API에서 책 정보를 가져와 저장
 *  관리자가 책 정보를 수정하거나 삭제하는 작업
 *
 */
public interface BookCommandService {

    /**
     * 책 정보를 알라딘 API에서 가져와 저장
     * 이 메소드의 파라미터는 BookStory, BookRecommed, Meeting 등 책을 검색하고 해당 객체 생성할 때 전달된다.
     *
     * @param request 알라딘 책 DTO
     */
    void saveBook(BookSharedDTO.BookCreateRequestDTO request);
    // 책 고유번호는 ISBN 13자리로

    /**
     * 책 정보를 삭제
     *
     * @param bookId 삭제할 책의 ID
     */
    void deleteBook(String bookId);
}
