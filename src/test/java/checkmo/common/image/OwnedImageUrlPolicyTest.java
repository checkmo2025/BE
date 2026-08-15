package checkmo.common.image;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class OwnedImageUrlPolicyTest {

    private final OwnedImageUrlPolicy policy = new OwnedImageUrlPolicy("test-bucket", "ap-northeast-2");

    @Test
    void acceptsOnlyCurrentMembersUrlForExpectedImageType() {
        String ownedUrl = imageUrl(7L, OwnedImageType.BOOK_STORY, 1);

        assertThat(policy.isOwnedBy(List.of(ownedUrl), 7L, OwnedImageType.BOOK_STORY)).isTrue();
        assertThat(policy.isOwnedBy(List.of(ownedUrl), 8L, OwnedImageType.BOOK_STORY)).isFalse();
        assertThat(policy.isOwnedBy(List.of(ownedUrl), 7L, OwnedImageType.BOOK_STORY_COMMENT)).isFalse();
    }

    @Test
    void rejectsForeignBucketMalformedAndDuplicateUrls() {
        String ownedUrl = imageUrl(7L, OwnedImageType.BOOK_STORY, 1);
        String foreignBucketUrl = ownedUrl.replace("test-bucket", "foreign-bucket");
        String encodedPathUrl = ownedUrl.replace("images/", "images%2F");

        assertThat(policy.isOwnedBy(List.of(foreignBucketUrl), 7L, OwnedImageType.BOOK_STORY)).isFalse();
        assertThat(policy.isOwnedBy(List.of(encodedPathUrl), 7L, OwnedImageType.BOOK_STORY)).isFalse();
        assertThat(policy.isOwnedBy(List.of(ownedUrl, ownedUrl), 7L, OwnedImageType.BOOK_STORY)).isFalse();
    }

    @Test
    void allowsNullAndEmptyListsForBackwardCompatibleRequests() {
        assertThat(policy.isOwnedBy(null, 7L, OwnedImageType.BOOK_STORY)).isTrue();
        assertThat(policy.isOwnedBy(List.of(), 7L, OwnedImageType.BOOK_STORY)).isTrue();
    }

    private String imageUrl(Long memberId, OwnedImageType type, int index) {
        return "https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/%s/%d/00000000-0000-0000-0000-%012d.jpg"
                .formatted(type.getPath(), memberId, index);
    }
}
