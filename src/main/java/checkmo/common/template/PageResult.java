package checkmo.common.template;

import java.util.List;

/**
 * 페이지 번호 기반 페이징 처리 결과를 담는 제네릭 레코드
 *
 * @param <T>           페이징된 데이터의 타입
 * @param content       현재 페이지의 데이터 리스트
 * @param page          현재 페이지 번호
 * @param size          페이지 당 데이터 수
 * @param totalElements 전체 데이터 수
 * @param totalPages    전체 페이지 수
 * @param hasNext       다음 페이지 존재 여부
 * @param hasPrevious   이전 페이지 존재 여부
 */
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
