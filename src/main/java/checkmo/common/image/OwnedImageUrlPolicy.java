package checkmo.common.image;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class OwnedImageUrlPolicy {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif", "heic", "heif"
    );
    private final String expectedHost;

    public OwnedImageUrlPolicy(
            @Value("${cloud.aws.s3.bucket}") String bucket,
            @Value("${cloud.aws.region.name}") String region
    ) {
        this.expectedHost = "%s.s3.%s.amazonaws.com".formatted(bucket, region);
    }

    public boolean isOwnedBy(List<String> imageUrls, Long memberId, OwnedImageType imageType) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return true;
        }
        if (memberId == null || imageType == null || new HashSet<>(imageUrls).size() != imageUrls.size()) {
            return false;
        }
        return imageUrls.stream().allMatch(imageUrl -> isOwnedBy(imageUrl, memberId, imageType));
    }

    private boolean isOwnedBy(String imageUrl, Long memberId, OwnedImageType imageType) {
        try {
            URI uri = URI.create(imageUrl);
            String host = uri.getHost();
            String rawPath = uri.getRawPath();
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || host == null
                    || !expectedHost.equalsIgnoreCase(host)
                    || uri.getUserInfo() != null
                    || uri.getPort() != -1
                    || uri.getQuery() != null
                    || uri.getFragment() != null
                    || rawPath == null
                    || rawPath.contains("%")) {
                return false;
            }

            String expectedPrefix = "/images/%s/%d/".formatted(imageType.getPath(), memberId);
            if (!rawPath.startsWith(expectedPrefix)) {
                return false;
            }

            String fileName = rawPath.substring(expectedPrefix.length());
            if (fileName.isBlank() || fileName.contains("/")) {
                return false;
            }

            int extensionIndex = fileName.lastIndexOf('.');
            String uuidPart = extensionIndex < 0 ? fileName : fileName.substring(0, extensionIndex);
            if (extensionIndex >= 0) {
                String extension = fileName.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
                if (!ALLOWED_EXTENSIONS.contains(extension)) {
                    return false;
                }
            }
            UUID.fromString(uuidPart);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
