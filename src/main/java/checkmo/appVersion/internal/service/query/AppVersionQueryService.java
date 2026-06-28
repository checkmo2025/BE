package checkmo.appVersion.internal.service.query;

import checkmo.appVersion.internal.entity.AppPlatform;
import checkmo.appVersion.internal.entity.AppVersionPolicy;
import checkmo.appVersion.internal.exception.AppVersionErrorStatus;
import checkmo.appVersion.internal.exception.AppVersionException;
import checkmo.appVersion.internal.repository.AppVersionPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppVersionQueryService {

    private final AppVersionPolicyRepository appVersionPolicyRepository;

    public AppVersionPolicy retrieveActivePolicy(String platformValue) {
        AppPlatform platform = AppPlatform.from(platformValue);

        return appVersionPolicyRepository.findByPlatformAndActiveTrue(platform)
                .orElseThrow(() -> new AppVersionException(AppVersionErrorStatus.POLICY_NOT_FOUND));
    }
}
