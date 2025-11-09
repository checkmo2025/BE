package checkmo.category.internal.service.query;

import checkmo.category.internal.entity.Category;
import java.util.List;

/**
 * 카테고리 조회 서비스 카테고리 자체에 대한 조회만 담당
 */
public interface CategoryQueryService {

    /**
     * 모든 카테고리 목록 조회
     *
     * @return 카테고리 엔티티 리스트
     */
    List<Category> findAllCategories();

}
