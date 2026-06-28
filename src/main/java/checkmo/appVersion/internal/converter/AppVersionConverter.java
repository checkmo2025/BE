package checkmo.appVersion.internal.converter;

import checkmo.appVersion.internal.entity.AppVersionPolicy;
import checkmo.appVersion.web.dto.AppVersionResponseDTO.VersionPolicy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppVersionConverter {

    public static VersionPolicy toVersionPolicy(AppVersionPolicy policy) {
        return VersionPolicy.builder()
                .minSupportedVersion(policy.getMinSupportedVersion())
                .latestVersion(policy.getLatestVersion())
                .storeUrl(policy.getStoreUrl())
                .build();
    }
}
