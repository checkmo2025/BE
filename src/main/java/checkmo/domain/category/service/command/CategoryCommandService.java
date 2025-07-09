package checkmo.domain.category.service.command;

/**
 * 카테고리 자체의 생성, 수정, 삭제 담당
 */
public interface CategoryCommandService {
    /**
     * 카테고리 생성
     *
     * @param title 카테고리 제목
     */
    void createCategory(String title);

    /**
     * 카테고리 수정
     *
     * @param categoryId 카테고리 ID
     * @param title      카테고리 제목
     */
    void updateCategory(Long categoryId, String title);

    /**
     * 카테고리 삭제
     *
     * @param categoryId 카테고리 ID
     */
    void deleteCategory(Long categoryId);
}
