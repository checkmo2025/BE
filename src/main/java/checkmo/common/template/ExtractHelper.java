package checkmo.common.template;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExtractHelper {
    private ExtractHelper() {
    }

    /**
     * source 컬렉션에서 값을 추출하고, 중복을 제거한 리스트를 반환하는 제너릭 메서드
     *
     * @param source 값을 추출할 원본 컬렉션
     * @param extractor 값을 추출하는 함수
     * @param <T> 원본 컬렉션의 데이터 타입
     * @param <R> 추출할 값의 데이터 타입
     * @return 추출된 값의 중복이 제거된 리스트
     */
    public static <T, R> List<R> extractDistinctList(
            Collection<T> source,
            Function<T, R> extractor
    ) {
        if (source == null || source.isEmpty()) return List.of();
        return source.stream()
                .map(extractor)
                .distinct()
                .toList();
    }

    /**
     * source 컬렉션에서 값을 추출하고, 세트를 반환하는 제너릭 메서드
     *
     * @param source 값을 추출할 원본 컬렉션
     * @param extractor 값을 추출하는 함수
     * @param <T> 원본 컬렉션의 데이터 타입
     * @param <R> 추출할 값의 데이터 타입
     * @return 추출된 값의 중복이 제거된 세트
     */
    public static <T, R> Set<R> extractSet(
            Collection<T> source,
            Function<T, R> extractor
    ) {
        if (source == null || source.isEmpty()) return Set.of();
        return source.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
