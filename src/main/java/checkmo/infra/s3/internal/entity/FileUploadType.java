package checkmo.infra.s3.internal.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileUploadType {
    PROFILE("profiles"),
    CLUB("clubs"),
    NOTICE("notices"),
    BOOK_STORY("book-stories"),
    BOOK_STORY_COMMENT("book-story-comments"),
    NOTICE_COMMENT("notice-comments");

    private final String path;
}
