package checkmo.infra.s3.internal.entity;

import checkmo.common.image.OwnedImageType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileUploadType {
    PROFILE("profiles"),
    CLUB("clubs"),
    NOTICE("notices"),
    BOOK_STORY(OwnedImageType.BOOK_STORY.getPath()),
    BOOK_STORY_COMMENT(OwnedImageType.BOOK_STORY_COMMENT.getPath()),
    NOTICE_COMMENT(OwnedImageType.NOTICE_COMMENT.getPath());

    private final String path;
}
