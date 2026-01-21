package checkmo.common.template;

import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PagePagingHelper {
    private PagePagingHelper() {
    }

    /**
     * Offset 기반 페이지네이션 로직을 처리하는 제너릭 메서드(Spring Data Pageable 사용)
     *
     * @param dataFetcher Pageable을 받아 Page<T>를 반환하는 함수
     * @param page        1-based 페이지 번호
     * @param pageSize    페이지 당 데이터 수
     * @param <T>         페이징할 데이터 타입
     * @return PageResult
     */
    public static <T> PageResult<T> getPage(
            Function<Pageable, Page<T>> dataFetcher,
            int page,
            int pageSize
    ) {
        int safePage = Math.max(page, 1);
        Pageable pageable = PageRequest.of(safePage - 1, pageSize); // 0-based 페이지 인덱스

        Page<T> result = dataFetcher.apply(pageable);

        return new PageResult<>(
                result.getContent(),
                safePage,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext(),
                result.hasPrevious()
        );
    }
}
