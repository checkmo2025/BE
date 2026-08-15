package checkmo.common.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OwnedImageType {
    BOOK_STORY("book-stories"),
    BOOK_STORY_COMMENT("book-story-comments"),
    NOTICE_COMMENT("notice-comments");

    private final String path;
}
