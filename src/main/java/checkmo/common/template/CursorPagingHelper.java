package checkmo.common.template;

import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class CursorPagingHelper {

    /**
     * 커서 기반 페이징 로직을 처리하는 제네릭 메소드
     *
     * @param dataFetcher pageSize + 1 만큼 데이터를 조회하는 함수
     * @param idExtractor 데이터 객체에서 ID(커서)를 추출하는 함수
     * @param pageSize    페이지 당 데이터 수
     * @param <T>         페이징할 엔티티 타입 (예: Recipe, RecipeComment)
     * @return 페이징 처리 결과가 담긴 CursorResult 객체
     */
    public <T> CursorResult<T> getPage(
            Function<Integer, List<T>> dataFetcher,
            Function<T, Long> idExtractor,
            int pageSize
    ) {
        // 1. 다음 페이지 확인을 위해 pageSize + 1 만큼 데이터 조회
        List<T> items = dataFetcher.apply(pageSize + 1);

        // 2. 다음 페이지 존재 여부 확인
        boolean hasNext = items.size() > pageSize;

        // 3. hasNext가 true이면 마지막 항목(다음 페이지 확인용) 제거
        if (hasNext) {
            items.removeLast();
        }

        // 4. 다음 커서 값 계산
        Long nextCursor = null;
        if (hasNext && !items.isEmpty()) {
            nextCursor = idExtractor.apply(items.getLast());
        }

        // 5. 최종 결과 반환
        return new CursorResult<>(items, hasNext, nextCursor);
    }
}
