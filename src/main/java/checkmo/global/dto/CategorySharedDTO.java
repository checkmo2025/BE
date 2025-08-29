package checkmo.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 카테고리 도메인과 관련된 공유 DTO 클래스
 * 다른 도메인에서 카테고리 정보를 참조할 때 사용
 */
public class CategorySharedDTO {

    /**
     * 카테고리 목록 응답 DTO
     * 회원 가입, 클럽 생성 시 여러 카테고리의 정보를 리스트 형태로 반환할 때 사용
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryInfoList {
        private List<CategoryInfo> categoryList;
    }

    /**
     * 카테고리 기본 정보 DTO
     * 카테고리의 기본적인 정보(ID, 이름)를 포함
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryInfo {
        private Long id;   // 카테고리 ID
        private String name; // 카테고리 이름
    }

    /**
     * 카테고리 ID 목록 요청 DTO
     * 회원가입 시 카테고리 설정할 때 사용
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryIdList {
        private List<Long> categoryIdList;
    }
}