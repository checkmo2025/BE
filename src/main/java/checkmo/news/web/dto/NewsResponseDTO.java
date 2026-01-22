package checkmo.news.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class NewsResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NewsList {
        private List<BasicInfo> basicInfoList;
        private boolean hasNext;
        private Long nextCursor;
        private int pageSize;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasicInfo {
        private Long newsId;
        private String title;
        private String description;
        private String thumbnailUrl;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate publishStartAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailInfo {
        private Long newsId;
        private String title;
        private String content;
        private String thumbnailUrl;
        private String originalLink;
        private List<String> imageUrls;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate publishStartAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminNewsList {
        private List<AdminBasicInfo> basicInfoList;
        private int page;
        private int totalPages;
        private long totalElements;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminBasicInfo {
        private Long newsId;
        private String title;
        private String requesterEmail;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate createdAt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate publishStartAt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate publishEndAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminDetailInfo {
        private Long newsId;
        private String title;
        private String requesterEmail;
        private String content;
        private String thumbnailUrl;
        private String originalLink;
        private List<String> imageUrls;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate createdAt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate publishStartAt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate publishEndAt;
    }
}
