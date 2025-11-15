package checkmo.common.template;

import java.util.List;

/**
 * 커서 기반 페이징 처리 결과를 담는 제네릭 레코드
 * @param <T> 페이징된 데이터의 타입
 * @param content 현재 페이지의 데이터 리스트
 * @param hasNext 다음 페이지 존재 여부
 * @param nextCursor 다음 페이지를 조회하기 위한 커서 ID
 */
public record CursorResult<T>(
        List<T> content,
        boolean hasNext,
        Long nextCursor
) {
}
