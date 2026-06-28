package checkmo.appVersion.internal.entity;

import checkmo.appVersion.internal.exception.AppVersionErrorStatus;
import checkmo.appVersion.internal.exception.AppVersionException;
import java.util.Arrays;

public enum AppPlatform {
    IOS,
    ANDROID;

    public static AppPlatform from(String value) {
        String normalized = value == null ? "" : value.trim();

        return Arrays.stream(values())
                .filter(platform -> platform.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new AppVersionException(AppVersionErrorStatus.INVALID_PLATFORM));
    }
}
